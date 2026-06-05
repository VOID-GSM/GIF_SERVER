package com.example.gifserverv2.global.file;


import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String save(MultipartFile file, String directory);

    void delete(String filePath);
}
