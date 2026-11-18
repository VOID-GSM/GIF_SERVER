---
name: self-review
description: PR을 올리기 전에 내 브랜치 변경(커밋 안 된 변경 포함)을 경계 검증(qa-inspector)하고 BLOCKER를 고친다. 사용자가 PR 생성 전 점검을 요청하거나 /self-review를 입력하면 사용한다.
argument-hint: "[BASE 브랜치, 기본 develop]"
---

# Self Review

## 핵심 역할

PR 전에 BLOCKER를 0개로 만든다. 오케스트레이터 없이 직접 짠 코드에도 같은 검증을 적용하기 위한 진입점이다.

## 사용 에이전트

- `qa-inspector` — 교차 검사 9개 (`.claude/agents/qa-inspector.md`)

## 작업 원칙

- BLOCKER는 바로 고친다. MAJOR는 목록으로 보여주고 고칠지 묻는다. 제품 결정이 필요한 것은 묻는다.
- 이번 변경 범위 밖은 고치지 않는다.
- 고친 뒤 다시 검사해 결과로 확인한다.
- 커밋은 사용자가 요청할 때만.

## 절차

1. `git fetch origin <BASE>` (BASE = `$ARGUMENTS` 또는 `develop`)
2. `qa-inspector`를 부른다: `BASE=origin/<BASE>`, 작업 트리 검사, 결과 반환.
3. BLOCKER를 고친다(`spring-java-conventions`, 해당 계층 스킬 기준).
4. 다시 확인: `bash .claude/scripts/convention-check.sh origin/<BASE>`, `./gradlew compileJava compileTestJava -q`, 관련 `./gradlew test --tests ...`.

## 출력

| 위치 | 심각도 | 내용 | 처리 (수정 / 사용자 판단 / 범위 밖) |
|------|--------|------|-----|

표 아래: 재검사 결과, 테스트 결과, **검증 못 함** 목록.

## 에러 핸들링

- 수정이 컴파일을 깨뜨림: 같은 흐름에서 고치고, 못 고치면 되돌리고 보고.
- 테스트 실패: 수정 전에도 실패했는지 먼저 확인. 기존 실패면 보고만.
- `GifServerV2ApplicationTests`가 DB 환경변수로 실패: "검증 못 함"에 적는다.
