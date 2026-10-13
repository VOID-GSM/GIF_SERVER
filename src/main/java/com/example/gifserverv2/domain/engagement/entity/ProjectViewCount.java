package com.example.gifserverv2.domain.engagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "project_view_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectViewCount implements Persistable<Long> {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private long views;

    /** 직접 생성한 객체는 항상 INSERT 되도록 한다. (merge 로 기존 조회수를 덮어쓰지 않기 위함) */
    @Transient
    private boolean isNew;

    public ProjectViewCount(Long projectId, long views) {
        this.projectId = projectId;
        this.views = views;
        this.isNew = true;
    }

    @Override
    public Long getId() {
        return projectId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
