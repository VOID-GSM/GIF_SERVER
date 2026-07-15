package com.example.gifserverv2.domain.notice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Long createdByUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "notice_target_grade", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "grade")
    @org.hibernate.annotations.BatchSize(size = 100)
    @Builder.Default
    private List<Integer> targetGrades = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "notice_target_project", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "project_id")
    @org.hibernate.annotations.BatchSize(size = 100)
    @Builder.Default
    private List<Long> targetProjectIds = new ArrayList<>();
}
