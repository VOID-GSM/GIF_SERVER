package com.example.gifserverv2.domain.student.dto.response;

public record UnregisteredStudentResponse(
        String studentNumber,
        String name,
        Integer grade,
        Integer classNo,
        Integer studentNo
) {
}
