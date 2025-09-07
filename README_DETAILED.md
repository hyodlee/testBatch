# Java 파일의 간단 역할 설명

## com.springboot.main
EgovBootApplication.java : 스프링 부트 애플리케이션 진입점으로 컴포넌트 및 매퍼 스캔 설정

## egovframework.bat.api
BatchApiController.java : 배치 잡 목록 조회, 실행 이력·상세·에러 로그 확인 및 재시작·중지 기능 제공

## egovframework.bat.notification
NotificationSender.java : 장애 알림 메시지 전송을 위한 인터페이스
EmailNotificationSender.java : 메일 알림 전송 구현체
SmsNotificationSender.java : SMS 알림 전송 구현체

## egovframework.bat.service
JobExecutionDto.java : 배치 잡 실행 정보를 담는 DTO
BatchManagementService.java : 잡 목록·이력 조회와 재시작·중지 등 배치 관리 서비스
JobLockService.java : 배치 작업 중복 실행을 막는 락 서비스
BatchManagementMapper.java : 잡 이름·실행·에러 로그 조회 매퍼

## egovframework.bat.management.api
BatchManagementController.java : 등록된 잡과 실행 이력·에러 로그 조회, 재시작·중지 API 제공
JobProgressController.java : 배치 진행 상황을 SSE 스트림으로 전달

## egovframework.bat.management.dto
JobProgress.java : 작업명과 상태를 담는 진행 상황 DTO
ScheduledJobDto.java : 잡 이름·크론 표현식·상태를 담는 스케줄 DTO

## egovframework.bat.management
JobProgressService.java : Reactor Sinks로 진행 상황을 전송하는 서비스
SchedulerManagementService.java : Quartz 잡 추가·수정·삭제·조회 서비스
SchedulerManagementController.java : 스케줄러 잡 관리 REST API

## egovframework.bat.job.erp.api
VehicleController.java : ERP용 차량 정보를 제공하는 조회 API
StgToRestJobController.java : STG 데이터를 외부 REST API로 전송하는 잡 실행
RestToStgJobController.java : ERP REST 데이터를 STG 테이블로 적재하는 잡 실행

## egovframework.bat.job.erp.exception
ErpApiException.java : ERP API 호출 오류를 나타내는 예외

## egovframework.bat.job.erp.processor
VehicleInfoProcessor.java : 차량 정보 후처리 프로세서(현재는 패스스루)

## egovframework.bat.job.erp.config
ErpFailLogTableInitializer.java : ERP 연동 실패 로그 테이블을 생성
ErpRestToStgJobConfig.java : ERP REST→STG 적재 잡 구성
ErpStgToLocalJobConfig.java : STG 차량 데이터를 로컬 DB로 이관하는 잡 구성
ErpStgToRestJobConfig.java : STG 데이터를 외부 REST API로 전송하는 잡 구성

## egovframework.bat.job.erp.domain
VehicleInfo.java : ERP 차량 정보를 담는 VO

## egovframework.bat.job.erp.tasklet
TruncateErpVehicleTasklet.java : STG 차량 테이블을 비우는 Tasklet
SendErpDataTasklet.java : STG DB 차량을 외부 API로 전송하고 실패 시 알림
FetchErpDataTasklet.java : ERP API에서 차량을 조회해 STG 테이블에 저장

## egovframework.bat.job.example.api
ExampleJobController.java : 마이바티스 샘플 배치 잡을 실행

## egovframework.bat.job.example.processor
CustomerCreditIncreaseProcessor.java : 고객 크레딧을 고정 금액만큼 증가

## egovframework.bat.job.example.config
MybatisToMybatisJobConfig.java : remote1에서 STG로 데이터를 옮기는 MyBatis→MyBatis 잡 구성

## egovframework.bat.job.example.domain
CustomerCredit.java : 고객 크레딧 엔티티와 증가 로직

## egovframework.bat.job.insa.api
Remote1ToStgJobController.java : Remote1 데이터를 STG로 적재하는 잡 실행

## egovframework.bat.job.insa.config
InsaStgToLocalJobConfig.java : STG 조직·사원 정보를 로컬 DB로 이관하는 잡 구성
InsaRemote1ToStgJobConfig.java : Remote1 조직·사원 데이터를 STG로 이관하는 잡 구성

## egovframework.bat.job.insa.listener
StepCountLogger.java : 스텝 처리 건수를 로그로 남기는 리스너

## egovframework.bat.job.insa.domain
EmployeeInfo.java : 직원 정보를 담는 VO
Orgnztinfo.java : 조직 정보를 담는 VO

## egovframework.bat.job.insa.tasklet
TruncateStgTablesTasklet.java : STG 조직·사원 테이블을 비우는 Tasklet
StgToLocalEmployeeTasklet.java : STG와 로컬 DB의 사원 정보를 동기화하는 Tasklet

## egovframework.bat.job.common.api
JobRunController.java : 공통 잡 실행 엔드포인트로 지정된 잡을 실행

## egovframework.bat.web
SchedulerPageController.java : 스케줄러 관리 페이지 뷰 제공
BatchPageController.java : 배치 리스트·상세·로그 페이지 뷰 제공

## egovframework.bat.config
BatchSchedulerConfig.java : Quartz 스케줄러를 동적으로 설정하고 JobChaining 리스너 구성
NotificationConfig.java : 이메일·SMS 알림 전송기 빈 등록
BatchInfraConfig.java : 배치용 트랜잭션 매니저와 LobHandler 설정
MultiDataSourceConfig.java : STG·로컬·Remote1 데이터소스 및 JdbcTemplate 구성
WebClientConfig.java : WebClient 빌더 빈 등록
AutowiringJobFactory.java : Quartz Job에 스프링 빈 자동 주입
BatchJobLauncherConfig.java : 배치 JobLauncher, Repository, Explorer 등 기본 설정
SchedulerProps.java : application.yml의 스케줄러 잡 설정 바인딩 클래스

## egovframework.bat.scheduler
EgovQuartzJobLauncher.java : Quartz에서 스프링 배치 잡을 실행하고 진행 상황을 전송

## egovframework.bat.repository.dto
BatchJobInstanceDto.java : 배치 잡 인스턴스 정보를 담는 DTO
SchedulerJobDto.java : 스케줄러 잡 정보 DTO
BatchJobExecutionDto.java : 배치 잡 실행 정보를 담는 DTO
BatchStepExecutionDto.java : 배치 스텝 실행 정보를 담는 DTO

## egovframework.bat.repository
BatchMetadataMapper.java : 배치 메타데이터 조회용 MyBatis 매퍼

## 테스트 코드
BatchManagementServiceTest.java : BatchManagementService 단위 테스트
VehicleControllerTest.java : VehicleController 차량 목록 API 테스트
DummyErpApiInfo.java : ERP API 테스트용 더미 엔드포인트 상수
DummyErpApiInfoTest.java : 더미 ERP 응답 파일과 엔드포인트 상수 검증
FetchErpDataTaskletTest.java : FetchErpDataTasklet의 원격/DB 실패 처리 테스트
FetchErpDataTaskletPropertyInjectionTest.java : FetchErpDataTasklet의 프로퍼티 주입 검증
InsaStgToLocalJobIntegrationTest.java : STG→로컬 동기화 배치 통합 테스트(갱신·삽입 확인)
InsaStgToLocalJobEmptyLocalIntegrationTest.java : 로컬이 비어 있을 때 ESNTL_ID 생성 검증 통합 테스트
