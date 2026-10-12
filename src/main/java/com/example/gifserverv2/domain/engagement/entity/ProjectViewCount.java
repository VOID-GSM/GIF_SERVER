package com.example.gifserverv2.domain.engagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_view_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectViewCount {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private long views;
}