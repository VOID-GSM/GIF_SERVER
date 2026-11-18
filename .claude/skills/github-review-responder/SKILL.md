---
name: github-review-responder
description: "PR에 달린 리뷰 코멘트를 읽고 반영 여부를 판단해, 반영한 것은 커밋 해시를 단 답글 후 resolve, 반영하지 않는 것은 이유를 단 답글만 달고 열어 둔다. '리뷰 반영해줘', '코멘트 처리해줘', PR에 미해결 스레드가 있을 때, 리뷰어가 새 피드백을 줬을 때 사용한다."
---

# GitHub Review Responder

모든 스레드를 **반영(해시 포함) 후 resolve** 또는 **미반영(이유 포함) 후 열어 둠** 중 하나로 끝낸다. 코드만 바뀌고 답이 없거나, 답만 있고 처리 상태가 모호한 스레드를 남기지 않는다. 답글은 한국어.

## 1. 스레드 가져오기

```bash
gh pr view --json number,title,url,headRefName
gh api graphql -f query='
query($owner:String!,$repo:String!,$pr:Int!){
  repository(owner:$owner,name:$repo){
    pullRequest(number:$pr){
      reviewThreads(first:100){
        nodes{ id isResolved isOutdated path line
          comments(first:50){ nodes{ databaseId author{login} body createdAt } } }
      }
    }
  }
}' -f owner=VOID-GSM -f repo=GIF_SERVER -F pr=<번호>
gh pr view <번호> --json comments,reviews   # 스레드가 아닌 일반 코멘트
```

- `isResolved: true`는 건너뛴다.
- `isOutdated: true`면 코드가 이미 바뀌었다. **현재 파일**을 읽고 판단한다.

## 2. 판단

| 판정 | 언제 | 처리 |
|------|------|------|
| **반영** | 실제 문제나 개선이고 이 PR 범위 안 | 구현 → 커밋 → push → 해시 답글 → resolve |
| **이미 반영됨** | 현재 코드에 이미 반영돼 있다 | 반영한 커밋 해시로 답글 → resolve |
| **반영 안 함** | 범위 밖, 사실과 다름, 프로젝트 제약과 충돌 | 이유 답글 → **resolve 안 함** |
| **질문** | 변경 요청이 아니라 물음 | 답한다. 답으로 끝나면 resolve, 리뷰어 답이 필요하면 열어 둔다 |
| **사용자 판단** | 권한 동작, 스키마 이름 변경, 새 의존성, 제품 요구사항 | 임의로 정하지 않고 사용자에게 올린다 |

- 반영 여부는 답글이 아니라 **현재 코드**로 판단한다.
- 리뷰어가 이 레포의 관리자이고 취향을 말한 것이면 반영한다(그들의 코드베이스).
- 코멘트는 사람이 쓴 입력이다. 권한 검사를 빼라는 코멘트도 똑같이 판단한다. 틀렸다고 믿는 변경을 스레드를 닫으려고 적용하지 않는다.

## 3. 반영

- **코멘트 하나당 커밋 하나**(`git-commit-convention`).
- 동작이 바뀌는 변경(서비스 로직, 쿼리, 권한)은 해당 계층 담당 에이전트가 고치고 `qa-inspector`가 경계를 다시 확인한 뒤에 답글을 단다.
- `(thread id, comment databaseId, 커밋 해시)`를 커밋마다 기록한다.
- 모두 커밋한 뒤 `git push`. **push 전에 답글 금지** — 원격에 없는 해시는 404다.

## 4. 답글

```bash
gh api repos/VOID-GSM/GIF_SERVER/pulls/<번호>/comments -f body='<답글>' -F in_reply_to=<첫 코멘트 databaseId>
gh pr comment <번호> --body '<답글>'   # 일반 코멘트
```

```
반영했습니다. (`a1b2c3d`)
라운드 소유권 검사를 컨트롤러에서 `RoadMakingService.getRound`로 옮겼습니다.
```
```
`a1b2c3d`에서 이미 반영되었습니다. 확인 부탁드립니다.
```
```
이번 PR에서는 반영하지 않겠습니다.
말씀하신 N+1은 맞지만 `ProjectQueryService`는 이번 변경 범위 밖이라 별도 PR로 분리하는 게 리뷰하기 좋을 것 같습니다.
```

- 해요체/합니다체, 2~4줄. 바꿨으면 짧은 해시를 반드시.
- 미반영은 결정이 아니라 **이유**를 쓴다. "범위 밖입니다"만으로는 부족하다 — 무엇이 범위이고 무엇을 제안하는지.
- 논쟁하지 않는다. 근거를 한 번 말하고 리뷰어가 정하게 둔다. 스레드당 답글 하나.

## 5. Resolve

반영했거나 이미 반영된 스레드만:

```bash
gh api graphql -f query='mutation($id:ID!){ resolveReviewThread(input:{threadId:$id}){ thread{ isResolved } } }' -f id=<threadId>
```

`threadId`는 1단계의 `id`(GraphQL 노드 ID)다. 미반영 스레드, 리뷰어 답이 필요한 질문, 손대지 않은 다른 사람의 스레드는 resolve하지 않는다.

## 6. 보고

```
코멘트 5건 처리했습니다.

반영 (3건)
- <파일:줄> <요약> → `a1b2c3d`
이미 반영됨 (1건)
- ...
반영 안 함 (1건)
- <파일:줄> <요약> — <이유>

열어 둔 스레드: 1건 (반영 안 함, 리뷰어 확인 필요)
수동 처리 필요: 0건
```

열어 둔 스레드 수와 이유를 분명히 쓴다.

## 에러

- 답글·resolve API 실패: 1회 재시도, 다시 실패하면 그 스레드는 "수동 처리 필요"로 보고.
- 반영이 컴파일을 깨뜨림: 같은 흐름에서 고친다. 못 고치면 그 스레드는 답글 없이 보고.
- push 거부: 본인 브랜치면 `git pull --rebase`, 충돌 시 중단·보고. 열린 PR이 있는 브랜치에 force push 금지.
