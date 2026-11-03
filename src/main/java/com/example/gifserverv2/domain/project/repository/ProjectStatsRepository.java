package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 통계 조회 전용. 기존 ProjectRepository는 건드리지 않기 위해 따로 만들었다.
public interface ProjectStatsRepository extends JpaRepository<Project, Long> {

    /*
     * 핵심 아이디어:
     * 1) 좋아요/댓글은 project_id 기준으로 "먼저 각각 집계"한 뒤 LEFT JOIN -> 카운트가 곱해지지 않는다.
     * 2) project.class_no 는 저장하는 코드가 없어 항상 null 이므로,
     *    팀원 학번(4자리: 학년+반+번호)의 두 번째 자리에서 반을 구해 대신 쓴다. (m 서브쿼리)
     *    팀원이 여러 반이면 가장 작은 반을 대표 반으로 한다. 미리 GROUP BY 해서 행이 늘어나지 않는다.
     * 3) project.class_no 에 값이 생기면 그 값을 우선 사용한다. (COALESCE)
     * SELECT만 있어서 데이터는 바뀌지 않는다.
     */
    @Query(value = """
            SELECT p.id AS projectId, p.name AS name, p.team_name AS teamName,
                   p.grade AS grade,
                   COALESCE(p.class_no, m.class_no) AS classNo,
                   COALESCE(v.views, 0) AS viewCount,
                   COALESCE(l.cnt, 0)   AS likeCount,
                   COALESCE(c.cnt, 0)   AS commentCount
            FROM project p
            LEFT JOIN project_view_count v ON v.project_id = p.id
            LEFT JOIN (SELECT project_id, COUNT(*) AS cnt FROM project_like
                       GROUP BY project_id) l ON l.project_id = p.id
            LEFT JOIN (SELECT project_id, COUNT(*) AS cnt FROM project_comment
                       GROUP BY project_id) c ON c.project_id = p.id
            LEFT JOIN (SELECT pm.project_id,
                              MIN(CAST(SUBSTRING(u.student_number, 2, 1) AS UNSIGNED)) AS class_no
                       FROM project_member pm
                       JOIN users u ON u.id = pm.user_id
                       WHERE u.student_number REGEXP '^[0-9]{4}$'
                       GROUP BY pm.project_id) m ON m.project_id = p.id
            WHERE (:grade IS NULL OR p.grade = :grade)
              AND (:classNo IS NULL OR COALESCE(p.class_no, m.class_no) = :classNo)
            ORDER BY CASE WHEN :sort = 'LIKES' THEN COALESCE(l.cnt, 0)
                          ELSE COALESCE(v.views, 0) END DESC,
                     p.id DESC
            """, nativeQuery = true)
    List<ProjectStatsRow> findStats(@Param("grade") Integer grade,
                                    @Param("classNo") Integer classNo,
                                    @Param("sort") String sort);

    // 쿼리 결과의 별칭(AS)과 getter 이름이 같아야 값이 매핑된다.
    interface ProjectStatsRow {
        Long getProjectId();
        String getName();
        String getTeamName();
        Integer getGrade();
        Integer getClassNo();
        Long getViewCount();
        Long getLikeCount();
        Long getCommentCount();
    }
}