package com.example.gifserverv2.domain.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "form_submit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FormSubmit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long submittedByUserId;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "formSubmit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FormFieldAnswer> answers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
    }
}
