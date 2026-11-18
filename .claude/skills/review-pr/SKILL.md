---
name: review-pr
description: 팀원 PR을 리뷰해 초안을 보여주고, 사용자가 확인하면 GitHub에 인라인 코멘트로 게시한다.
argument-hint: <PR 번호>
disable-model-invocation: true
---

# Review PR

## 핵심 역할

PR #$ARGUMENTS 에 머지 전에 고쳐야 할 문제를 인라인 코멘트로 남긴다. 이 스킬은 사용자 확인 게이트이고, 리뷰 자체는 `git-workflow-engineer`가 `github-review-commenter` 기준으로 한다.

## 사용 에이전트

- `git-workflow-engineer` — 작업 종류 `리뷰 작성 <PR>` (`.claude/agents/git-workflow-engineer.md`)

## 작업 원칙

- 사용자가 초안을 확인하기 전에는 게시하지 않는다.
- 인라인 코멘트만. 리뷰 본문 요약·칭찬·인사 없음.
- 작업 트리를 건드리지 않는다. PR 코드는 `origin/pr/<번호>` ref로만 읽는다.

## 절차

1. `gh pr view <pr> --json number,title,author,state,baseRefName,headRefOid`
2. `git fetch origin <baseRefName> "+pull/<pr>/head:refs/remotes/origin/pr/<pr>"`
3. `git-workflow-engineer`를 **계획 모드, 리뷰 작성**으로 부른다. `BASE=origin/<baseRefName>`, `HEAD=origin/pr/<pr>`, `headRefOid`를 넘긴다. `_workspace/pr/review_<pr>.md`가 있으면 재리뷰(그 검사 커밋 이후만)라고 알린다.
4. 초안을 심각도별로 보여주고 확인받는다. 빼거나 고치라는 항목을 반영한다.
5. 확인받은 코멘트로 `git-workflow-engineer`를 **실행 모드**(사용자 확인 받음)로 불러 게시한다.
6. 결과(리뷰 URL, 게시 수, 게시 못 한 코멘트)를 보여준다.

## 에러 핸들링

- `gh` 인증 실패: `! gh auth login` 안내 후 중단.
- PR이 merged/closed: 알리고 계속할지 묻는다.
- 새 커밋 없는 재리뷰: "새 변경 없음"으로 끝낸다.
