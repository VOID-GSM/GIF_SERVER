package com.example.gifserverv2.global.file;

import java.util.Set;

public final class AllowedFileExtensions {

    public static final Set<String> ALL = Set.of(
            "pdf", "ppt", "pptx", "doc", "docx", "hwp", "hwpx", "xls", "xlsx", "txt", "zip",
            "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp",
            "mp4", "mov", "avi", "mkv",
            "mp3", "wav"
    );

    private AllowedFileExtensions() {
    }
}