---
name: spring-jpa-domain
description: "GIF-server-v2의 Entity 상태 변경, Repository 쿼리, 서비스 트랜잭션, 동시성 락, N+1 방지, SchemaPatchRunner 기반 스키마 변경을 다룬다. Entity·Repository·Service를 추가·변경할 때, 목록 API 성능을 볼 때, 컬럼·테이블을 바꿀 때 사용한다."
---

# JPA Domain

`spring-java-conventions`를 먼저 읽는다.

## 트랜잭션

- 서비스에 클래스 단위 `@Transactional`이 없다. **메서드마다** 단다.
- 쓰기 메서드에 `@Transactional`이 없으면 트랜잭션 없이 실행된다. 로드한 엔티티를 바꿔도 dirty checking이 일어나지 않아 **변경이 조용히 사라진다**(예외도 로그도 없음). 새 메서드마다 확인한다.
- 조회는 `@Transactional(readOnly = true)`. LAZY 연관을 DTO 변환 중에 읽으려면 트랜잭션 안이어야 한다.
- 같은 클래스 안 `this.method()` 호출에는 `@Transactional`이 적용되지 않는다(프록시 우회).
- 예외를 던져도 앞선 상태 변경은 커밋돼야 하면 `noRollbackFor`(`RoadMakingService`, `TeamJoinRequestService`).

## 상태 변경

- 엔티티 메서드로만: `round.complete(now)`. 서비스에서 필드를 직접 바꾸지 않는다(`@Setter` 없음).
- 상태와 짝이 되는 시각·값을 한 메서드에서 같이 바꾼다.
- 계산 값(정답률, 소요 시간)은 컬럼이 아니라 엔티티 메서드로(`RoadMakingRound`로 옮긴 PR #273).

## 쿼리

- 파생 쿼리 이름은 엔티티 필드 이름과 정확히 맞아야 한다. 틀리면 컴파일은 되고 **애플리케이션 시작(또는 `@DataJpaTest`) 때** 실패한다.
- 목록은 정렬을 명시한다. 없으면 MySQL 반환 순서가 정해지지 않는다.
- `@Query` JPQL은 엔티티·필드 이름을 쓴다(테이블·컬럼 이름 아님). `@Param` 이름과 플레이스홀더를 맞춘다.
- `@Modifying` 벌크 쿼리 뒤에 같은 트랜잭션에서 엔티티를 다시 읽으면 영속성 컨텍스트가 낡는다 → `clearAutomatically`/`flushAutomatically`(`NotificationRepository`, `TeamJoinRequestRepository`).

## N+1

- 목록 응답의 `from()`이 LAZY 연관(`project.getLeader().getName()` 등)을 읽으면 행마다 쿼리가 하나씩 더 나간다.
- 해결은 리포지토리에서 `join fetch` 또는 `@EntityGraph`. EAGER 전환 금지.
- 컬렉션 `join fetch`와 페이징을 같이 쓰면 메모리 페이징이 된다. 그 경우 `@BatchSize`나 ID 먼저 조회를 쓴다.

## 동시성

- 읽고-계산하고-쓰는 값(코인 잔액, 라운드 진행 상태, 문항 수)은 `@Lock(PESSIMISTIC_WRITE)` 조회로 잠근다(`CoinAccountRepository`, `RoadMakingRoundRepository`, `CatChaseRoundRepository`, `UserRepository`).
- 중복 지급·중복 생성은 유니크 제약으로도 막는다(`RewardedCommit`의 `uk_rewarded_commit_user_sha`).

## 스키마 변경

`ddl-auto`는 환경 변수 `DB_DDL`이라 환경마다 다르다. 운영이 `update`가 아닐 수 있어서 `global/config/SchemaPatchRunner`가 시작할 때 빠진 테이블·컬럼·인덱스를 만든다.

| 변경 | 해야 할 일 |
|------|-----------|
| 새 `@Entity`(테이블) | `REQUIRED_TABLES`에 `TableSpec` 추가(CREATE TABLE IF NOT EXISTS, 인덱스 포함) |
| 기존 테이블에 nullable 컬럼 | `REQUIRED_COLUMNS`에 `ColumnSpec` 추가 |
| 기존 테이블에 NOT NULL 컬럼 | `DEFAULT`를 넣은 `ColumnSpec`. DEFAULT 없으면 기존 행 때문에 실패 |
| 필드 이름 변경 | 새 컬럼이 생기고 옛 컬럼 데이터는 버려진다 → **수동 SQL 필요**, 완료로 보고하지 않는다 |
| 타입 변경, 필드 삭제 | 자동 반영 안 됨 → **수동 SQL 필요** |

작업 노트에 `## 스키마 영향` 절을 반드시 쓴다: 무엇이 자동 반영되고, 무엇이 수동 SQL을 필요로 하는지.

## 테스트

- 서비스 테스트는 `@DataJpaTest` + `@Import({XxxService.class, ...})` + H2(`ItemServiceTest`). 파생 쿼리 이름도 여기서 검증된다.
- `@Transactional(propagation = NOT_SUPPORTED)`로 테스트 트랜잭션을 끄면 실제 커밋 동작(트랜잭션 누락 버그)을 잡을 수 있다(`ItemServiceTest`).
- 락이 필요한 동시성은 H2에서 재현이 어렵다. 재현하지 못했으면 "검증 못 함"으로 보고한다.
