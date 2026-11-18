---
name: gif-server-orchestrator
description: "GIF-server-v2 백엔드 에이전트 팀(api-designer, domain-engineer, web-engineer, security-engineer, qa-inspector, git-workflow-engineer)을 지휘해 기능을 설계·구현·검증·전달한다. 엔드포인트 추가·변경, Entity·쿼리 변경, 권한 연결, 기존 코드 감사 같은 사소하지 않은 백엔드 작업 전부에 사용한다. 후속 요청('QA 다시', 'QA가 찾은 것 고쳐줘', '보안 부분만 다시', '이전 결과 개선', '기능 확장')도 이 스킬로 처리하며 이전 _workspace를 재사용한다. 한 줄 수정이나 코드 변경 없는 질문에는 쓰지 않는다."
---

# GIF Server Orchestrator

## 실행 방식: 서브에이전트 파이프라인

계층이 강하게 묶여 있다. DTO 모양이 Entity를 제약하고, 권한 규칙이 컨트롤러와 서비스를 같이 제약하며, 가치가 큰 결함(SecurityConfig 순서, 예외→상태 매핑, null principal)은 두 에이전트 파일 **사이**에 있다. 그래서 계약을 먼저 고정하고, 계층 순서대로 구현한 뒤, QA가 경계를 교차 검증하고, 발견을 담당자에게 되돌리는 루프로 진행한다.

에이전트끼리 직접 대화하지 않는다. 산출물은 `_workspace/feature/` 파일로 넘기고, 리더(이 스킬을 실행하는 메인 대화)가 다음 에이전트에게 앞 단계 파일 경로를 넘긴다.

## 팀

| 에이전트 | 역할 | 스킬 | 산출물 |
|---------|------|------|--------|
| `api-designer` | 엔드포인트 계약, DTO 모양, 실패 표, 권한 | `spring-api-contract` | `01_contract.md` |
| `domain-engineer` | Entity, Repository, Service, 예외, 스키마 | `spring-jpa-domain` | `02_domain_notes.md` |
| `web-engineer` | Controller, 요청·응답 DTO, Swagger | `spring-api-contract` | `03_web_notes.md` |
| `security-engineer` | SecurityConfig, `@PreAuthorize`, 세부 권한, 본인 확인 | `spring-security-guard` | `03_security_notes.md` |
| `qa-inspector` | 교차 검사 9개 | `spring-qa-verify` | `04_qa_report.md` |
| `git-workflow-engineer` | 커밋, PR, 리뷰 코멘트·답글 | `git-commit-convention`, `github-pr-author`, `github-review-commenter`, `github-review-responder` | `_workspace/pr/delivery_<브랜치>.md` |
| 리더(메인 대화) | 지휘, 사용자 확인, 빌드, 요약 | 이 스킬 | 최종 요약 |

모든 에이전트는 `spring-java-conventions`와 루트 `CLAUDE.md`를 읽는다. `git-workflow-engineer`는 5단계에서만 부른다(덜 끝난 작업을 커밋하지 못하게).

## 0단계: 실행 모드 판단

1. `_workspace/feature/`가 있는지 본다.
2. 분기:
   - **없음** → 최초 실행. 1단계로.
   - **있음 + 부분 수정 요청**("보안 부분만", "QA 발견 고쳐줘", "QA 다시") → 부분 재실행. 1단계를 건너뛰고 필요한 에이전트만 부른다. 각자 자기 이전 산출물 경로를 받아 **새로 만들지 말고 고치게** 한다.
   - **있음 + 무관한 새 기능** → `_workspace/feature/`를 `_workspace/archive/<YYYYMMDD_HHMMSS>/`로 옮기고 1단계로.
3. 부분 재실행에도 `qa-inspector`는 항상 부른다. 코드가 바뀌면 이전 보고서 일부가 무효가 된다.

## 1단계: 준비

1. 요청에서 대상 도메인(`domain/<x>`), 엔드포인트, 권한 변경 여부, 스키마 변경 여부를 파악한다.
2. `_workspace/feature/00_input/request.md`에 요청과 사용자 제약을 적는다.
3. 대상 도메인의 기존 코드를 읽어 실제 파일 이름을 지시에 넣는다(추상적 설명 대신).
4. `git rev-parse HEAD`, `git status --short`를 `_workspace/feature/00_input/baseline.md`에 적는다.
5. 요구사항이 모호해 계약을 정할 수 없으면(누가 쓸 수 있나, 실패 시 어떻게 되나) 여기서 사용자에게 묻는다.

## 2단계: 계약

`api-designer`를 부른다 → `01_contract.md`. 메모에 **제품 결정이 필요한 항목**이 있으면 사용자에게 보여주고 답을 받은 뒤 진행한다. 보수적인 해석으로 진행해도 되는 항목은 그렇게 표시하고 넘어간다.

## 3단계: 구현 (순서대로)

| 순서 | 에이전트 | 입력 | 하는 일 |
|------|---------|------|--------|
| 1 | `domain-engineer` | 계약 | Entity·SchemaPatchRunner·Repository·예외·Service. 노트에 **서비스 시그니처**와 **## 스키마 영향** |
| 2 | `web-engineer` | 계약 + 도메인 노트 | DTO·Controller·Swagger. 노트에 새 경로 목록 |
| 3 | `security-engineer` | 계약 + 도메인·웹 노트 | SecurityConfig 규칙·위치, `@PreAuthorize`, 서비스의 세부 권한·본인 확인. 노트에 권한 표 |

파일 소유: Controller와 DTO는 `web-engineer`, `@PreAuthorize`와 `SecurityConfig`는 `security-engineer`, Service는 `domain-engineer`가 쓰되 권한 검사 코드는 `security-engineer`가 추가할 수 있다. 순서대로 실행하므로 같은 파일을 동시에 고치지 않는다.

에이전트가 계약을 구현할 수 없다고 보고하면(필수 필드가 null일 수밖에 없음 등) `api-designer`를 다시 불러 계약을 고친다. Entity가 런타임의 사실이므로 Entity가 이긴다.

## 4단계: 검증 루프

1. `qa-inspector`를 부른다 → `04_qa_report.md`.
2. BLOCKER가 있으면 담당 에이전트에게 해당 발견과 양쪽 `file:line`을 넘겨 고치게 하고, `qa-inspector`로 다시 확인한다. **루프는 최대 2회.** 그래도 남으면 사용자에게 올린다.
3. 리더가 직접 빌드한다 — 에이전트의 말을 믿지 않는다:
   ```bash
   ./gradlew compileJava compileTestJava -q
   ./gradlew test --tests "<관련 서비스 테스트>"
   bash .claude/scripts/convention-check.sh origin/develop
   ```
4. 도메인 노트의 `## 스키마 영향`을 모은다. 수동 SQL이 필요하면 요약 맨 위에 둔다 — 조용히 실패하는 유일한 것이다.

## 5단계: 전달 (요청이 있을 때만)

사용자가 커밋·PR·리뷰 처리를 원할 때만. 탐색·감사만 한 실행이면 건너뛴다.

1. `git-workflow-engineer`를 **계획 모드**로 부른다: 커밋 분할 계획, 브랜치명, PR 제목·본문 초안을 받는다. QA 보고서의 남은 BLOCKER와 "검증 못 함"을 PR 본문에 반영하게 한다.
2. 계획을 사용자에게 보여주고 확인받는다. **push와 `gh pr create`는 외부 공개 작업이다.**
3. 확인받은 계획으로 **실행 모드**로 다시 부른다.
4. 리뷰 코멘트 처리는 `/respond-review`와 같은 흐름: 동작이 바뀌는 반영은 담당 엔지니어에게 되돌리고 `qa-inspector`가 다시 확인한 뒤 답글.

## 최종 요약 형식 (한국어)

```markdown
## <기능> — 완료 | 막힘

**변경:** <n>개 파일 (<계층>)
**빌드:** compileJava PASS | 서비스 테스트 PASS/FAIL/미실행(<이유>) | 컨벤션 검사 PASS/FAIL
**QA:** BLOCKER <n>, MAJOR <n>, MINOR <n> — 남은 것 <n>

### 엔드포인트
| METHOD | 경로 | 권한(USER/ADMIN/EXTERNAL/비로그인) | 상태 |

### 남은 항목
- <BLOCKER 또는 필요한 제품 결정>

### 스키마 영향
- <SchemaPatchRunner 등록 내용 / 필요한 수동 SQL / "없음">

### 검증 못 함
- <환경 때문에 못 돌린 검사와 이유>
```

마지막 두 절은 필수다. 이 프로젝트에서 조용한 실패가 쌓이는 곳이 스키마와 테스트하지 못한 경로다.

## 에러 처리

| 상황 | 대응 |
|------|------|
| 에이전트가 실패·중단 | 1회 재시도. 다시 실패하면 남은 일을 리더가 처리하거나 요약에 빈 곳으로 적는다 |
| `GifServerV2ApplicationTests`가 DB 환경변수 때문에 실패 | 코드 실패가 아니다. 출력 그대로 "검증 못 함"에 적고 서비스 테스트·정적 검증으로 진행. **통과로도 결함으로도 보고하지 않는다** |
| QA와 엔지니어 의견 충돌 | 리더가 양쪽 파일을 읽고 결정. 루프 2회 초과 금지 |
| 계약과 Entity 모순 | Entity가 이긴다. `api-designer`가 계약을 고치고 정정을 기록 |
| 스키마 변경이 자동 반영 불가(이름·타입 변경, 삭제) | 그 작업을 멈추고 필요한 수동 SQL을 사용자에게 먼저 보여준다. "완료"로 보고하지 않는다 |
| 기존 권한을 넓히는 변경 | 멈추고 누가 새로 접근하게 되는지 정확히 적어 사용자에게 확인 |
| 4단계 끝에 BLOCKER 3개 초과 | 완료로 보고하지 않는다. BLOCKER를 보고하고 진행 방법을 묻는다 |

## 부분 재실행 예

| 사용자 요청 | 부를 에이전트 | workspace |
|------------|--------------|-----------|
| "QA 다시" | `qa-inspector` | 재사용, 보고서에 날짜 절 추가 |
| "QA가 찾은 것 고쳐줘" | 지목된 엔지니어 + `qa-inspector` | 재사용 |
| "보안 부분만 다시" | `security-engineer` + `qa-inspector` | 재사용 |
| "계약에서 X가 틀렸어" | `api-designer` + 영향받는 엔지니어 + `qa-inspector` | 재사용 |
| "Y 엔드포인트 추가" | 전체 | 재사용, 추가 |
| 무관한 새 기능 | 전체 | 보관 후 새로 |
| "커밋하고 PR 올려줘" | `git-workflow-engineer` | 재사용 |
| "리뷰 반영해줘" | `git-workflow-engineer` + 지목된 엔지니어 + `qa-inspector` | 재사용 |
