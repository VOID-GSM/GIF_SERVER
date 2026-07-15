package com.example.gifserverv2.domain.notice.controller;

import com.example.gifserverv2.domain.notice.dto.request.CreateNoticeRequest;
import com.example.gifserverv2.domain.notice.dto.response.DetailNoticeResponse;
import com.example.gifserverv2.domain.notice.dto.response.ListNoticeResponse;
import com.example.gifserverv2.domain.notice.service.NoticeService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createNotice(
            @AuthenticationPrincipal AuthenticatedUser user,
            @jakarta.validation.Valid @RequestBody CreateNoticeRequest request
    ) {
        return ResponseEntity.ok(noticeService.createNotice(user.userId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ListNoticeResponse>> getAllNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<DetailNoticeResponse> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}
