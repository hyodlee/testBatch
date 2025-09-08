관리(Management) 관련 HTTP 주소
BatchManagementController – 공통 경로 /api/management/batch
GET /api/management/batch/jobs – 등록된 배치 잡 이름 목록 조회
GET /api/management/batch/jobs/{jobName}/executions – 특정 잡의 실행 이력 조회
GET /api/management/batch/executions/{jobExecutionId}/errors – 특정 실행 ID의 에러 로그 조회
POST /api/management/batch/jobs/{jobName}/restart – 실패한 잡 재실행
POST /api/management/batch/jobs/{jobName}/stop – 실행 중인 잡 중지

SchedulerManagementController – 공통 경로 /api/management/scheduler
POST /api/management/scheduler/jobs – 새로운 잡 추가
POST /api/management/scheduler/jobs/{jobName}/pause – 지정한 잡 일시 중지
POST /api/management/scheduler/jobs/{jobName}/resume – 일시 중지된 잡 재개
POST /api/management/scheduler/jobs/{jobName}/delete – 등록된 잡 삭제
POST /api/management/scheduler/jobs/{jobName}/cron – 잡의 크론 표현식 변경
GET /api/management/scheduler/jobs – 모든 잡 정보 조회
GET /api/management/scheduler/jobs/{jobName} – 특정 잡 정보 조회

JobProgressController – 공통 경로 /api/management/batch
GET /api/management/batch/progress – 배치 진행 상황 SSE 스트림 제공

관리 관련 Java 패키지/폴더 구조
src/main/java/egovframework/bat/management
├─ 주요 클래스: JobProgressService, SchedulerManagementController, SchedulerManagementService
├─ api 패키지: 관리용 REST 컨트롤러 (BatchManagementController, JobProgressController)
├─ dto 패키지: CronRequest, JobProgress, ScheduledJobDto 등 DTO 모음
└─ exception 패키지: 스케줄러 제어 관련 예외 클래스들
(DurableJobCronUpdateNotAllowedException, DurableJobPauseResumeNotAllowedException, InvalidCronExpressionException)

테스트 코드
├─ src/test/java/egovframework/bat/management/SchedulerManagementServiceTest.java 등 서비스 테스트
└─ src/test/java/egovframework/bat/management/dto/CronRequestTest.java 등 DTO 검증 테스트

위와 같이 관리 기능은 management 패키지 아래에 API·DTO·예외 등으로 명확히 분리되어 있으며, 
다양한 HTTP 엔드포인트를 통해 배치 및 스케줄러를 제어하도록 구성되어 있음

관리 UI 리소스 분리
src/management-ui
├─ templates/batch, templates/scheduler: 배치·스케줄러 화면용 템플릿
├─ static/index.html: 관리 콘솔 진입점
└─ static/js: 관리 화면 전용 JavaScript 파일

위 리소스는 pom.xml에 별도 리소스 경로로 등록되어 있으며
application.yml의 `spring.thymeleaf.prefix`와
`spring.web.resources.static-locations` 설정을 통해 서빙된다.
빌드 후 관리 콘솔은 `/index.html`로 접근하여 사용할 수 있다.
