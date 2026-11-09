package com.example.gifserverv2.domain.ai.controller;

import com.example.gifserverv2.domain.ai.dto.request.RetrospectiveAiRequest;
import com.example.gifserverv2.domain.ai.dto.response.RetrospectiveAiResponse;
import com.example.gifserverv2.domain.ai.dto.response.RetrospectiveStructureResponse;
import com.example.gifserverv2.domain.ai.service.RetrospectiveAiService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/retrospective")
@RequiredArgsConstructor
public class RetrospectiveAiController {

    private final RetrospectiveAiService retrospectiveAiService;

    @PostMapping("/polish")
    public ResponseEntity<RetrospectiveAiResponse> polish(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectiveAiRequest request
    ) {
        return ResponseEntity.ok(retrospectiveAiService.polish(user.userId(), request));
    }

    @PostMapping("/structure")
    public ResponseEntity<RetrospectiveStructureResponse> recommendStructure(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectiveAiRequest request
    ) {
        return ResponseEntity.ok(retrospectiveAiService.recommendStructure(user.userId(), request));
    }

    @PostMapping("/portfolio")
    public ResponseEntity<RetrospectiveAiResponse> toPortfolio(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectiveAiRequest request
    ) {
        return ResponseEntity.ok(retrospectiveAiService.toPortfolio(user.userId(), request));
    }
}
