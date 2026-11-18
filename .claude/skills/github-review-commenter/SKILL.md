---
name: github-review-commenter
description: "GIF-server-v2 PR을 리뷰해 한국어 인라인 코멘트 초안을 만든다. 무엇이 코멘트할 가치가 있는지의 기준, 이 코드베이스의 결함 패턴(references/checklist.md), 코멘트 형식, gh 게시 방법. 팀원 PR 리뷰, 머지 전 브랜치 리뷰에 사용한다."
---

# GitHub Review Commenter

## 코멘트를 남길 기준

코멘트 하나마다 작성자가 읽고 판단하고 답하는 비용이 든다. 질문은 "이게 사실인가?"가 아니라 **"작성자가 이것 때문에 코드를 바꿀까?"**다.

| 남긴다 | 남기지 않는다 |
|--------|--------------|
| 구체적 입력에서 틀리게 동작한다 | 내가 썼으면 다르게 썼을 것이다 |
| 이 레포의 컨벤션(`CLAUDE.md`)을 어긴다 | 이 레포 어디에서도 안 지키는 일반 모범 사례 |
| 실패 경로가 처리 안 되거나 상태 코드가 틀리다 | 취향 차이인 이름 |
| 권한이 빠졌거나 넓다 | 이미 다른 코멘트(다른 리뷰어 포함)가 지적한 것 |
| 계층 경계가 서로 맞지 않는다 | diff가 하는 일을 다시 설명하는 것 |
| 정말 이해가 안 돼서 답이 필요하다 | |

칭찬, 요약, 인사는 쓰지 않는다. 인라인 코멘트만 단다.

## 볼 것

`references/checklist.md`를 위에서부터 적용한다. 방법은 `spring-qa-verify`의 교차 검사(양쪽 파일을 같이 연다)를 따른다. diff에는 경계의 반대쪽이 안 보인다 — `SecurityConfig`, `SchemaPatchRunner`, `GlobalExceptionHandler`, 호출하는 서비스를 직접 연다.

## 심각도

| 접두어 | 기준 |
|--------|------|
| `[필수]` | 머지하면 버그, 보안 문제, 운영 장애, 데이터 손상, 또는 컨벤션 위반(CLAUDE.md: 미준수 merge 금지) |
| `[권장]` | 동작은 하지만 유지보수·성능·일관성이 눈에 띄게 나빠진다 |
| `[질문]` | 의도를 모르면 판단할 수 없다. 진짜 질문이지 돌려 말한 요구가 아니다 |

같은 도메인의 기존 코드가 같은 방식이면 `[질문]`으로 낮춘다. 확신이 없어도 `[질문]`.

## 코멘트 형식

무엇이 틀렸나 → 왜 문제인가 → 어떻게 하나. 해요체, 사람이 아니라 코드에 대해.

~~~
[필수] `submitAnswer`가 쓰기 메서드인데 `@Transactional`이 없습니다.
이 서비스엔 클래스 단위 트랜잭션이 없어서 `round.complete()` 변경이 저장되지 않습니다.
```suggestion
    @Transactional
    public SubmitRoadMakingAnswerResponse submitAnswer(...) {
```
~~~

~~~
[필수] 새 `GET /api/project/drafts`가 SecurityConfig의 `GET /api/project/*`(authenticated)에 먼저 걸려 외부 사용자에게 열립니다.
재학생 전용이면 그 규칙보다 위에 `.requestMatchers(HttpMethod.GET, "/api/project/drafts").hasAnyRole(SCHOOL_ROLES)`를 추가해 주세요.
~~~

- suggestion 블록은 그 줄(범위)을 그대로 대체할 수 있을 때만.
- 같은 원인이 여러 줄에 반복되면 첫 줄에만 쓰고 `(같은 문제: 57, 80행)`.
- 다른 파일이 근거면 `File.java:10`을 적는다.

## 초안 형식 (호출자에게 반환)

```
[필수] n / [권장] n / [질문] n

- path/to/File.java:42
  <코멘트 본문>
```

`line`은 HEAD 기준 줄 번호이고 diff 안이어야 한다.

## 게시 (사용자 확인 후, 호출자가 지시할 때만)

```json
{ "commit_id": "<headRefOid>", "event": "COMMENT",
  "comments": [{ "path": "src/main/java/...", "line": 42, "side": "RIGHT", "body": "[필수] ..." }] }
```

```bash
gh api repos/{owner}/{repo}/pulls/<pr>/reviews --method POST --input <json 파일>
```

- `event`는 항상 `COMMENT`. `APPROVE`/`REQUEST_CHANGES`는 사용자가 명시적으로 요청할 때만.
- 422(diff 밖의 줄): 같은 파일의 가장 가까운 변경 줄로 옮겨 1회 재시도. 옮길 줄이 없으면 빼고, 뺀 코멘트를 보고한다. 리뷰 본문으로 옮기지 않는다.

## 규칙

- 구체적 실패나 이 레포의 컨벤션으로 정당화할 수 없는 코멘트는 남기지 않는다.
- 경계에 대한 코멘트는 양쪽을 읽은 뒤에만. 한 파일만 보고 쓴 코멘트는 추측이다.
- PR이 크면 가치 높은 항목에 집중하고, 나머지는 얕게 봤다고 호출자에게 알린다.
