package com.example.gifserverv2.domain.inquiry.controller;

import com.example.gifserverv2.domain.inquiry.dto.request.AnswerInquiryRequest;
import com.example.gifserverv2.domain.inquiry.dto.request.UpdateInquiryRequest;
import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.service.AdminInquiryService;
import com.example.gifserverv2.domain.inquiry.service.ClientInquiryService;
import com.example.gifserverv2.domain.inquiry.dto.request.CreateInquiryRequest;
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
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final ClientInquiryService clientInquiryService;
    private final AdminInquiryService adminInquiryService;

    @PostMapping
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute CreateInquiryRequest request
    ) {
        return ResponseEntity.ok(clientInquiryService.createInquiry(
                user.userId(),
                request.title(),
                request.content(),
                request.file()
        ));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ListInquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiries(user.userId(), user.name()));
    }

    @GetMapping("/my/{inquiryId}")
    public ResponseEntity<DetailInquiryResponse> getMyInquiryDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiryDetail(user.userId(), user.name(), inquiryId));
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<ListInquiryResponse>> getAllInquiries(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminInquiryService.getAllInquiries(user.email(), pageable));
    }

    @GetMapping("/admin/{inquiryId}")
    public ResponseEntity<DetailInquiryResponse> getInquiryDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(adminInquiryService.getInquiryDetail(user.email(), inquiryId));
    }

    @PatchMapping("/admin/{inquiryId}/answer")
    public ResponseEntity<Void> answerInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long inquiryId,
            @Valid @RequestBody AnswerInquiryRequest request
    ) {
        adminInquiryService.answerInquiry(user.email(), inquiryId, request.answerContent());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/my/{inquiryId}")
    public ResponseEntity<Void> updateInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long inquiryId,
            @Valid @ModelAttribute UpdateInquiryRequest request
    ) {
        clientInquiryService.updateInquiry(
                user.userId(),
                inquiryId,
                request.title(),
                request.content(),
                request.file()
        );
        return ResponseEntity.noContent().build();
    }
}