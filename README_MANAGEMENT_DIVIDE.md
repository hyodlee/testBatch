# 관리용 Web UI 분리와 API 통신 가이드

## 1. 분리 전략
- 서비스용 UI와 관리용 UI를 서로 다른 빌드(또는 패키지)로 분리한다.
- 관리 UI는 `src/management-ui` 아래 리소스를 사용해 별도 번들로 빌드하고,
  `/management/` 서브 경로나 독립 도메인에 배포한다.
- `pom.xml`의 리소스 경로, `spring.thymeleaf.prefix`,
  `spring.web.resources.static-locations` 설정을 관리 UI 전용으로 조정한다.

## 2. API 통신 방식
- REST 엔드포인트  
  - 배치 잡 목록 조회: `GET /api/management/batch/jobs`  
  - 배치 잡 재실행: `POST /api/management/batch/jobs/{jobName}/restart`  
  - 스케줄러 잡 추가: `POST /api/management/scheduler/jobs` 등
- SSE 통신  
  - 배치 진행 상황: `GET /api/management/batch/progress`로 SSE 스트림 연결
- 인증/보안  
  - API 키 또는 토큰 기반 인증 적용  
  - 별도 도메인일 경우 CORS 허용 혹은 리버스 프록시 설정

## 3. 디렉터리 구조 제안
```
src/
management-ui/
src/            # 관리 콘솔 전용 소스
public/         # 빌드 산출물
```

## 4. 배포 및 운영
- CI에서 관리 UI를 별도 빌드하여 정적 파일로 생성
- 운영 서버의 `/management/` 또는 독립 호스트에 정적 파일 배포
- API 서버는 `/api/management/**` 경로만 노출되도록 네트워크/보안 설정
