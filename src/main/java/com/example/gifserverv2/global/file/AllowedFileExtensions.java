package com.example.gifserverv2.global.file;

import java.util.Set;

public final class AllowedFileExtensions {

    public static final Set<String> ALL = Set.of(
            "pdf", "ppt", "pptx", "doc", "docx", "hwp", "hwpx", "xls", "xlsx", "txt", "zip",
            "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp",
            "mp4", "mov", "avi", "mkv",
            "mp3", "wav"
    );

    /**
     * 실제 파일 확장자가 아니라, 해당 FILE 타입 항목이 파일 업로드 대신(또는 함께)
     * 외부 링크(캔바/미리캔버스/구글 드라이브 등) 제출을 허용한다는 것을 나타내는 값.
     * allowedExtensions 목록에 다른 실제 확장자와 함께 포함될 수 있다.
     */
    public static final String LINK = "url";

    public static boolean isSupported(String extension) {
        return ALL.contains(extension) || LINK.equals(extension);
    }

    private AllowedFileExtensions() {
    }
}