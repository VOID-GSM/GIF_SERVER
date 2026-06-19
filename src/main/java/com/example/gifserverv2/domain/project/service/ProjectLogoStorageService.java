package com.example.gifserverv2.domain.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProjectLogoStorageService {

    String save(MultipartFile file);

    void delete(String logoUrl);
}