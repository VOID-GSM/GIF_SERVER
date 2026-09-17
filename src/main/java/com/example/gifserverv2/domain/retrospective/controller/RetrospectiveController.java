package com.example.gifserverv2.domain.retrospective.controller;

import com.example.gifserverv2.domain.retrospective.dto.request.CreateRetrospectiveRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectivePatchRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectiveSort;
import com.example.gifserverv2.domain.retrospective.dto.response.BookmarkToggleResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.DetailRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.ListRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.service.RetrospectiveService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retrospective")
@RequiredArgsConstructor
public class RetrospectiveController {

    private final RetrospectiveService retrospectiveService;

    @GetMapping
    public ResponseEntity<Page<ListRetrospectiveResponse>> getList(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) RetrospectiveCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RetrospectiveSort sort,
            Pageable pageable
    ) {
        return ResponseEntity.ok(retrospectiveService.getList(user.userId(), category, keyword, sort, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailRetrospectiveResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(retrospectiveService.getDetail(user.userId(), id));
    }

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateRetrospectiveRequest request
    ) {
        return ResponseEntity.ok(retrospectiveService.create(user.userId(), request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody RetrospectivePatchRequest request
    ) {
        retrospectiveService.update(user.userId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        retrospectiveService.delete(user.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(retrospectiveService.toggleBookmark(user.userId(), id));
    }

    @PostMapping("/draft")
    public ResponseEntity<Long> createDraft(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectivePatchRequest request
    ) {
        return ResponseEntity.ok(retrospectiveService.createDraft(user.userId(), request));
    }

    @PatchMapping("/draft/{id}")
    public ResponseEntity<Void> updateDraft(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody RetrospectivePatchRequest request
    ) {
        retrospectiveService.updateDraft(user.userId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/draft")
    public ResponseEntity<List<ListRetrospectiveResponse>> getMyDrafts(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(retrospectiveService.getMyDrafts(user.userId()));
    }

    @DeleteMapping("/draft/{id}")
    public ResponseEntity<Void> deleteDraft(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        retrospectiveService.deleteDraft(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
