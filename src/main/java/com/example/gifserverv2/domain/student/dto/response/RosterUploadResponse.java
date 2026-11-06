package com.example.gifserverv2.domain.student.dto.response;

import java.util.List;

public record RosterUploadResponse(
        int created,
        int updated,
        List<RowError> errors
) {
    public record RowError(int line, String reason) {
    }
}
