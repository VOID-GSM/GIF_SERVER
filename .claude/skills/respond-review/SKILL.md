---
name: respond-review
description: 내 PR에 달린 리뷰 코멘트를 분류해 보여주고, 사용자가 확인하면 반영 커밋·push·해시 답글·resolve(반영한 것만)까지 처리한다. 사용자가 리뷰 반영·코멘트 처리를 요청하거나 /respond-review를 입력하면 사용한다.
argument-hint: "[PR 번호, 생략 시 현재 브랜치 PR]"
---

# Respond Review

## 핵심 역할

PR의 미해결 스레드를 모두 "반영(해시) 후 resolve" 또는 "미반영(이유) 후 열어 둠"으로 끝낸다. 판단 기준은 `github-review-responder`, 실행은 `git-workflow-engineer`, 이 스킬은 확인 게이트다.

## 사용 에이전트

- `git-workflow-engineer` — 작업 종류 `리뷰 반영 <PR>`
- 동작이 바뀌는 반영이 있으면: 해당 계층 엔지니어(`domain-engineer`/`web-engineer`/`security-engineer`) + `qa-inspector`

## 절차

1. PR 번호: `$ARGUMENTS` 또는 `gh pr view --json number`.
2. `git-workflow-engineer`를 **계획 모드, 리뷰 반영**으로 부른다 → 스레드별 판정 표(반영 / 이미 반영됨 / 반영 안 함 / 질문 / 사용자 판단)와 답글 초안.
3. 표를 보여주고 확인받는다. "사용자 판단" 항목은 여기서 답을 받는다.
4. 동작이 바뀌는 반영(서비스 로직, 쿼리, 권한)은 담당 엔지니어에게 코멘트와 위치를 넘겨 고치게 하고, `qa-inspector`로 경계를 다시 확인한다.
5. `git-workflow-engineer`를 **실행 모드**(사용자 확인 받음)로 부른다: 코멘트당 커밋 → push → 해시 답글 → 반영한 스레드만 resolve.
6. 처리 결과와 열어 둔 스레드 수·이유를 보여준다.

## 에러 핸들링

- 반영이 컴파일·테스트를 깨뜨림: 고쳐서 이어간다. 못 고치면 그 스레드는 답글 없이 보고한다.
- 답글·resolve 실패: 1회 재시도 후 "수동 처리 필요"로 보고.
