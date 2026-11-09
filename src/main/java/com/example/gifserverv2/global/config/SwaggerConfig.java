package com.example.gifserverv2.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    private static final String API_DESCRIPTION = """
            광주소프트웨어마이스터고 아이디어 페스티벌(GIF) 운영 플랫폼 API.

            ## 인증
            - 로그인(`/api/auth/**`) 응답의 `accessToken`을 `Authorization: Bearer {accessToken}` 헤더로 보낸다. 쿠키는 사용하지 않는다.
            - 추가 정보 입력·회원 정보 수정처럼 토큰 내용이 바뀌는 API는 새 `accessToken`을 돌려주므로 그 값으로 교체해야 한다.
            - 토큰이 없거나 만료·위조되면 **401** (본문 없음)을 응답한다.
            - 오른쪽 위 **Authorize** 버튼에 토큰만 넣으면(`Bearer ` 접두어 제외) Swagger에서 바로 호출할 수 있다.

            ## 사용자 구분
            | 구분 | 값 | 설명 |
            |---|---|---|
            | 학생 | `role=USER` | DataGSM 학생 계정. `clientRole` = `LEADER`(팀장) / `MEMBER`(팀원) |
            | 선생님 | `role=ADMIN` | DataGSM 선생님 계정. `adminRole`로 세부 역할을 나눈다 |
            | 외부 사용자 | `role=EXTERNAL` | Google 로그인 사용자. 프로젝트 조회·좋아요·댓글, 알림 등 일부 API만 사용할 수 있다 |

            `adminRole`
            - `MASTER`: 아이디어 페스티벌 담당 선생님. 양식·평가 기준·평가 기간·교사 배정 등 운영 전반을 관리한다.
            - `MAJOR_TEACHER`: 전공 교과 선생님. 전공 점수를 채점한다.
            - `GENERAL_TEACHER`: 일반 교과 선생님. 사회 점수를 채점한다.
            - `VOID`: 서비스 운영(개발)팀 계정. 학생 명단·문의 처리 등 운영 기능을 사용한다.
            - `gradeHead=true`: 학년부장. 보고서 점수를 채점한다.

            별도 표기가 없는 API는 **학생·선생님만** 호출할 수 있고, 외부 사용자가 호출하면 403(`EXTERNAL_USER_FORBIDDEN`)이다.

            ## 응답 형식
            - 성공 응답은 감싸지 않고 각 API의 응답 스키마 그대로 내려간다.
            - 에러 응답은 아래 형태로 통일된다. 403은 `code`(`EXTERNAL_USER_FORBIDDEN` / `ACCESS_DENIED`)가 추가될 수 있다.
            ```json
            {
              "timestamp": "2026-11-09T01:23:45.678Z",
              "status": 400,
              "error": "Bad Request",
              "message": "요청 값이 올바르지 않습니다.",
              "path": "/api/project"
            }
            ```
            - 날짜·시간은 한국 시간(KST) 기준 `yyyy-MM-ddTHH:mm:ss` 형식이다. 요청에서는 `yyyy-MM-dd`만 보내도 되며, 이 경우 그날 23:59:59로 처리된다.
            """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GIF v2 API")
                        .description(API_DESCRIPTION)
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 응답의 accessToken")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("gif-v2-apis")
                .pathsToMatch("/**")
                .packagesToScan("com.example.gifserverv2")
                .build();
    }
}
