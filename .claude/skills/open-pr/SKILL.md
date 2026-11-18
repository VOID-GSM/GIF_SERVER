---
name: open-pr
description: 작업 트리의 변경을 팀 규칙대로 커밋하고 develop 대상 PR을 만든다(이미 있으면 갱신). 사용자가 PR을 올려 달라고 하거나 /open-pr을 입력하면 사용한다.
argument-hint: "[이슈 번호] [--draft]"
---

# Open PR

## 핵심 역할

`git-workflow-engineer`가 커밋·PR을 만들 수 있게 입력을 갖추고, push 전에 사용자 확인을 받는 게이트다.

## 사용 에이전트

- `git-workflow-engineer` — 작업 종류 `커밋·PR` (`.claude/agents/git-workflow-engineer.md`)

## 절차

1. 이슈 번호: `$ARGUMENTS` → 현재 브랜치 이름(`feat/#250-...`) 순으로 찾는다. 없으면 묻는다. 추측하지 않는다.
2. `gh issue view <이슈> --json title,labels,state`로 열린 이슈인지 확인한다.
3. `git-workflow-engineer`를 **계획 모드**로 부른다: 이슈 번호, draft 여부, `_workspace/feature/04_qa_report.md`(있으면).
4. 계획(브랜치명, 커밋 분할, 커밋하지 않을 파일, PR 제목·본문)을 보여주고 확인받는다. 열린 PR이 있으면 "기존 PR #n 갱신"으로 보여준다.
5. **실행 모드**(사용자 확인 받음)로 다시 부른다.
6. PR URL, 커밋 목록, 커밋하지 않은 파일을 보여준다.

## 에러 핸들링

- 컨벤션·컴파일 실패로 중단: 오류를 보여주고 `/self-review`를 먼저 제안한다.
- `gh` 인증 실패: `! gh auth login` 안내.
