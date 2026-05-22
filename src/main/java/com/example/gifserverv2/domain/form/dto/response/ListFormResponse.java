package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;

import java.time.LocalDate;

public record ListFormResponse(
        Long id,
        String title,
        LocalDate deadline,
        boolean announced,
        boolean submitted
) {
    public static ListFormResponse from(Form form) {
        return new ListFormResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), false);
    }

    public static ListFormResponse from(Form form, boolean submitted) {
        return new ListFormResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), submitted);
    }
}