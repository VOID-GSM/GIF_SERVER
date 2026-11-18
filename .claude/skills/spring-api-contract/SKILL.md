---
name: spring-api-contract
description: "GIF-server-v2 엔드포인트의 계약(경로, 메서드, 요청·응답 DTO, 상태 코드, 권한, 실패별 예외)을 구현 전에 설계하고 Swagger 어노테이션과 맞춘다. 엔드포인트를 추가·변경·리뷰할 때, 프론트엔드가 '이 API 뭐 돌려줘요?'라고 물을 때, 컨트롤러나 DTO를 쓰기 전에 사용한다."
---

# API Contract

이 프로젝트의 API 명세는 컨트롤러의 Swagger 어노테이션(`@Operation`, `@ApiResponses`, `@Schema`)이다. 계약을 먼저 문서로 정하고, 구현은 그 계약을 어노테이션과 코드에 그대로 옮긴다. 이렇게 하면 두 가지 사고를 막는다: Swagger에 적힌 상태 코드와 실제 응답이 다른 것, 아무도 설계하지 않은 실패 경로가 500이 되는 것.

`spring-java-conventions`를 먼저 읽는다.

## 계약 문서 형식

`_workspace/feature/01_contract.md`에 엔드포인트마다:

```markdown
### <METHOD> /api/<path> — <한 줄 목적>

**권한:** <public(permitAll) | authenticated | hasAnyRole(...) | 세부 권한(AdminRole/ClientRole/gradeHead) | 본인만>
**SecurityConfig:** <기존 규칙 중 어느 줄에 걸리는지 / 추가할 규칙과 위치>
**Controller:** `domain/<도메인>/controller/<X>Controller#<메서드>`
**Service:** `<X>Service.<메서드>(...)` — 트랜잭션: readOnly | 쓰기

**요청**
- Path: `<param>: <Type>`
- Query: `<param>: <Type>` (required=false면 기본값)
- Body: `<Prefix><Name>Request(field: Type, ...)` — 검증 어노테이션 포함

**성공**
- 상태: `200 OK` | `201 CREATED` | `204`(Void)
- 본문: `<Prefix><Name>Response` — 필드 전체, null이 되는 조건

**실패**
| 조건 | 던지는 예외 | 상태 | 메시지 |
|------|------------|------|--------|
| 대상 없음 | `XxxException.notFound()` | 404 | "..." |

**메모**
- 부수효과(알림, 코인 지급), 멱등성, 정렬, 스키마 영향
```

실패 표의 모든 행은 실제 예외(기존 정적 팩토리 또는 새로 추가할 것)를 적는다. 예외 이름 없이 "400 반환"은 계약이 아니다.

## 경로와 메서드

- 기본 경로는 `/api/<도메인>`. 기존 컨트롤러의 경로 체계를 따른다(`/api/project/{projectId}/...`, `/api/users/me/...`).
- GET 조회, POST 생성·행동, PATCH 부분 수정, DELETE 삭제. PUT은 전체 교체가 정말 필요할 때만(현재 5곳).
- 대상은 경로 변수로 드러낸다(`/api/assessment/road-making/rounds/{roundId}`).

## 상태 코드와 예외

`GlobalExceptionHandler`가 예외 타입으로 상태를 정한다:

| 던지는 것 | 상태 |
|-----------|------|
| `ResponseStatusException`(도메인 예외 포함) | 지정한 상태 |
| `IllegalArgumentException` | 400 |
| `@Valid` 실패, 본문 파싱 실패, 타입 불일치 | 400 |
| `AccessDeniedException` (또는 `@PreAuthorize`/SecurityConfig 거부) | 403 (`AccessDeniedCode`) |
| 인증 없음 | 401 |
| 그 외 모든 예외 (`IllegalStateException`, NPE, 외부 API 예외) | **500** |

- 없는 리소스, 다른 사용자의 리소스: **404**가 관례다(PR #273 라운드 상세). 존재 여부를 숨긴다.
- 외부 사용자에게 막힌 기능: 403 + `AccessDeniedCode.EXTERNAL_USER_FORBIDDEN`.
- 상태 충돌(이미 제출함 등): 409, 잘못된 입력: 400.
- 외부 API(OpenAI, GitHub, DataGSM) 실패: 502(응답 오류) / 503(연결 불가) (#267).

## 권한 표기

| 형태 | 구현 위치 |
|------|-----------|
| public | `SecurityConfig`에 permitAll 추가 — principal이 null일 수 있음을 계약에 적는다 |
| authenticated | `SecurityConfig`에 `.authenticated()` 규칙 (EXTERNAL 포함) |
| 재학생·관리자만 | 아무것도 안 하면 `anyRequest().hasAnyRole("USER", "ADMIN")` |
| 관리자만 | `@PreAuthorize("hasRole('ADMIN')")` |
| 세부 권한 | 서비스에서 `user.adminRole()`, `clientRole()`, `gradeHead()` 확인 |
| 본인만 | 서비스에서 로드 후 소유자 비교 → 404 |

**새 경로가 기존 와일드카드에 걸리는지 반드시 확인한다.** `SecurityConfig`는 위에서부터 처음 맞는 규칙을 쓴다. 예: 새 `GET /api/project/{id}/secret`은 `GET /api/project/*`가 아니라 `/*/secret`이라 안 걸리지만, 새 `GET /api/project/drafts`는 `GET /api/project/*`(authenticated)에 걸려 외부 사용자에게 열린다.

## Swagger 반영

계약 → 어노테이션 매핑:
- 목적 → `@Operation(summary)`, 권한·null 조건·부수효과 → `@Operation(description)`
- 성공 + 실패 표의 모든 상태 → `@ApiResponses({@ApiResponse(responseCode = "...", description = "...")})`
- DTO 필드 → `@Schema(description, example)`

## 넘겨주기

계약은 세 에이전트의 입력이다: domain-engineer는 실패 표로 가드를 쓰고, web-engineer는 DTO·컨트롤러·Swagger를 쓰고, qa-inspector는 실패 표를 체크리스트로 쓴다. 실패 조건의 올바른 동작이 제품 결정이면(두 번 제출하면 409인가 덮어쓰기인가) 두 안을 메모에 쓰고 묻는다. 임의로 고르지 않는다.
