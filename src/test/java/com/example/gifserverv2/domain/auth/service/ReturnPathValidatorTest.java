package com.example.gifserverv2.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnPathValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/projects/12",
            "/projects/12?tab=comments",
            "/retrospectives?sort=latest#top",
            "/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8"
    })
    void 같은_사이트의_상대_경로는_허용한다(String returnTo) {
        assertThat(ReturnPathValidator.sanitize(returnTo)).isEqualTo(returnTo);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "https://evil.com",
            "http://evil.com/projects/1",
            "javascript:alert(1)",
            "evil.com/projects",
            "projects/12",
            "//evil.com",
            "//evil.com/projects",
            "/\\evil.com",
            "\\\\evil.com",
            "/ /evil.com",
            "/projects\n/evil",
            "/projects\t1"
    })
    void 외부_도메인이나_우회_시도는_차단한다(String returnTo) {
        assertThat(ReturnPathValidator.sanitize(returnTo)).isNull();
    }

    @Test
    void 너무_긴_경로는_차단한다() {
        assertThat(ReturnPathValidator.sanitize("/" + "a".repeat(2048))).isNull();
    }
}
