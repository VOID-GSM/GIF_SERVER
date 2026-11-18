# GIF-server-v2 리뷰 체크리스트

실제로 장애를 내는 빈도 순. 괄호는 근거가 된 기존 코드.

## [필수] 후보

1. **쓰기 메서드의 `@Transactional` 누락.** 클래스 단위 트랜잭션이 없어(60개 서비스 중 2개만) 빠지면 dirty checking 변경이 조용히 사라진다.
2. **SecurityConfig 순서.** 새 경로가 위쪽 와일드카드(`GET /api/project/*`, `/api/items/**`, `/api/assessment/**`)에 먼저 걸려 의도와 다른 권한이 된다. 외부 사용자(EXTERNAL)에게 새로 열리는지, ADMIN이 막히는지(역할 계층 없음).
3. **permitAll 경로의 null principal.** `user.userId()`를 null 확인 없이 호출 → 비로그인 500. (`ProjectController.filterProjects`)
4. **500이 되는 예외.** `IllegalStateException`, `Optional.get()`, NPE, 잡지 않은 외부 API 예외는 `GlobalExceptionHandler`에서 500. 도메인 예외·`ResponseStatusException`으로. 엔티티 불변식은 서비스에서 먼저 검사(`CoinService.spend`).
5. **본인 확인 누락.** ID로 다른 사용자 리소스를 조회할 수 있다. 본인 것이 아니면 404(PR #273).
6. **세부 권한 누락.** 관리자 기능의 `AdminRole`(MASTER 등), 팀장 기능의 `ClientRole.LEADER` 확인이 서비스에 없다.
7. **스키마 미반영.** 새 필드·엔티티가 `SchemaPatchRunner`의 `REQUIRED_COLUMNS`/`REQUIRED_TABLES`에 없다. NOT NULL에 DEFAULT 없음. 이름·타입 변경에 수동 SQL 없음.
8. **동시성.** 코인·라운드 상태를 읽고-계산하고-쓰는데 `@Lock(PESSIMISTIC_WRITE)` 조회가 아니다. 중복 지급을 막는 유니크 제약이 없다(`RewardedCommit`).
9. **입력 검증.** `@RequestBody`에 `@Valid` 없음, 요청 DTO에 제약 어노테이션 없음, 길이 제한 컬럼인데 확인 없음.
10. **외부 API.** 실패를 500으로 내보냄(502/503으로, #267). 타임아웃 없음 — 공용 `RestTemplateConfig` 빈은 타임아웃이 없으니 `OpenAiService`·`GithubOAuthClient`처럼 직접 설정.
11. **컨벤션 위반.** `spring-java-conventions`의 기계 검사·판정 경계.

## [권장] 후보

- N+1: 목록 `from()`이 LAZY 연관을 읽는데 `join fetch`/`@EntityGraph` 없음.
- 목록 쿼리에 정렬 없음.
- Swagger `@ApiResponses`와 실제 상태 코드가 다름, 권한 설명이 실제와 다름.
- 새 서비스 로직에 `@DataJpaTest` 테스트 없음(`ItemServiceTest` 패턴).
- 같은 성격의 기존 API와 응답 필드 이름이 다름(`solvedCount` vs `correctCount`).
- 계속 쌓이는 테이블인데 페이징 없이 전체 반환.
- 엔티티를 응답으로 그대로 반환.

## [질문] 후보

- 조회 API가 상태를 바꾼다(만료 라운드 종료 등). 의도인지.
- 기존 API의 응답 형식·권한이 바뀐다. 프론트엔드와 합의됐는지.
- `/api/assessment/**` 아래 새 API인데 ADMIN이 못 쓰는 게 의도인지.
