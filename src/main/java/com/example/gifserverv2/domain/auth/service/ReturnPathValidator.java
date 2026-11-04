package com.example.gifserverv2.domain.auth.service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 로그인 후 복귀 경로(returnTo) 검증.
 * Open Redirect 방지를 위해 같은 사이트 안의 상대 경로("/"로 시작)만 허용한다.
 */
public final class ReturnPathValidator {

    private static final int MAX_LENGTH = 2048;

    private ReturnPathValidator() {
    }

    /**
     * 허용되는 복귀 경로면 그대로, 아니면 null 을 반환한다.
     */
    public static String sanitize(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || returnTo.length() > MAX_LENGTH) {
            return null;
        }
        // "/"로 시작해야 하고, "//evil.com" 처럼 프로토콜 상대 URL 은 외부 도메인이므로 차단
        if (!returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return null;
        }
        // 브라우저는 "\"를 "/"로 해석하므로 "/\evil.com" 같은 우회를 막는다. 공백·제어 문자도 차단
        for (int i = 0; i < returnTo.length(); i++) {
            char c = returnTo.charAt(i);
            if (c == '\\' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                return null;
            }
        }
        try {
            URI uri = new URI(returnTo);
            if (uri.getScheme() != null || uri.getRawAuthority() != null) {
                return null;
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return returnTo;
    }
}
