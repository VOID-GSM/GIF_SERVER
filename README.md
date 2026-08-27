# GIF (GSM Idea Festival)
GIF는 광주소프트웨어마이스터고등학교 아이디어 페스티벌의 전체 운영 과정을 하나의 플랫폼에서 효율적으로 관리하기 위한 서비스입니다. 기존의 수기 방식에서 벗어나, 학생(Client)의 팀 프로젝트 관리·자료 제출부터 관리자(Admin)의 양식 생성·제출 현황 추적·평가·점수 집계까지 모든 워크플로우를 통합합니다.

이 저장소는 GIF의 백엔드 API 서버입니다. 프론트엔드(학생/관리자 웹)는 [GIF_WAB](https://github.com/VOID-GSM/GIF_WAB) 저장소에서 관리됩니다.

## 주요 기능

- **인증**: 구글(Google) 및 DGSM(DataGSM) OAuth 로그인
- **프로젝트 관리**: 팀 생성/수정, 팀원 관리, 팀장 양도, 링크 등록, AI 요약
- **양식(Form) 관리**: 양식 생성·수정·공지, 학년/파일 확장자 제한 설정, 제출 및 파일 업로드, 마감 준수 여부 자동/수동 관리, AI 요약
- **점수 관리**: 프로젝트 점수 산정 및 집계
- **공지사항**: 학년/팀 태그 기반 공지, 디스코드 봇 연동 알림
- **문의하기**: 학생 문의 등록·수정·조회
- **푸시 알림**: Web Push 기반 실시간 알림 (마감 임박, 팀원 변경, 점수 공지 등)
- **AI 요약**: OpenAI(GPT-4o-mini)를 활용한 프로젝트/제출 내용 자동 요약

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language & Framework | Java, Spring Boot |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL / MariaDB |
| Cache | Redis |
| Auth | Spring Security, OAuth2 (Google, DGSM), JWT |
| AI | OpenAI API (GPT-4o-mini) |
| Notification | Web Push (VAPID), Discord Bot (JDA) |
| API 문서 | Springdoc OpenAPI (Swagger UI) |
| Build Tool | Gradle |

## 시작하기

### 요구 사항

- JDK 17 이상
- MySQL / MariaDB
- Redis


### 설치 및 실행

```bash
git clone https://github.com/VOID-GSM/GIF-server-v2.git
cd GIF-server-v2
./gradlew build -x test
./gradlew bootRun
```

기본 포트는 `8080`이며, 서버 실행 후 아래 경로에서 API 문서를 확인할 수 있습니다.

http://localhost:8080/swagger-ui.html

