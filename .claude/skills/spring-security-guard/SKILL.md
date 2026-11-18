---
name: spring-security-guard
description: "GIF-server-v2의 인증·인가를 설정하고 검증한다. SecurityConfig 규칙 순서, USER/ADMIN/EXTERNAL 역할, AdminRole·ClientRole·gradeHead 세부 권한, 본인 확인, permitAll 경로의 null principal, 403 응답 코드. 엔드포인트에 권한을 정할 때, SecurityConfig나 global/security/를 바꿀 때, 예상 못 한 401·403을 디버깅할 때, 새 API가 보호되는지 리뷰할 때 사용한다."
---

# Security Guard

`spring-java-conventions`를 먼저 읽는다.

## 구조

- `global/security/SecurityConfig`: `@EnableMethodSecurity`, STATELESS, CSRF 끔. 인증 없음 401(`HttpStatusEntryPoint`), 거부 403(`JsonAccessDeniedHandler`).
- `TokenAuthenticationFilter`가 토큰에서 `AuthenticatedUser(userId, email, name, studentNumber, role, adminRole, adminTeam, gradeHead, clientRole)`를 만들어 principal로 넣는다.
- 컨트롤러는 `@AuthenticationPrincipal AuthenticatedUser user`로 받는다.

## 규칙 1 — SecurityConfig는 위에서부터 처음 맞는 규칙을 쓴다

순서가 곧 의미다. 기존 주석("아래 POST 인증 규칙보다 반드시 먼저 와야 한다")처럼, 새 규칙을 넣을 때는 **위에 있는 와일드카드가 먼저 잡지 않는지** 확인한다.

- 새 경로 → 위에서부터 읽으며 처음 맞는 규칙을 찾는다. 그게 의도한 권한인지 확인한다.
- 위험한 와일드카드: `GET /api/project/*`(authenticated, 외부 사용자 포함), `/api/items/**`(authenticated), `/api/assessment/**`(USER, EXTERNAL), `/api/notifications/**`·`/api/push/**`(authenticated).
- 아무 규칙에도 안 걸리면 `anyRequest().hasAnyRole("USER", "ADMIN")` — 외부 사용자는 403.

## 규칙 2 — 역할 계층이 없다

역할은 `USER`(재학생), `ADMIN`, `EXTERNAL`(외부 사용자). `RoleHierarchy`가 없어서 ADMIN이 USER 권한을 자동으로 갖지 않는다.

- `/api/assessment/**`는 `hasAnyRole("USER", "EXTERNAL")`이라 **ADMIN은 403**이다. 의도된 것인지 새 규칙마다 확인한다.
- `hasRole`/`hasAnyRole`에 `ROLE_` 접두어를 쓰지 않는다(`ROLE_ROLE_ADMIN`이 되어 아무도 통과 못 함).
- 외부 사용자에게 여는 API는 SecurityConfig에 명시 규칙이 있어야 한다. 기본값으로는 막힌다.

## 규칙 3 — 세부 권한은 서비스에서

`@PreAuthorize`로 표현할 수 없는 권한은 서비스에서 `AuthenticatedUser` 필드로 확인한다:

| 필드 | 값 | 예 |
|------|-----|-----|
| `adminRole` | `MAJOR_TEACHER`, `GENERAL_TEACHER`, `MASTER`, `VOID` | `adminRole() != AdminRole.MASTER`면 거부 |
| `clientRole` | `LEADER`, `MEMBER` | 팀장만 프로젝트 수정 |
| `gradeHead` | boolean | 학년부장 기능 |
| `adminTeam` | 문자열 | 담당 팀 |

거부는 `AccessDeniedException`(403) 또는 도메인 예외. 외부 사용자 거부는 `AccessDeniedCode.EXTERNAL_USER_FORBIDDEN` 메시지(`UserService`).

## 규칙 4 — 본인 확인은 로드한 뒤 서비스에서

- 리소스를 로드한 뒤 소유자 ID와 `user.userId()`를 비교한다. 다르면 **404**(존재를 숨김, PR #273).
- 목록은 조회 자체를 소유자로 좁힌다(`findAllByUserId...`). 전부 불러와 걸러내지 않는다.
- `Long` 비교는 `equals`/`Objects.equals`. `==`는 캐시 범위(-128~127) 밖에서 틀린다.

## 규칙 5 — permitAll 경로에서 principal은 null일 수 있다

`/api/project/filter`, `/api/project/{id}`, `/api/project/{id}/comments`, `POST /api/project/*/view` 등은 비로그인 호출이 가능하다. 이 경로의 컨트롤러에서 `user.userId()`를 바로 부르면 비로그인 요청이 **NPE → 500**이 된다. `user == null ? null : user.userId()`로 받는다(`ProjectController.filterProjects`).

## 새 엔드포인트 체크

1. SecurityConfig를 위에서부터 읽어 처음 맞는 규칙과 그 권한을 적는다.
2. 의도한 권한과 다르면 규칙을 추가하고, **위치**(어느 규칙보다 위)를 정한다.
3. 외부 사용자(EXTERNAL)와 ADMIN이 각각 통과하는지 따로 적는다.
4. permitAll이면 principal null 처리를 확인한다.
5. 세부 권한·본인 확인이 서비스에 있는지 확인한다.
6. 기존 권한을 넓히는 변경(누가 새로 접근하게 되는지)은 멈추고 사용자에게 확인한다.

작업 노트에 권한 표를 남긴다:

```markdown
| METHOD path | 처음 맞는 규칙 | USER | ADMIN | EXTERNAL | 비로그인 | 서비스 검사 |
```
