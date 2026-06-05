package com.example.gifserverv2.domain.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private LocalDate deadline;

    @Column(nullable = false)
    private boolean announced;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<FormField> fields = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, LocalDate deadline, List<FormField> newFields) {
        this.title = title;
        this.deadline = deadline;
        this.fields.clear();
        if (newFields != null) {
            this.fields.addAll(newFields);
        }
    }

    public void announce() {
        this.announced = true;
    }

    public boolean isDeadlinePassed() {
        return LocalDate.now().isAfter(this.deadline);
    }
}