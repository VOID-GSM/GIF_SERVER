package com.example.gifserverv2.domain.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @Column(length = 200)
    private String allowedExtensions;

    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean required;

    public enum FieldType {
        TEXT,
        FILE,
        CALENDAR
    }

    public List<String> getAllowedExtensionList() {
        if (allowedExtensions == null || allowedExtensions.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public void update(String title, String description, FieldType type, int orderIndex,
                        String allowedExtensions, boolean required) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.orderIndex = orderIndex;
        this.allowedExtensions = allowedExtensions;
        this.required = required;
    }

    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        String normalized = extension.trim().toLowerCase();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        List<String> allowed = getAllowedExtensionList();
        return allowed.isEmpty() || allowed.contains(normalized);
    }
}