package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectStatsRepository extends JpaRepository<Project, Long> {

    @Query(value = """
            SELECT p.id AS projectId, p.name AS name, p.team_name AS teamName,
                   p.grade AS grade, p.class_no AS classNo,
                   COALESCE(v.views, 0) AS viewCount,
                   COALESCE(l.cnt, 0)   AS likeCount,
                   COALESCE(c.cnt, 0)   AS commentCount
            FROM project p
            LEFT JOIN project_view_count v ON v.project_id = p.id
            LEFT JOIN (SELECT project_id, COUNT(*) AS cnt FROM project_like
                       GROUP BY project_id) l ON l.project_id = p.id
            LEFT JOIN (SELECT project_id, COUNT(*) AS cnt FROM project_comment
                       GROUP BY project_id) c ON c.project_id = p.id
            WHERE (:grade IS NULL OR p.grade = :grade)
              AND (:classNo IS NULL OR p.class_no = :classNo)
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