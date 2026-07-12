package com.example.gifserverv2.domain.inquiry.controller;
import com.example.gifserverv2.domain.inquiry.service.AdminInquiryService;
import com.example.gifserverv2.domain.inquiry.service.ClientInquiryService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final ClientInquiryService clientInquiryService;
    private final AdminInquiryService adminInquiryService;

    @PostMapping
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(clientInquiryService.createInquiry(user.userId(), title, content, file));
    }
}