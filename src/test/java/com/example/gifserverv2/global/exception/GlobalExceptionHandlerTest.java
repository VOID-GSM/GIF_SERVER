package com.example.gifserverv2.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/assessment/cat-chase/ranking");

    @Test
    void 없는_경로는_500이_아니라_404로_응답한다() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(
                new NoResourceFoundException(HttpMethod.GET, "api/assessment/cat-chase/ranking"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
    }

    @Test
    void 지원하지_않는_메서드는_405로_응답한다() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(
                new HttpRequestMethodNotSupportedException("DELETE"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void 파라미터_형식이_틀리면_400으로_응답한다() {
        ResponseEntity<Map<String, Object>> response = handler.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("abc", int.class, "questionCount", null, null), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 예상하지_못한_예외는_500으로_응답한다() {
        ResponseEntity<Map<String, Object>> response = handler.handleUnexpected(new IllegalStateException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
