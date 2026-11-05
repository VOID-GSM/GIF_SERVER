package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.service.ScoreCriteriaService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(ScoreCriteriaControllerTest.MethodSecurityConfig.class)
class ScoreCriteriaControllerTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
        @Bean
        ScoreCriteriaService scoreCriteriaService() {
            return Mockito.mock(ScoreCriteriaService.class);
        }

        @Bean
        ScoreCriteriaController scoreCriteriaController(ScoreCriteriaService service) {
            return new ScoreCriteriaController(service);
        }
    }

    @Autowired
    private ScoreCriteriaController controller;

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_영역을_조회할_수_있다() {
        assertThat(controller.getAreas().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = "USER")
    void 학생은_접근할_수_없다() {
        assertThatThrownBy(() -> controller.getAreas()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.deleteArea(null, 1L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void 영역_요청_값을_검증한다() {
        assertThat(VALIDATOR.validate(new CreateScoreAreaRequest("전공", null))).isEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreAreaRequest(" ", null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreAreaRequest(null, null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreAreaRequest("a".repeat(101), null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreAreaRequest("전공", -1))).isNotEmpty();

        assertThat(VALIDATOR.validate(new PatchScoreAreaRequest(null, null))).isEmpty();
        assertThat(VALIDATOR.validate(new PatchScoreAreaRequest(" ", null))).isNotEmpty();
    }

    @Test
    void 항목_요청_값을_검증한다() {
        assertThat(VALIDATOR.validate(new CreateScoreItemRequest("발표", null, 20, null))).isEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreItemRequest("발표", null, null, null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreItemRequest("발표", null, 0, null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreItemRequest("발표", null, 1001, null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateScoreItemRequest("발표", "a".repeat(1001), 20, null))).isNotEmpty();

        assertThat(VALIDATOR.validate(new PatchScoreItemRequest(null, null, null, null))).isEmpty();
        assertThat(VALIDATOR.validate(new PatchScoreItemRequest(" ", null, null, null))).isNotEmpty();
        assertThat(VALIDATOR.validate(new PatchScoreItemRequest(null, null, 0, null))).isNotEmpty();
    }
}
