package com.example.gifserverv2.domain.inquiry.controller;

import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.service.ClientInquiryService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final ClientInquiryService clientInquiryService;

    @PostMapping
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(clientInquiryService.createInquiry(user.userId(), title, content, file));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ListInquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiries(user.userId()));
    }

    @GetMapping("/my/{inquiryId}")
    public ResponseEntity<DetailInquiryResponse> getMyInquiryDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiryDetail(user.userId(), inquiryId));
    }
}