package com.example.gifserverv2.domain.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "form_field_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FormFieldAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_submit_id", nullable = false)
    private FormSubmit formSubmit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_field_id", nullable = false)
    private FormField formField;

    @Column(columnDefinition = "TEXT")
    private String textAnswer;

    @Column
    private String filePath;

    @Column
    private LocalDate dateAnswer;

    @Column
    private Long fileSize;

    @Column
    private String eventName;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(length = 7)
    private String color;

}
