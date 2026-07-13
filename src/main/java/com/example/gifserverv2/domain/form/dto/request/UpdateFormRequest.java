package com.example.gifserverv2.domain.form.dto.request;

import com.example.gifserverv2.domain.form.entity.FormField;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateFormRequest(
        String title,
        String description,
        LocalDateTime deadline,
        Integer targetGrade,
        List<FieldRequest> fields
) {
    public record FieldRequest(
            String title,
            String description,
            FormField.FieldType type,
            int orderIndex,
            List<String> allowedExtensions
    ) { }
}