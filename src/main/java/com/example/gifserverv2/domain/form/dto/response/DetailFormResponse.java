package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;

import java.time.LocalDate;
import java.util.List;

public record DetailFormResponse(
        Long id,
        String title,
        LocalDate deadline,
        boolean announced,
        List<FieldResponse> fields
) {
    public record FieldResponse(
            Long id,
            String title,
            String description,
            String type,
            int orderIndex
    ) {}

    public static DetailFormResponse from(Form form) {
        List<FieldResponse> fieldResponses = form.getFields().stream()
                .map(f -> new FieldResponse(f.getId(), f.getTitle(), f.getDescription(), f.getType().name(), f.getOrderIndex()))
                .toList();

        return new DetailFormResponse(
                form.getId(),
                form.getTitle(),
                form.getDeadline(),
                form.isAnnounced(),
                fieldResponses
        );
    }
}
