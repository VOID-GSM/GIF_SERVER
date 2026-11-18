---
name: github-pr-author
description: "GIF-server-v2 브랜치 규칙, PR 제목·본문(템플릿), 라벨·담당자, 생성·갱신 명령. PR을 만들거나 갱신할 때 사용한다."
---

# GitHub PR Author

## 브랜치

- 형식: `<태그>/#<이슈번호>-<영문-kebab-설명>` (예: `feat/#271-road-making-rounds`). 태그는 `CLAUDE.md` Git 섹션 목록.
- 최신 `origin/develop`에서 만든다: `git fetch origin develop && git switch -c <브랜치> origin/develop`
  - 작업 트리에 변경이 있으면 `git switch -c <브랜치>`로 현재 위치에서 만들고, `origin/develop`보다 뒤처졌는지 보고한다.
- PR 대상은 항상 `develop`. `main`은 `develop`을 머지해 배포할 때만 쓴다.

## 제목

`<type>: <한국어 설명>` — 이슈 번호는 넣지 않는다(본문에 쓴다). 커밋이 여러 개면 PR 전체를 대표하는 한 줄.

## 본문

`.github/PULL_REQUEST_TEMPLATE.md`의 섹션 제목을 그대로 두고 내용만 채운다. 실제 예: PR #273.

- **#️⃣연관된 이슈**: `> Closes #<이슈번호>`
- **📝작업 내용**: 리뷰어가 diff를 열기 전에 이해할 수 있게.
  - 새·변경 API: `METHOD /path: 한 줄 설명`, 주요 응답 필드, 권한(외부 사용자 포함 여부), 오류 응답
  - 스키마 변경과 `SchemaPatchRunner` 등록 여부, 수동 SQL이 필요하면 그 SQL
  - 테스트: 테스트 클래스와 검증한 시나리오
  - **검증 못 한 것**: 실행하지 못한 테스트와 이유. 예: "`GifServerV2ApplicationTests`는 로컬에 DB 환경변수가 없어 실패(기존 실패)". 돌리지 않은 테스트를 통과했다고 쓰지 않는다.
- **스크린샷 (선택)**: 서버 작업이면 비운다.
- **💬리뷰 요구사항(선택)**: 판단이 필요했던 결정을 인용 블록으로(조회 API가 상태를 바꾸는 이유, 기존 API와 다른 필드명, 운영 `DB_DDL` 관련 주의). 없으면 비운다.

QA 보고서(`_workspace/feature/04_qa_report.md`)가 있으면 남은 BLOCKER와 "검증 못 함" 목록을 작업 내용과 리뷰 요구사항에 반영한다.

본문은 `_workspace/pr/body_<브랜치>.md`에 파일로 쓰고 `--body-file`로 넘긴다.

## 라벨·담당자

- 라벨: 연관 이슈의 라벨 그대로 — `gh issue view <이슈> --json labels --jq '[.labels[].name] | join(",")'`
- 담당자: `@me`

## 생성 / 갱신

먼저 열린 PR이 있는지 본다: `gh pr view --json number,url,body`

```bash
git push -u origin <브랜치>
gh pr create --base develop --head <브랜치> --title "<제목>" --body-file <본문 파일> --label "<라벨>" --assignee @me [--draft]
```

- 브랜치당 PR 하나. 열린 PR이 있으면 새로 만들지 않고, push 후 "📝작업 내용"에 새 커밋 내용을 더해 `gh pr edit <번호> --body-file <본문 파일>`. 다른 섹션은 지우지 않는다.
- push와 PR 생성·수정은 외부에 공개된다. 호출자가 사용자 확인을 받았다고 명시한 경우에만 실행한다.
