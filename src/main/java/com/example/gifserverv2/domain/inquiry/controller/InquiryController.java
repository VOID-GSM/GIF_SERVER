package com.example.gifserverv2.domain.inquiry.controller;

import com.example.gifserverv2.domain.inquiry.dto.request.AnswerInquiryRequest;
import com.example.gifserverv2.domain.inquiry.dto.request.CreateInquiryRequest;
import com.example.gifserverv2.domain.inquiry.dto.response.InquiryDetailResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.InquiryListResponse;
import com.example.gifserverv2.domain.inquiry.service.AdminInquiryService;
import com.example.gifserverv2.domain.inquiry.service.ClientInquiryService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final ClientInquiryService clientInquiryService;
    private final AdminInquiryService adminInquiryService;

    @PostMapping
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ModelAttribute @Valid CreateInquiryRequest request
    ) {
        return ResponseEntity.ok(clientInquiryService.createInquiry(user.userId(), request.title(), request.content(), request.file()));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<InquiryListResponse>> getMyInquiries(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiries(user.userId(), pageable));
    }

    @GetMapping("/my/{inquiryId}")
    public ResponseEntity<InquiryDetailResponse> getMyInquiryDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiryDetail(user.userId(), inquiryId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<InquiryListResponse>> getAllInquiries(Pageable pageable) {
        return ResponseEntity.ok(adminInquiryService.getAllInquiries(pageable));
    }

    @GetMapping("/admin/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(@PathVariable Long inquiryId) {
        return ResponseEntity.ok(adminInquiryService.getInquiryDetail(inquiryId));
    }

    @PatchMapping("/admin/{inquiryId}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> answerInquiry(
            @PathVariable Long inquiryId,
            @RequestBody @Valid AnswerInquiryRequest request
    ) {
        adminInquiryService.answerInquiry(inquiryId, request.answerContent());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long inquiryId) {
        adminInquiryService.deleteInquiry(inquiryId);
        return ResponseEntity.noContent().build();
    }
}