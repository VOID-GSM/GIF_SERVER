package com.example.gifserverv2.domain.form.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "form_field")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType type;

    @Column(nullable = false)
    private int orderIndex;

    public enum FieldType {
        TEXT,
        FILE,
        CALENDAR
    }
}