package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;

import java.time.LocalDate;
import java.util.List;

public record FormDetailResponse(
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

    public static FormDetailResponse from(Form form) {
        List<FieldResponse> fieldResponses = form.getFields().stream()
                .map(f -> new FieldResponse(f.getId(), f.getTitle(), f.getDescription(), f.getType().name(), f.getOrderIndex()))
                .toList();

        return new FormDetailResponse(
                form.getId(),
                form.getTitle(),
                form.getDeadline(),
                form.isAnnounced(),
                fieldResponses
        );
    }
}
