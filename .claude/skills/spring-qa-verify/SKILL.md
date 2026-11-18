---
name: spring-qa-verify
description: "GIF-server-v2의 계층 사이 경계를 교차 검증한다. Controller↔Service, DTO↔Entity null, 던지는 예외↔계약↔GlobalExceptionHandler, SecurityConfig 순서↔역할, permitAll↔null principal, 파생 쿼리↔Entity 필드, LAZY↔DTO(N+1), Entity↔SchemaPatchRunner, Swagger↔실제 상태 코드. 엔드포인트 구현·변경 후, PR 전, diff 리뷰, 예상 못 한 500/403/null 디버깅, 검증·QA 요청 때 사용한다."
---

# QA Verify

각 클래스가 따로는 맞아도 **둘 사이 경계**가 틀려서 운영 장애가 난다. 모든 검사는 **양쪽 파일을 같이 열어** 비교한다. 한쪽만 읽고 다른 쪽을 짐작하는 것이 이런 버그가 리뷰를 통과하는 방식이다.

## 먼저 돌릴 것

```bash
bash .claude/scripts/convention-check.sh origin/develop   # 컨벤션 기계 검사
./gradlew compileJava compileTestJava -q                  # 타입 오류
./gradlew test --tests "<관련 서비스 테스트>"                # @DataJpaTest + H2: 파생 쿼리, 트랜잭션, 매핑
git diff --stat origin/develop...HEAD                     # 변경 범위
```

- `GifServerV2ApplicationTests`(전체 컨텍스트)는 DB 환경변수(`DB_HOST`, `DB_DRIVER`, `DB_USER`, `DB_PASSWORD`, `DB_DDL`) 없이는 실패한다. 이건 **"검증 못 함"**이다. 통과도, 코드 결함도 아니다.
- `compileJava` 성공을 "테스트 통과"로 보고하지 않는다.
- 서비스 테스트(H2)는 로컬에서 돌아간다. 돌릴 수 있는데 안 돌렸으면 그것도 "검증 안 함"이다.

## 교차 검사 9개

### 1. Controller ↔ Service
컨트롤러가 넘기는 값과 서비스가 기대하는 값, 서비스 반환 DTO와 `ResponseEntity<T>`가 맞는지. 컨트롤러에 비즈니스 분기·repository 접근이 없는지.

### 2. Response DTO ↔ Entity null 가능성
DTO 필드마다, 엔티티가 그 값을 비워 두는 **도달 가능한 상태**가 있는지 본다(진행 중 라운드의 `elapsedSeconds`, 이번 변경 전 데이터의 새 컬럼). 있으면 `@Schema` 설명에 null 조건이 있어야 하고, primitive(`int`, `long`, `boolean`) 필드면 언박싱 NPE가 난다. `from()`이 모든 필드를 채우는지도 확인한다.

### 3. 던지는 예외 ↔ 계약 실패 표 ↔ GlobalExceptionHandler
코드 경로의 모든 `throw`에 대해:
- `ResponseStatusException`/도메인 예외/`IllegalArgumentException`/`AccessDeniedException`이 아니면 **500**이다. 특히 `IllegalStateException`, `Optional.get()`, NPE, 잡지 않은 `RestClientException`.
- 계약의 실패 행마다 대응하는 throw가 있고, throw마다 행이 있는지.
- 엔티티의 `IllegalStateException` 불변식을 사용자 입력이 깨뜨릴 수 있으면, 서비스가 먼저 검사해 4xx로 바꾸는지(`CoinService.spend` 패턴).

### 4. SecurityConfig 순서 ↔ 역할 ↔ 의도한 권한
`spring-security-guard`의 "새 엔드포인트 체크"를 그대로 수행한다. 새 경로를 SecurityConfig 위에서부터 대조해 처음 맞는 규칙을 찾고, USER/ADMIN/EXTERNAL/비로그인 각각의 결과를 적는다. 특히 `GET /api/project/*` 같은 와일드카드가 의도치 않게 외부 사용자에게 여는지, ADMIN이 의도치 않게 막히는지(`/api/assessment/**`).

### 5. permitAll 경로 ↔ principal 사용
permitAll 경로의 컨트롤러·서비스가 `user`를 null 확인 없이 역참조하는지. 비로그인 요청이 500이 된다.

### 6. 파생 쿼리 이름 ↔ Entity 필드
`findAllByUserIdAndStatusOrderByCreatedAtDesc` → `userId`(또는 `user.id`), `status`, `createdAt`이 엔티티에 있는지. 목록 쿼리에 정렬이 있는지. `@Query` JPQL이 엔티티·필드 이름을 쓰는지. 관련 `@DataJpaTest`가 돌면 자동 검증되고, 못 돌렸으면 손으로 대조했다고 적는다.

### 7. LAZY 연관 ↔ DTO 필드 접근 (N+1)
목록을 반환하는 쿼리와 그 `from()`이 읽는 연관을 같이 본다. LAZY 연관을 읽는데 `join fetch`/`@EntityGraph`가 없으면 지적한다.

### 8. Entity 변경 ↔ SchemaPatchRunner
새 필드·새 엔티티가 `REQUIRED_COLUMNS`/`REQUIRED_TABLES`에 있는지. NOT NULL 컬럼에 DEFAULT가 있는지. 이름·타입 변경, 삭제가 있으면 수동 SQL이 노트와 PR 본문에 있는지.

### 9. Swagger ↔ 실제 동작
`@ApiResponses`에 적힌 상태 코드와 3번에서 찾은 실제 상태 코드가 같은지. 적히지 않은 상태가 나오거나, 적혔는데 나올 수 없는 상태가 있으면 지적한다. `@Operation(description)`의 권한 설명이 4번 결과와 같은지.

## 트랜잭션 확인 (모든 변경)

쓰기 메서드마다 `@Transactional`이 있는지 직접 본다. 클래스 단위 트랜잭션이 없어서 빠지면 변경이 조용히 사라진다.

## 보고서 형식

`_workspace/feature/04_qa_report.md`(로컬 리뷰면 호출자에게 반환):

```markdown
# QA 보고서 — <기능>

## 검증 결과
| # | 검사 | 결과 | 근거 |
|---|------|------|------|
| 1 | Controller ↔ Service | PASS | RoundController.java:71 → RoadMakingService.java:124 |

## 발견 사항
### [BLOCKER] <한 줄 결함>
- **위치:** `A.java:LINE` ↔ `B.java:LINE`
- **실패 시나리오:** 구체적 입력 → 잘못된 결과
- **수정:** 구체적 변경, 담당 에이전트

## 검증 못 함
- <검사> — <이유: DB 환경변수 없음 등>
```

심각도: **BLOCKER**(런타임 오동작: 500, 권한 누락·과다, 변경 유실, 스키마 미반영, 컨벤션 위반), **MAJOR**(부하·경계 조건에서 틀림: N+1, 정렬 없음, null 처리 누락, Swagger 불일치), **MINOR**(이름, 죽은 코드).

**"검증 못 함" 절은 필수다.** 환경 때문에 못 돌린 검사가 있으면 비워 둘 수 없다. 말하지 않은 검사는 통과로 읽힌다.

## 보고 규칙

- 경계 발견은 **양쪽** `file:line`을 적는다. 한쪽만 적힌 발견은 교차 검사를 안 한 것이다.
- 구체적 실패 시나리오: "비로그인 사용자가 `GET /api/project/filter`를 부르면 `user.userId()`에서 NPE → 500" — "null일 수도 있음"이 아니라.
- 돌리지 않은 검사를 PASS로 적지 않는다.
- 발견이 10개를 넘으면 BLOCKER는 전부 쓰고 나머지는 분류별로 요약한다.
