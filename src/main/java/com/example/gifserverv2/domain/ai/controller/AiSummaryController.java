package com.example.gifserverv2.domain.ai.controller;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    @GetMapping("/project/{projectId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> summarizeProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(aiSummaryService.summarizeProject(projectId));
    }

    @GetMapping("/form/submit/{submitId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> summarizeFormSubmit(@PathVariable Long submitId) {
        return ResponseEntity.ok(aiSummaryService.summarizeFormSubmit(submitId));
    }
}