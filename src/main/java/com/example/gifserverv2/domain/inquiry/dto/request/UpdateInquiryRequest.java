package com.example.gifserverv2.domain.inquiry.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdateInquiryRequest(String title, String content, MultipartFile file) {}