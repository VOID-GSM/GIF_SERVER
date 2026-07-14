package com.example.gifserverv2.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(length = 1000)
    private String filePath;

    @Column(length = 500)
    private String originalFileName;

    @Column
    private Long fileSize;

    @Column(nullable = false)
    private Long createdByUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @Column(length = 2000)
    private String answerContent;

    @Column
    private LocalDateTime answeredAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = InquiryStatus.PENDING;
    }

    public void answer(String answerContent) {
        this.answerContent = answerContent;
        this.answeredAt = LocalDateTime.now();
        this.status = InquiryStatus.ANSWERED;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateFile(String filePath, String originalFileName, Long fileSize) {
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
    }
}