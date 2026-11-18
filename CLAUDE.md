# GIF-server-v2

GIF(GSM Idea Festival) — 광주소프트웨어마이스터고 아이디어 페스티벌 운영 플랫폼의 백엔드(VOID, `VOID-GSM`). 프론트엔드는 `GIF_WAB` 레포. 프로젝트·팀, 양식 제출, 점수 평가, 공지·문의, 회고, 역량검사(길 만들기·고양이 술래잡기), 코인·아이템(Thyn 꾸미기), AI 요약·번역, 알림을 다룬다.

사용자: 재학생(`USER`, DataGSM 로그인), 관리자(`ADMIN`, 선생님·VOID, DataGSM 로그인), 외부 사용자(`EXTERNAL`, Google 로그인 — 프로젝트 조회·좋아요·댓글 등 명시적으로 연 기능만).

모든 응답, 커밋 메시지, PR, 리뷰 코멘트, 답글, 작업 노트, 사용자에게 보이는 예외 메시지는 한국어로 쓴다. 코드 식별자와 로그는 영어.

## 스택

| 항목 | 값 |
|------|-----|
| Java / Spring Boot | 21 / 3.4.3, Gradle |
| 영속성 | Spring Data JPA, MySQL·MariaDB. `ddl-auto`는 `DB_DDL` 환경변수 + `SchemaPatchRunner` |
| 보안 | Spring Security, 자체 토큰(`TokenAuthenticationFilter`), DataGSM·Google OAuth |
| 그 외 | Redis, springdoc(Swagger), bean validation, Lombok, OpenAI, GitHub API, Discord(JDA), Web Push |
| 테스트 | JUnit 5, `@DataJpaTest` + H2 |

## 명령어

- 컴파일: `./gradlew compileJava compileTestJava -q`
- 단일 테스트: `./gradlew test --tests "com.example.gifserverv2.domain.item.service.ItemServiceTest"`
- 빌드: `./gradlew build -x test` (CI와 동일 — **CI는 테스트를 돌리지 않는다**)
- 컨벤션 기계 검사: `bash .claude/scripts/convention-check.sh origin/develop`

## 검증을 정직하게

- `GifServerV2ApplicationTests`는 전체 컨텍스트를 띄워서 DB 환경변수(`DB_HOST`, `DB_DRIVER`, `DB_USER`, `DB_PASSWORD`, `DB_DDL`) 없이는 실패한다. 이건 **"검증 못 함"**이다. 통과도 코드 결함도 아니다.
- 서비스 테스트(`@DataJpaTest` + H2)는 로컬에서 돈다. 관련 테스트를 돌리지 않았으면 돌리지 않았다고 말한다.
- 컴파일 성공을 "테스트 통과"로 보고하지 않는다. 확인하지 못한 것은 확인하지 못했다고 쓴다.

## 패키지 구조

```
com.example.gifserverv2
├── domain/<도메인>/{controller, service, repository, entity, dto/request, dto/response, exception}
└── global/{config, exception, security, file, ai, discord, i18n}
```

새 기능은 해당 도메인 패키지 안에 위 구조대로 둔다. 여러 도메인이 공유하는 것만 `global`에 둔다.

## 계층 역할

- **controller**: 요청/응답 처리만 담당한다. 비즈니스 로직 작성 금지.
- **service**: 비즈니스 로직 처리. HTTP 관련 객체(`HttpServletRequest`, `ResponseEntity` 등) 직접 사용 금지.
- **repository**: DB 접근만 담당한다.

## 코드 컨벤션

- 테이블명은 `@Table(name = "snake_case")`로 명시한다.
- DTO는 요청(`Request`)/응답(`Response`)으로 나누고, 동작을 앞에 붙인다.
  - 허용 접두어: `Get`(조회), `Create`(추가), `Patch`(수정), `Delete`(삭제)
  - 예: `GetLectureRequest`. 접두어를 바꾸면 검사 스크립트가 이 줄을 읽어 자동 반영한다.
- Entity, Repository는 대상 DB에 따라 이름을 짓는다. (예: `MemberJpaEntity`, `MemberJpaRepository`)
  - 허용 Entity 접미어: `JpaEntity`
  - 허용 Repository 접미어: `JpaRepository`
  - 다른 DB를 추가하면 위 두 줄에 접미어를 추가한다. 검사 스크립트가 이 줄들을 읽어 자동 반영한다.
- 불필요한 주석은 작성하지 않는다.
- 예외는 도메인별 `XxxException extends ResponseStatusException`에 정적 팩토리 메서드로 추가한다. (예: `FormException.notFound()`)
- 위 네이밍 규칙은 **새로 작성하는 코드**에 적용한다. 기존 클래스는 요청이 없으면 이름을 바꾸지 않는다.

작성법 상세(Entity·DTO·Swagger·트랜잭션)는 `spring-java-conventions` 스킬.

## 이 코드베이스의 함정

빌드로는 안 잡히고 운영에서 깨지는 것들. 모든 변경에서 확인한다.

1. **쓰기 메서드의 `@Transactional` 누락.** 서비스에 클래스 단위 트랜잭션이 없다(60개 중 2개만). 빠지면 로드한 엔티티 변경이 저장되지 않고 **조용히 사라진다.**
2. **SecurityConfig는 위에서부터 처음 맞는 규칙을 쓴다.** 새 경로가 `GET /api/project/*`(authenticated) 같은 와일드카드에 먼저 걸리면 외부 사용자에게 열린다. 아무 규칙에도 안 걸리면 `anyRequest().hasAnyRole("USER", "ADMIN")`.
3. **역할 계층이 없다.** ADMIN이 USER 권한을 갖지 않는다. `/api/assessment/**`는 USER·EXTERNAL만이라 ADMIN은 403. `hasRole`에 `ROLE_` 접두어 금지.
4. **세부 권한은 서비스에서.** `AdminRole`(MAJOR_TEACHER, GENERAL_TEACHER, MASTER, VOID), `ClientRole`(LEADER, MEMBER), `gradeHead`는 `@PreAuthorize`로 표현되지 않는다. `AuthenticatedUser` 필드로 서비스에서 확인한다.
5. **permitAll 경로에서 `AuthenticatedUser`는 null일 수 있다.** `user.userId()`를 바로 부르면 비로그인 요청이 500. (`ProjectController.filterProjects`)
6. **500이 되는 예외.** `GlobalExceptionHandler`는 `ResponseStatusException`(지정 상태), `IllegalArgumentException`(400), 검증 실패(400), `AccessDeniedException`(403)만 매핑한다. `IllegalStateException`, `Optional.get()`, NPE, 외부 API 예외는 500. 엔티티 불변식은 서비스에서 먼저 검사해 4xx로(`CoinService.spend`).
7. **스키마는 `SchemaPatchRunner`에 등록한다.** 운영 `DB_DDL`이 `update`가 아닐 수 있다. 새 테이블·컬럼은 `REQUIRED_TABLES`/`REQUIRED_COLUMNS`에, NOT NULL은 DEFAULT와 함께. 이름·타입 변경과 삭제는 수동 SQL — "완료"로 보고하지 않는다.
8. **동시에 바뀌는 값은 잠근다.** 코인 잔액·라운드 상태는 `@Lock(PESSIMISTIC_WRITE)` 조회로 읽는다.
9. **목록 N+1.** `@ManyToOne`은 전부 LAZY다. 목록 `from()`이 연관을 읽으면 `join fetch`. EAGER 전환 금지.
10. **외부 API 타임아웃.** 공용 `RestTemplateConfig` 빈은 타임아웃이 없다. 실패는 502/503으로(#267).

## Git

- 브랜치: `<태그>/#<이슈번호>-<설명>` (예: `feat/#250-github-push-event-coin`). PR 대상은 `develop`. `main`은 `develop`을 머지해 배포할 때만 쓴다.
  - 태그: feat, fix, style, refactor, comment, docs, test, chore, rename, remove
- 커밋: `<type>: <한국어 설명> (#<이슈번호>)` (예: `fix: 새 브랜치 푸시의 저장소가 404면 그 푸시만 건너뛰기 (#250)`)
  - type: feat, fix, chore, refactor, docs, test, init
  - 커밋 하나 = 관심사 하나, 의존 순서(Entity → Repository → Service → DTO → Controller). `git add -A` 금지.
- PR: `.github/PULL_REQUEST_TEMPLATE.md` 섹션을 모두 채운다. 브랜치당 PR 하나.
- 리뷰 스레드: 반영하면 커밋 해시를 단 답글 후 resolve, 반영하지 않으면 이유를 단 답글만 달고 **열어 둔다**.
- 컨벤션을 지키지 않은 상태로는 merge하지 않는다.

### 확인 게이트

| 작업 | 조건 |
|------|------|
| 커밋 | 요청받았을 때만 ("이거 고쳐줘"는 커밋 요청이 아니다) |
| `git push`, `gh pr create`, 리뷰 게시, 답글, resolve | 사용자 확인 후 |
| `--approve`, `--request-changes` | 명시적 요청만 |
| force push, push된 커밋 amend, `develop`/`main` 직접 커밋 | 절대 안 함 |

## 하네스

**목표:** 계층 경계의 결함(권한 순서, 예외→상태, null principal, 스키마 미반영)을 운영이 아니라 교차 검증에서 잡는다.

**언제 쓰나:** 엔드포인트 추가·변경, Entity·쿼리 변경, 권한 연결, 기존 코드 감사처럼 사소하지 않은 백엔드 작업은 `gif-server-orchestrator` 스킬로 한다. 후속 요청("QA 다시", "보안 부분만 다시")도 같은 스킬이 `_workspace/feature/`를 재사용한다. 한 줄 수정과 질문에는 쓰지 않는다. Java 파일을 쓰거나 리뷰할 때는 오케스트레이터 밖에서도 `spring-java-conventions`를 읽는다.

| 에이전트 | 역할 | 스킬 |
|---------|------|------|
| `api-designer` | 계약, DTO 모양, 실패 표, 권한 | `spring-api-contract` |
| `domain-engineer` | Entity, Repository, Service, 예외, 스키마 | `spring-jpa-domain` |
| `web-engineer` | Controller, DTO, Swagger | `spring-api-contract` |
| `security-engineer` | SecurityConfig, 세부 권한, 본인 확인 | `spring-security-guard` |
| `qa-inspector` | 교차 검사 9개 | `spring-qa-verify` |
| `git-workflow-engineer` | 커밋, PR, 리뷰 작성·반영 | `git-commit-convention`, `github-pr-author`, `github-review-commenter`, `github-review-responder` |

모든 에이전트가 `spring-java-conventions`를 읽는다. 에이전트끼리는 `_workspace/` 파일로 산출물을 넘기고, 오케스트레이터(메인 대화)가 순서를 지휘한다.

| 명령 | 하는 일 |
|------|--------|
| `/self-review` | 내 변경을 `qa-inspector`로 검증하고 BLOCKER 수정 |
| `/open-pr <이슈>` | 커밋 계획·PR 초안 확인 후 커밋·push·PR |
| `/review-pr <번호>` | 팀원 PR 리뷰 초안 확인 후 인라인 코멘트 게시 |
| `/respond-review [번호]` | 받은 리뷰 판정표 확인 후 반영·답글·resolve |

자동 장치: Stop 훅이 Java 변경이 있으면 `compileJava compileTestJava`를 돌려 실패를 되돌려준다(`.claude/hooks/compile-check.sh`).

### 변경 이력

| 날짜 | 변경 | 대상 | 이유 |
|------|------|------|------|
| 2026-11-18 | 최초 구성: CLAUDE.md, 컴파일 Stop 훅, 컨벤션 검사 스크립트 | 전체 | 팀 컨벤션을 매 세션 적용하고 컴파일 깨진 결과를 막기 위해 |
| 2026-11-18 | 리뷰 하네스(`/self-review`, `/review-pr`, `/open-pr`) | `skills/`, `agents/` | 컨벤션 위반이 리뷰 단계에서야 발견됨 |
| 2026-11-18 | TaekBaeWatShong_SERVER 하네스를 참고해 6인 에이전트 팀 + 오케스트레이터로 재편, `/respond-review` 추가, 함정 10개 문서화 | 전체 | 계층 경계 결함을 교차 검증으로 잡고, 리뷰 반영까지 같은 흐름으로 처리하기 위해 |
