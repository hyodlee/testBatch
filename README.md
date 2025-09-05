# 빌드 가이드

이 프로젝트는 환경별 설정을 Maven 프로필로 관리합니다. 아래 명령으로 원하는 환경에 맞춰 빌드할 수 있습니다.

```bash
mvn -Plocal package   # 로컬 환경 빌드
mvn -Pdev package     # 개발 환경 빌드
mvn -Pprod package    # 운영 환경 빌드
```

각 프로필은 해당 환경의 `application-<프로필>.yml` 설정 파일을 사용합니다.

## STG 환경용 DDL 스크립트

migstg 데이터베이스 초기화 시 `src/script/mysql/test/2.stg_ddl-mysql.sql`을 실행해 테이블 구조를 생성합니다. STG 연결 정보는 각 환경별 `application-<프로필>.yml` 파일의 관련 항목을 참고하세요. 해당 스크립트에는 REST 호출 실패 로그 테이블(`erp_api_fail_log`)과 DB 적재 실패 로그 테이블(`erp_db_fail_log`) DDL이 포함되어 있으며, `FetchErpDataTasklet`이 DB 적재 실패 시 이 테이블에 로그를 남깁니다.

## Quartz 스키마 초기화

`src/main/resources/schema-quartz.sql`을 이용해 Quartz 테이블을 생성합니다. 애플리케이션 시작 시 `spring.sql.init.schema-locations` 설정으로 자동 실행되며, 이미 운영 DB가 있다면 스크립트를 수동 실행해 테이블을 구성해야 합니다.

## 배치 실행 기본

배치 잡 실행 시 `sourceSystem` 파라미터를 생략하면 `LND` 프리픽스로 ESNTL_ID가 생성됩니다.
잡을 반복 실행해야 할 경우 `RunIdIncrementer`를 사용하거나 실행 명령에 임의의 `JobParameters`를 추가해 Run ID를 증가시킬 수 있습니다.

## 배치 메타데이터 정리

배치 실패로 메타데이터가 남아 있는 경우에는 스텝별로 전용 정리 스크립트를 작성해 실행하세요.

> ⚠️ 운영 데이터베이스에서 실행하기 전에 반드시 전체 백업을 완료하세요.

## 배치 컨피그 파일 관계

**작성 순서**
  - context-batch-mapper.xml
  - → BatchJobLauncherConfig.java
  - → BatchSchedulerConfig.java
  - → 각 JobConfig.java

**상위→하위 참조 구조**
  - BatchSchedulerConfig.java
  - → 각 JobConfig.java
  - → BatchJobLauncherConfig.java
  - → context-batch-mapper.xml

**배치 컨피그 파일 간단설명**
  - MultiDataSourceConfig.java / BatchInfraConfig.java: 공통 데이터소스와 트랜잭션 매니저 등을 정의한다.
  - context-batch-mapper.xml: MyBatis SqlSessionFactory와 매퍼 위치를 정의한다.
  - BatchJobLauncherConfig.java: MyBatis 설정을 import하여 `JobLauncher`, `JobRepository` 등을 구성한다.
  - BatchSchedulerConfig.java: Quartz `JobDetail`, 크론 트리거, `SchedulerFactoryBean` 및 잡 체이닝을 자바 설정으로 정의한다.
  - 각 JobConfig.java: 각 업무의 배치 Job과 Step을 자바 설정으로 정의한다. `step` 안에 chunk를 사용하면 chunk 기반 Step, tasklet을 사용하면 tasklet 기반 Step이 된다.
                       BatchSchedulerConfig.java에서 참조되어 스케줄러가 실행할 Job을 결정한다.

## 기타 공통 설정

WebClient처럼 이미 자바 설정으로 작성된 클래스(`WebClientConfig`)는 좋은 예시입니다. 반복적으로 사용하는 공통 빈(예: 로깅, 글로벌 메시지)은 별도의 `@Configuration` 클래스에 배치하여 응집력을 높입니다. 예를 들어, `NotificationConfig`는 이메일 및 SMS 알림 전송기를 빈으로 등록합니다.

## Spring Batch 처리 방식: Chunk와 Tasklet

Spring Batch는 두 가지 대표적인 Step 구현 방식을 제공합니다.

- **Chunk 지향 처리**
  - ItemReader, ItemProcessor, ItemWriter를 조합하여 데이터를 일정 크기(chunk) 단위로 읽고 처리한 뒤 한 번에 커밋합니다.
  - 대량 데이터 처리에 적합하며, 트랜잭션은 각 chunk마다 걸립니다.

- **Tasklet 기반 처리**
  - 단일 Tasklet을 실행하는 간단한 Step 구조로, 반복이 필요 없는 작업에 적합합니다.
  - 파일 이동, 디렉터리 정리 등 단순 작업을 구현할 때 사용합니다.

### StepCountLogger 활용

`StepCountLogger`는 스텝 종료 후 읽기·쓰기·스킵 건수를 로그로 남기는 `StepExecutionListener`입니다. 클래스 경로: `src/main/java/egovframework/bat/job/insa/listener/StepCountLogger.java`

#### Chunk 기반 스텝에서의 사용

Chunk 스텝에서는 `StepCountLogger`를 리스너로 등록하기만 하면 자동으로 처리 건수가 기록됩니다.

```java
@Bean
public Step sampleStep(StepBuilderFactory stepBuilderFactory,
                       StepCountLogger stepCountLogger) {
    return stepBuilderFactory.get("sampleStep")
            .<Input, Output>chunk(100)
            .reader(reader())              // 아이템 읽기
            .processor(processor())        // 아이템 가공
            .writer(writer())              // 아이템 쓰기
            .listener(stepCountLogger)     // StepExecutionListener 등록
            .build();
}
```

#### Tasklet 기반 스텝에서의 사용

Tasklet 스텝은 `StepContribution`의 카운트를 자동 증가시키지 않으므로, 처리 건수를 수동으로 갱신해야 `StepCountLogger`가 정확한 값을 출력합니다.

```java
public class SampleTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {
        contribution.incrementReadCount();      // 읽은 건수 +1
        // 비즈니스 로직 수행
        contribution.incrementWriteCount(1);    // 쓴 건수 +1
        return RepeatStatus.FINISHED;
    }
}
```


## 인사 배치 잡 디렉터리(`insa`)

`src/main/java/egovframework/bat/job/insa/config` 디렉터리는 인사 관련 배치 Job 설정 클래스를 모아두는 곳입니다. 현재 포함된 Job은 다음과 같습니다.

- `insaRemote1ToStgJob`
- `insaStgToLocalJob`

다음은 관련된 주요 파일들입니다.

- 잡 설정 클래스
  - `src/main/java/egovframework/bat/job/insa/config/InsaRemote1ToStgJobConfig.java`
  - `src/main/java/egovframework/bat/job/insa/config/InsaStgToLocalJobConfig.java`
- 매퍼 파일
  - `src/main/resources/egovframework/batch/mapper/job/insa/insa_remote1_to_stg.xml`
  - `src/main/resources/egovframework/batch/mapper/job/insa/insa_stg_to_local.xml`
- 공통·도메인·유틸 클래스
  - `src/main/java/egovframework/bat/job/insa/common/SourceSystemPrefix.java`
  - `src/main/java/egovframework/bat/job/insa/processor/EmployeeInfoProcessor.java`
  - `src/main/java/egovframework/bat/job/insa/common/EsntlIdGenerator.java`
  - `src/main/java/egovframework/bat/job/insa/domain/EmployeeInfo.java`
  - `src/main/java/egovframework/bat/job/insa/domain/Orgnztinfo.java`
- 테스트 코드
  - `src/test/java/egovframework/bat/job/insa/common/EsntlIdGeneratorTest.java`

### 인사 배치 Job 추가시 확인 사항

새로운 인사 배치 Job을 추가할 때는 다음 규칙을 지킵니다.

- 설정 클래스: `src/main/java/egovframework/bat/job/insa/config`에 `<Source>To<Target>JobConfig.java` 형태로 작성합니다. 클래스명은 UpperCamelCase를 사용하며 반드시 `JobConfig`로 끝납니다.
- 관련 도메인 클래스: `src/main/java/egovframework/bat/job/insa/domain` 아래에 작성하고 패키지 구조를 유지합니다.
- 공통 클래스: `src/main/java/egovframework/bat/job/insa/common` 아래에 작성하고 패키지 구조를 유지합니다.
- 테스트 코드: `src/test/java/egovframework/bat/job/insa/domain` 및 `src/test/java/egovframework/bat/job/insa/common`에 동일한 패키지 구조로 작성합니다.

## ERP 배치 잡 디렉터리(`erp`)

`src/main/java/egovframework/bat/job/erp/config` 디렉터리는 ERP 관련 배치 Job 설정 클래스를 모아두는 곳입니다. 현재 포함된 Job은 다음과 같습니다.

- `erpRestToStgJob`
- `erpStgToLocalJob`
- `erpStgToRestJob`

다음은 관련된 주요 파일들입니다.

- 잡 설정 클래스
  - `src/main/java/egovframework/bat/job/erp/config/ErpRestToStgJobConfig.java`
  - `src/main/java/egovframework/bat/job/erp/config/ErpStgToLocalJobConfig.java`
  - `src/main/java/egovframework/bat/job/erp/config/ErpStgToRestJobConfig.java`
  - `src/main/java/egovframework/bat/job/erp/config/ErpFailLogTableInitializer.java`
- 매퍼 파일
  - `src/main/resources/egovframework/batch/mapper/job/erp/erp_rest_to_stg.xml`
  - `src/main/resources/egovframework/batch/mapper/job/erp/erp_stg_to_local.xml`
- 공통·도메인·유틸 클래스
  - `src/main/java/egovframework/bat/job/erp/domain/VehicleInfo.java`
  - `src/main/java/egovframework/bat/job/erp/processor/VehicleInfoProcessor.java`
  - `src/main/java/egovframework/bat/job/erp/exception/ErpApiException.java`
  - `src/main/java/egovframework/bat/job/erp/tasklet/FetchErpDataTasklet.java`
  - `src/main/java/egovframework/bat/job/erp/tasklet/SendErpDataTasklet.java`
  - `src/main/java/egovframework/bat/job/erp/tasklet/TruncateErpVehicleTasklet.java`
- 테스트 코드
  - `src/test/java/egovframework/bat/job/erp/tasklet/FetchErpDataTaskletTest.java`
  - `src/test/java/egovframework/bat/job/erp/tasklet/FetchErpDataTaskletPropertyInjectionTest.java`
  - `src/test/java/egovframework/bat/job/erp/api/VehicleControllerTest.java`
  - `src/test/java/egovframework/bat/job/erp/api/DummyErpApiInfoTest.java`

## EXAMPLE 배치 잡 디렉터리(`example`)

`src/main/java/egovframework/bat/job/example/config` 디렉터리는 예제 배치 Job 설정 클래스를 모아둔 곳입니다. 예제 Job과 관련된 주요 파일은 다음과 같습니다.

- `src/main/java/egovframework/bat/job/example/config/MybatisToMybatisJobConfig.java`: MyBatis 간 데이터 이동을 정의한 배치 Job 설정 클래스(잡 ID: `mybatisToMybatisSampleJob`)
- `src/main/resources/egovframework/batch/mapper/job/example/Egov_Example_SQL.xml`: 예제 배치를 위한 SQL 매퍼 파일
- `src/main/java/egovframework/bat/job/example/domain/CustomerCredit.java`: 고객 신용 정보를 담는 도메인 클래스
- `src/main/java/egovframework/bat/job/example/processor/CustomerCreditIncreaseProcessor.java`: 신용 증가 로직을 처리하는 배치 프로세서
- `src/main/java/egovframework/bat/scheduler/EgovQuartzJobLauncher.java`: Quartz 스케줄러에서 배치 Job을 실행하는 클래스
- `src/main/resources/egovframework/batch/context-batch-mapper.xml`: 예제 SQL 매퍼와 데이터소스가 등록된 설정 파일


### 예제 배치 잡 실행 API

`ExampleJobController`를 통해 MyBatis 예제 잡을 REST로 호출할 수 있습니다.

- **URL**: `POST /api/batch/mybatis`
- **파라미터**: `userId` (선택)
- **응답**: 실행 결과 `BatchStatus`

### ERP 배치 잡 실행 API

`RestToStgJobController`와 `StgToRestJobController`를 통해 ERP 배치를 REST로 호출할 수 있습니다.

- **URL**: `POST /api/batch/erp-rest-to-stg`
- **파라미터**: 없음
- **응답**: 실행 결과 `BatchStatus`

- **URL**: `POST /api/batch/erp-stg-to-rest`
- **파라미터**: 없음
- **응답**: 실행 결과 `BatchStatus`

### 인사 배치 잡 실행 API

`Remote1ToStgJobController`를 통해 인사 배치를 REST로 호출할 수 있습니다.

- **URL**: `POST /api/batch/remote1-to-stg`
- **파라미터**: `sourceSystem` (선택)
- **응답**: 실행 결과 `BatchStatus`

## 컨트롤러별 역할과 주소

| 컨트롤러 | 역할 요약 | 기본 URL | 주요 하위 경로 |
| --- | --- | --- | --- |
| BatchApiController | 배치 잡 목록 조회, 실행 이력/상세, 에러 로그, 재시작·중지 등 관리 API 제공 | `/api/batch` | `/jobs`, `/jobs/{jobName}/executions`, `/executions/{execId}`, `/error-log`, `/jobs/{jobName}/restart`, `/jobs/{jobName}` |
| BatchManagementController | DTO 기반 관리용 API로 잡 목록·이력·에러 로그·재시작/중지 기능 제공 | `/api/batch/management` | `/jobs`, `/jobs/{jobName}/executions`, `/executions/{jobExecutionId}/errors`, `/jobs/{jobName}/restart`, `/jobs/{jobName}/stop` |
| JobProgressController | 배치 진행 상황을 SSE 스트림으로 전송 | `/api/batch` | `/progress` |
| BatchPageController | 배치 작업 리스트·상세·로그 화면을 렌더링 | `/batch` | `/list`, `/detail`, `/log` |
| JobRunController | 공통 잡 실행 엔드포인트로 지정된 잡을 실행 | `/api/batch` | `/run` |
| Remote1ToStgJobController | Remote1 데이터를 STG로 적재하는 배치 실행 | `/api/batch` | `/remote1-to-stg` |
| ExampleJobController | 마이바티스 예제 배치 잡을 실행 | `/api/batch` | `/mybatis` |
| StgToRestJobController | STG 데이터를 외부 REST API로 전송 | `/api/batch` | `/erp-stg-to-rest` |
| RestToStgJobController | ERP REST 데이터를 STG 테이블로 적재 | `/api/batch` | `/erp-rest-to-stg` |
| VehicleController | ERP 서비스용 차량 정보를 조회 | `/api/v1` | `/vehicles` |

## API 엔드포인트

### 일반 배치 관리 API
- `GET /api/batch/jobs` – 등록된 배치 작업 목록 조회
- `GET /api/batch/jobs/{jobName}/executions` – 특정 작업의 실행 이력 조회
- `GET /api/batch/executions/{execId}` – 특정 실행 ID 상세 조회
- `GET /api/batch/error-log` – 배치 에러 로그 조회

### 배치 실행 제어 API
- `POST /api/batch/executions/{execId}/restart` – 지정 실행 ID 재시작
- `DELETE /api/batch/executions/{execId}` – 실행 중인 배치 중지
- `GET /api/batch/progress` (SSE) – 배치 진행 상태 스트림

### 개별 Job 실행 API
- `POST /api/batch/erp-rest-to-stg` – ERP REST → STG 전송 배치 실행
- `POST /api/batch/erp-stg-to-rest` – STG → ERP REST 전송 배치 실행
- `POST /api/batch/mybatis` – MyBatis 기반 배치 실행
- `POST /api/batch/remote1-to-stg` – Remote1 → STG 전송 배치 실행

### 관리용 배치 API
- `GET /api/batch/management/jobs` – 관리용 배치 작업명 목록
- `GET /api/batch/management/jobs/{jobName}/executions` – 관리용 작업 실행 이력 조회
- `GET /api/batch/management/executions/{jobExecutionId}/errors` – 특정 실행 ID 에러 로그 조회
- `POST /api/batch/management/executions/{jobExecutionId}/restart` – 관리용 배치 재시작
- `POST /api/batch/management/executions/{jobExecutionId}/stop` – 관리용 실행 중지

### 도메인 데이터 조회 API
- `GET /api/v1/vehicles` – 차량 목록 조회

### 페이지(HTML) 엔드포인트
- `GET /batch/detail` – 배치 상세 페이지
- `GET /batch/list` – 배치 목록 페이지
- `GET /error` – 오류 페이지

더 자세한 설명은 각 컨트롤러 소스 코드를 참고하세요.

## 완전 새로운 배치 작업 추가시 매뉴얼

1. Job 설정 클래스 작성: `src/main/java/egovframework/bat/crm/config/NewcrmJobConfig.java` - 새 작업의 단계와 흐름을 정의합니다.
2. 매퍼 XML 작성: `src/main/resources/egovframework/batch/mapper/crm/` 아래에 매퍼 XML을 생성해 데이터 조회와 저장 SQL을 작성합니다.
3. 도메인 클래스 생성: `src/main/java/egovframework/bat/crm/domain/Newcrm.java` - 배치에서 사용할 데이터 구조를 정의합니다.
4. 프로세서 클래스 구현: `src/main/java/egovframework/bat/crm/processor/NewcrmProcessor.java` - 도메인 데이터를 가공하는 로직을 구현합니다.
5. (선택) 테스트 코드 추가: `src/test/java/egovframework/bat/crm/processor/NewcrmProcessorTest.java` - 주요 기능이 예상대로 동작하는지 검증합니다.

예시 파일 구조:

- `src/main/java/egovframework/bat/crm/config/NewcrmJobConfig.java`
- `src/main/java/egovframework/bat/crm/domain/Newcrm.java`
- `src/main/java/egovframework/bat/crm/processor/NewcrmProcessor.java`
- `src/test/java/egovframework/bat/crm/processor/NewcrmProcessorTest.java`

CRM 배치는 신규 시스템 추가시의 예시입니다

## 배치 잡 임시 비활성화 방법

* **크론 기반 잡**: `application.yml`의 `scheduler.jobs` 항목에서 해당 잡의 크론 표현식을 주석 처리하거나 삭제하면 CronTrigger가 생성되지 않는다.
* **JobChain으로 연결된 잡(예: `insaStgToLocalJob`)**: `application.yml`에서 잡 설정을 비활성화한 뒤, `BatchSchedulerConfig`의 `JobChainingJobListener`에서 체인 링크를 제거하거나 주석 처리해 연쇄 실행을 막는다.

## 스프링 배치 순차 실행 요약

1. 스프링 배치는 Job → Step 구조를 기반으로 한다.
2. 하나의 Job 안에서 Step들을 순서대로 정의해 실행할 수 있다.
3. Step1 완료 후 Step2, Step2 완료 후 Step3처럼 순차 실행이 가능하다.
4. 여러 Job을 만들어 Job 간에도 순차 실행을 구성할 수 있다.
5. JobLauncher나 JobStep을 사용하면 다른 Job을 Step처럼 호출할 수 있다.
6. Step의 성공/실패 결과에 따라 다음 Step을 분기 처리할 수 있다.
7. XML 설정과 Java Config 방식을 모두 지원하나, 본 프로젝트는 Java Config 방식을 사용한다.
8. 조건부 로직으로 유연한 배치 흐름 제어가 가능하다.
9. 기본 기능만으로도 단순한 순차 실행부터 복잡한 플로우 구성까지 가능하다.
10. 재사용성과 확장성이 높아 대규모 배치 처리에 적합하다.

※ ID이름이 중복되지 않게 신경쓸것. (예로 step빈의 ID가 다른 업무와 같게 구성할경우, 에러가 발생하지 않는데, 그 작업이 실행되지 않는 현상을 경험할 수 있음)

