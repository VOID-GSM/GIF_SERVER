package com.example.gifserverv2.domain.ai.dto.response;

import java.util.List;

public record RetrospectiveStructureResponse(
        List<String> sections,
        String content
) {}
