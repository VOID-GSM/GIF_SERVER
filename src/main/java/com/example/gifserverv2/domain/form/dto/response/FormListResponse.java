package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;

import java.time.LocalDate;

public record FormListResponse(
        Long id,
        String title,
        LocalDate deadline,
        boolean announced,
        boolean submitted
) {
    public static FormListResponse from(Form form) {
        return new FormListResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), false);
    }

    public static FormListResponse from(Form form, boolean submitted) {
        return new FormListResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), submitted);
    }
}