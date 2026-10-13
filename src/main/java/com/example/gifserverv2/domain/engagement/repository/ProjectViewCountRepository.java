package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectViewCountRepository extends JpaRepository<ProjectViewCount, Long> {

    /** 갱신된 행 수를 반환한다. 0이면 아직 카운터 행이 없는 프로젝트다. */
    @Transactional
    @Modifying
    @Query("UPDATE ProjectViewCount v SET v.views = v.views + 1 WHERE v.projectId = :projectId")
    int increment(@Param("projectId") Long projectId);
}
