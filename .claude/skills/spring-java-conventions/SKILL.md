---
name: spring-java-conventions
description: "GIF-server-v2(Spring Boot 3.4 / Java 21) 코드 작성·리뷰 기준. 패키지 구조, Entity·DTO·Controller·Service·Repository·예외·Swagger 작성법과 기계 검사 스크립트, 위반 판정 경계를 담는다. 이 레포의 Java 파일을 쓰거나 리뷰하기 전에 항상 읽는다."
---

# Spring Java Conventions

규칙 원문은 루트 `CLAUDE.md`("계층 역할", "코드 컨벤션", "이 코드베이스의 함정")가 기준이다. 이 스킬은 원문을 다시 적지 않고, **실제 코드에서 어떻게 쓰는지**와 **위반 판정 경계**만 담는다.

## 1. 계층별 작성법 (기존 코드 기준)

### Entity
- `@Entity` + `@Table(name = "snake_case")` + `@Getter` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)`. 생성은 `@Builder` 또는 정적 팩토리.
- `@Setter` 금지. 상태 변경은 이름 있는 메서드로: `RoadMakingRound.complete(now)`, `abandon(now)`. 상태와 짝이 되는 시각을 같이 바꾼다.
- `@ManyToOne(fetch = FetchType.LAZY)`가 기본(레포 17곳 모두 LAZY). EAGER로 바꾸지 않는다.
- enum은 `@Enumerated(EnumType.STRING)`.
- 엔티티 불변식은 엔티티 안에서 지키되, 사용자 입력으로 깨질 수 있는 조건은 서비스가 먼저 검사해 4xx로 응답한다. (`CoinService.spend`가 잔액을 먼저 확인해 400, `CoinAccount.deductCoin`의 `IllegalStateException`은 최후 방어선)

### DTO
- `record`가 기본(124개). 요청은 `dto/request/`, 응답은 `dto/response/`.
- 응답은 `public static XxxResponse from(Entity)` 팩토리로 만든다. 엔티티를 컨트롤러 밖으로 내보내지 않는다.
- 필드마다 `@Schema(description, example)`를 단다.
- 요청 검증은 `jakarta.validation` 어노테이션(`@NotNull`, `@Size` 등) + 컨트롤러의 `@Valid`. 검증 실패는 `GlobalExceptionHandler`가 400으로 바꾼다.
- 응답 필드가 null일 수 있는 상태가 하나라도 있으면 `@Schema` 설명에 언제 null인지 적는다.

### Controller
- `@RestController` + `@RequestMapping("/api/<도메인>")` + `@Tag`. 의존성은 `@RequiredArgsConstructor`.
- 메서드마다 `@Operation(summary, description)` + `@ApiResponses`로 **실제로 나올 수 있는 모든 상태 코드**를 적는다. Swagger가 이 프로젝트의 API 명세다.
- 반환은 `ResponseEntity<T>`. 생성은 `ResponseEntity.status(HttpStatus.CREATED)`, 본문 없음은 `ResponseEntity<Void>`.
- 사용자 정보는 `@AuthenticationPrincipal AuthenticatedUser user`. permitAll 경로에서는 null일 수 있다(`ProjectController.filterProjects`).
- service 호출과 응답 변환만 한다.

### Service
- `@Service` + `@RequiredArgsConstructor`. **트랜잭션은 메서드 단위**로 단다(클래스 단위는 60개 중 2개뿐).
  - 조회: `@Transactional(readOnly = true)`, 쓰기: `@Transactional`
- 순서: 가드(존재·권한·상태 확인) → 로드 → 변경 → 이벤트/알림 → DTO 반환.
- 로드한 엔티티 변경은 dirty checking으로 저장된다. `save()`는 새 엔티티에만.
- HTTP 객체(`HttpServletRequest`, `ResponseEntity`)를 받거나 반환하지 않는다.

### Repository
- 파생 쿼리 이름 우선, 읽기 어려우면 `@Query`(JPQL). 목록 조회는 정렬을 명시한다(`OrderBy...` 또는 `ORDER BY`).
- 동시에 바뀔 수 있는 값(코인, 라운드 상태)은 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 조회 메서드를 쓴다.

### 예외
- 도메인별 `XxxException extends ResponseStatusException` + 정적 팩토리(`FormException.notFound()`). 도메인 예외가 없는 도메인은 `new ResponseStatusException(HttpStatus.X, "메시지")`.
- 메시지는 한국어, 사용자에게 그대로 보인다.

## 2. 기계 검사

```bash
bash .claude/scripts/convention-check.sh <BASE> [HEAD]
```

- HEAD를 비우면 작업 트리(커밋 안 된 변경, 새 파일 포함). 추가된 줄과 새 파일만 본다.
- exit 0: 위반 없음, exit 1: 위반 있음(`path:line: [규칙] 설명`), exit 2: git 오류(ref 확인).
- 검사 항목: service의 `jakarta.servlet` import, controller의 repository import, `@Table` 누락·스네이크 표기법, 새 Entity·Repository 접미어, 새 DTO 접두어. 접두어·접미어 목록은 `CLAUDE.md`의 "허용 접두어:", "허용 Entity 접미어:", "허용 Repository 접미어:" 줄에서 읽는다.

## 3. 위반 판정 경계

| 규칙 | 위반으로 보는 경우 | 위반 아님 (오탐 방지) |
| --- | --- | --- |
| [계층] controller | 비즈니스 규칙 판단용 분기, 계산, 여러 service 결과 조합 | `@Valid`, 응답 변환, permitAll 경로의 `user == null ? null : user.userId()` |
| [계층] service | `HttpServletRequest/Response` 파라미터, `ResponseEntity` 반환 | 예외용 `HttpStatus`/`ResponseStatusException`, 외부 API용 `RestTemplate`/`RestClient`와 그 응답 |
| [계층] repository | default 메서드 안의 분기·계산 | 쿼리 메서드, `@Query`, `@Lock` |
| [주석] | 코드를 그대로 다시 말하는 주석, 주석 처리된 코드, 디버그 흔적 | 왜 그렇게 했는지 설명하는 주석 (`SecurityConfig`의 순서 주석처럼) |
| [예외] | 해당 도메인에 `XxxException`이 있는데 직접 `new ResponseStatusException(...)` | 도메인 예외 클래스가 없는 도메인 |
| [네이밍] | 기계 검사가 놓친 새 DTO/Entity/Repository 이름 | 기존 클래스 이름 |

같은 도메인의 기존 코드가 같은 방식이면 팀이 받아들인 패턴일 수 있으므로 위반이 아니라 질문으로 다룬다. 기존 코드의 위반은 이번 변경이 그것을 더 퍼뜨릴 때만 지적한다.
