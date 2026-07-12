package com.example.gifserverv2.domain.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "form")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private boolean announced;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private Integer targetGrade;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<FormField> fields = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, String description, LocalDateTime deadline, Integer targetGrade, List<FormField> newFields) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.targetGrade = targetGrade;
        this.fields.clear();
        if (newFields != null) {
            this.fields.addAll(newFields);
        }
    }

    public void announce() {
        this.announced = true;
    }

    public boolean isDeadlinePassed() {
        return LocalDateTime.now().isAfter(this.deadline);
    }
}