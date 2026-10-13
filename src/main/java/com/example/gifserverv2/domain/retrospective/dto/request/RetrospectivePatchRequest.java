package com.example.gifserverv2.domain.retrospective.dto.request;

import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RetrospectivePatchRequest(
        RetrospectiveCategory category,
        RetrospectiveVisibility visibility,

        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Size(max = 20000, message = "내용은 20000자를 초과할 수 없습니다.")
        String content,

        @Valid
        List<RelatedWorkRequest> relatedWorks,

        @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
        List<@NotBlank(message = "태그는 비어 있을 수 없습니다.")
        @Size(max = 30, message = "태그는 30자를 초과할 수 없습니다.") String> tags
) {}