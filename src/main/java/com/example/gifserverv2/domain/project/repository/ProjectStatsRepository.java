package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 통계 조회 전용.
public interface ProjectStatsRepository extends JpaRepository<Project, Long> {

    @Query(value = """
            SELECT p.id AS projectId, p.name AS name, p.team_name AS teamName,
                   COALESCE(p.grade, m.grade) AS grade,
                   COALESCE(p.class_no, m.class_no) AS classNo,
                   COALESCE(v.views, 0) AS viewCount,
                   COALESCE(l.cnt, 0)   AS likeCount,
                   COALESCE(c.cnt, 0)   AS commentCount
            FROM project p
            LEFT JOIN project_view_count v ON v.project_id = p.id
            LEFT JOIN (SELECT project_id, COUNT(*) AS cnt FROM project_comment
                                   WHERE hidden = 0 AND deleted_at IS NULL
                                   GROUP BY project_id) c ON c.project_id = p.id
                       GROUP BY project_id) c ON c.project_id = p.id
            LEFT JOIN (SELECT pm.project_id,
                              MIN(CAST(SUBSTRING(u.student_number, 1, 1) AS UNSIGNED)) AS grade,
                              MIN(CAST(SUBSTRING(u.student_number, 2, 1) AS UNSIGNED)) AS class_no
                       FROM project_member pm
                       JOIN users u ON u.id = pm.user_id
                       WHERE u.student_number REGEXP '^[0-9]{2}'
                       GROUP BY pm.project_id) m ON m.project_id = p.id
            WHERE (:grade IS NULL AND :classNo IS NULL
                   OR EXISTS (SELECT 1 FROM project_member pm2
                              JOIN users u2 ON u2.id = pm2.user_id
                              WHERE pm2.project_id = p.id
                                AND u2.student_number REGEXP '^[0-9]{2}'
                                AND (:grade IS NULL
                                     OR CAST(SUBSTRING(u2.student_number, 1, 1) AS UNSIGNED) = :grade)
                                AND (:classNo IS NULL
                                     OR CAST(SUBSTRING(u2.student_number, 2, 1) AS UNSIGNED) = :classNo)))
            ORDER BY CASE WHEN :sort = 'LIKES' THEN COALESCE(l.cnt, 0)
                          ELSE COALESCE(v.views, 0) END DESC,
                     p.id DESC
            """, nativeQuery = true)
    List<ProjectStatsRow> findStats(@Param("grade") Integer grade,
                                    @Param("classNo") Integer classNo,
                                    @Param("sort") String sort);

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