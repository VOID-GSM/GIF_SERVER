package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ListFormResponse(
        Long id,
        String title,
        LocalDateTime deadline,
        boolean announced,
        boolean submitted,
        Boolean deadlineComplied,
        Integer targetGrade
) {
    public static ListFormResponse from(Form form) {
        return new ListFormResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), false, null, form.getTargetGrade());
    }

    public static ListFormResponse from(Form form, boolean submitted, Boolean deadlineComplied) {
        return new ListFormResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), submitted, deadlineComplied, form.getTargetGrade());
    }
}