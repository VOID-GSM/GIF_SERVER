package com.example.gifserverv2.domain.inquiry.controller;

import com.example.gifserverv2.domain.inquiry.service.ClientInquiryService;
import com.example.gifserverv2.domain.inquiry.dto.request.CreateInquiryRequest;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final ClientInquiryService clientInquiryService;

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
}