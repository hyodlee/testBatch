# 빌드 가이드

이 프로젝트는 환경별 설정을 Maven 프로필로 관리합니다. 아래 명령을 사용하여 원하는 환경으로 빌드할 수 있습니다.

```bash
mvn -Plocal package   # 로컬 환경 빌드
mvn -Pdev package     # 개발 환경 빌드
mvn -Pprod package    # 운영 환경 빌드
```

각 프로필은 해당 환경의 `globals-<프로필>.properties` 파일을 사용하여 `globals.properties`를 생성합니다.

## STG 환경용 DDL 스크립트

migstg 데이터베이스 초기화 시 `src/script/mysql/test/2.stg_ddl-mysql.sql`을 실행해 테이블 구조를 생성합니다. STG 연결 정보는 각 환경별 `globals.properties` 파일의 `Globals.Stg.*` 항목을 참고하세요.

배치 잡 실행 시 `sourceSystem` 파라미터를 생략하면 `LND` 프리픽스로 ESNTL_ID가 생성됩니다.

## Spring Batch 처리 방식: Chunk와 Tasklet

Spring Batch는 두 가지 대표적인 Step 구현 방식을 제공합니다.

- **Chunk 지향 처리**
  - ItemReader, ItemProcessor, ItemWriter를 조합하여 데이터를 일정 크기(chunk) 단위로 읽고 처리한 뒤 한 번에 커밋합니다.
  - 대량 데이터 처리에 적합하며, 트랜잭션은 각 chunk마다 걸립니다.

- **Tasklet 기반 처리**
  - 단일 Tasklet을 실행하는 간단한 Step 구조로, 반복이 필요 없는 작업에 적합합니다.
  - 파일 이동, 디렉터리 정리 등 단순 작업을 구현할 때 사용합니다.

## 인사 배치 잡 디렉터리(`insa`)

`src/main/resources/egovframework/batch/job/insa` 디렉터리는 인사 관련 배치 Job 설정을 모아두는 곳입니다. 현재 포함된 Job은 다음과 같습니다.

- `insaRemote1ToStgJob`
- `insaStgToLocalJob`

다음은 관련된 주요 파일들입니다.

- 잡 설정:
  - `src/main/resources/egovframework/batch/job/insa/insaRemote1ToStgJob.xml`: 원격 시스템에서 스테이징으로 데이터를 전송하는 Job 설정 파일
  - `src/main/resources/egovframework/batch/job/insa/insaStgToLocalJob.xml`: 스테이징에서 로컬로 데이터를 이동하는 Job 설정 파일
- 매퍼 파일:
  - `src/main/resources/egovframework/batch/mapper/insa/insa_remote1_to_stg.xml`: 원격→스테이징 데이터 이동을 위한 SQL 매퍼
  - `src/main/resources/egovframework/batch/mapper/insa/insa_stg_to_local.xml`: 스테이징→로컬 데이터 이동을 위한 SQL 매퍼
- 공통, 도메인 및 유틸 클래스:
  - `src/main/java/egovframework/bat/insa/util/SourceSystemPrefix.java`: 시스템 구분을 위한 접두어 상수 정의 클래스
  - `src/main/java/egovframework/bat/insa/processor/EmployeeInfoProcessor.java`: 직원 정보를 처리하는 배치 프로세서
  - `src/main/java/egovframework/bat/insa/util/EsntlIdGenerator.java`: ESNTL_ID를 생성하는 유틸리티 클래스
  - `src/main/java/egovframework/bat/insa/domain/EmployeeInfo.java`: 직원 정보를 담는 도메인 클래스
  - `src/main/java/egovframework/bat/insa/domain/Orgnztinfo.java`: 조직 정보를 표현하는 도메인 클래스
- 테스트 코드:
  - `src/test/java/egovframework/bat/insa/util/EsntlIdGeneratorTest.java`: ESNTL_ID 생성 로직을 검증하는 테스트

### 인사 배치 Job 추가시 확인 사항

새로운 인사 배치 Job을 추가할 때는 다음 규칙을 지킵니다.

- 설정 파일: `src/main/resources/egovframework/batch/job/insa`에 `<Source>To<Target>Job.xml` 형태로 저장합니다. 파일명은 lowerCamelCase를 사용하며 반드시 `Job.xml`으로 끝납니다.
- 관련 도메인 클래스: `src/main/java/egovframework/bat/insa/domain` 아래에 작성하고 패키지 구조를 유지합니다.
- 공통 클래스: `src/main/java/egovframework/bat/insa/util` 아래에 작성하고 패키지 구조를 유지합니다.
- 테스트 코드: `src/test/java/egovframework/bat/insa/domain` 및 `src/test/java/egovframework/bat/insa/util`에 동일한 패키지 구조로 작성합니다.

## ERP 배치 잡 디렉터리(`erp`)

`src/main/resources/egovframework/batch/job/erp` 디렉터리는 ERP 관련 배치 Job 설정을 모아두는 곳입니다. 현재 포함된 Job은 다음과 같습니다.

- `erpRestToStgJob`
- `erpStgToLocalJob`

다음은 관련된 주요 파일들입니다.

- 잡 설정:
  - `src/main/resources/egovframework/batch/job/erp/erpRestToStgJob.xml`: ERP REST API에서 데이터를 조회하여 STG에 적재하는 Job 설정 파일
  - `src/main/resources/egovframework/batch/job/erp/erpStgToLocalJob.xml`: STG에 적재된 ERP 데이터를 로컬 DB로 이관하는 Job 설정 파일
- 매퍼 파일:
  - `src/main/resources/egovframework/batch/mapper/erp/erp_rest_to_stg.xml`: ERP REST 데이터→STG 적재를 위한 SQL 매퍼
  - `src/main/resources/egovframework/batch/mapper/erp/erp_stg_to_local.xml`: STG→로컬 데이터 이동을 위한 SQL 매퍼
- 공통, 도메인 및 유틸 클래스:
  - `src/main/java/egovframework/bat/erp/tasklet/FetchErpDataTasklet.java`: ERP 시스템에서 차량 정보를 조회하여 STG에 적재하는 Tasklet
  - `src/main/java/egovframework/bat/erp/processor/VehicleInfoProcessor.java`: ERP 차량 정보를 처리하는 배치 프로세서
  - `src/main/java/egovframework/bat/erp/domain/VehicleInfo.java`: ERP 차량 정보를 담는 도메인 클래스
  - `src/main/java/egovframework/bat/erp/api/RestToStgJobController.java`: ERP REST 배치를 수동 실행하는 컨트롤러

## 예제 배치 잡 디렉터리(`example`)

`src/main/resources/egovframework/batch/job/example` 디렉터리는 예제 배치 Job 설정을 모아둔 곳입니다. 예제 Job과 관련된 주요 파일은 다음과 같습니다.

- `src/main/resources/egovframework/batch/job/example/mybatisToMybatisJob.xml`: MyBatis 간 데이터 이동을 정의한 배치 Job 설정 파일(잡 ID: `mybatisToMybatisSampleJob`)
- `src/main/resources/egovframework/batch/mapper/example/Egov_Example_SQL.xml`: 예제 배치를 위한 SQL 매퍼 파일
- `src/main/java/egovframework/bat/example/domain/CustomerCredit.java`: 고객 신용 정보를 담는 도메인 클래스
- `src/main/java/egovframework/bat/example/processor/CustomerCreditIncreaseProcessor.java`: 신용 증가 로직을 처리하는 배치 프로세서
- `src/main/java/egovframework/bat/scheduler/EgovQuartzJobLauncher.java`: Quartz 스케줄러에서 배치 Job을 실행하는 클래스
- `src/main/resources/egovframework/batch/context-batch-mapper.xml`: 예제 SQL 매퍼와 데이터소스가 등록된 설정 파일
- `src/main/resources/egovframework/batch/context-scheduler-job.xml`: 예제 Job을 스케줄러에 등록하기 위한 설정 파일

### 예제 배치 잡 실행 API

`ExampleJobController`를 통해 MyBatis 예제 잡을 REST로 호출할 수 있습니다.

- **URL**: `POST /api/batch/mybatis`
- **파라미터**: `userId` (선택)
- **응답**: 실행 결과 `BatchStatus`

### ERP 배치 잡 실행 API

`RestToStgJobController`를 통해 ERP 배치를 REST로 호출할 수 있습니다.

- **URL**: `POST /api/batch/erp-rest-to-stg`
- **파라미터**: 없음
- **응답**: 실행 결과 `BatchStatus`

### 인사 배치 잡 실행 API

`insaRemote1ToStgJob`을 REST로 호출할 수 있습니다.

- **URL**: `POST /api/batch/remote1-to-stg`
- **파라미터**: `sourceSystem` (선택)
- **응답**: 실행 결과 `BatchStatus`

## 완전 새로운 배치 작업 추가시 매뉴얼

1. Job 설정 파일 작성: `src/main/resources/egovframework/batch/job/crm/NewcrmJob.xml` - 새 작업의 단계와 흐름을 정의합니다.
2. 매퍼 XML 작성: `src/main/resources/egovframework/batch/mapper/crm/crm_new_sample.xml` - 데이터 조회와 저장 SQL을 작성합니다.
3. 도메인 클래스 생성: `src/main/java/egovframework/bat/domain/crm/Newcrm.java` - 배치에서 사용할 데이터 구조를 정의합니다.
4. 프로세서 클래스 구현: `src/main/java/egovframework/bat/domain/crm/NewcrmProcessor.java` - 도메인 데이터를 가공하는 로직을 구현합니다.
5. (선택) 테스트 코드 추가: `src/test/java/egovframework/bat/domain/crm/NewcrmProcessorTest.java` - 주요 기능이 예상대로 동작하는지 검증합니다.

예시 파일 구조:

- `src/main/resources/egovframework/batch/job/crm/NewcrmJob.xml`
- `src/main/resources/egovframework/batch/mapper/crm/crm_new_sample.xml`
- `src/main/java/egovframework/bat/domain/crm/Newcrm.java`
- `src/main/java/egovframework/bat/domain/crm/NewcrmProcessor.java`
- `src/test/java/egovframework/bat/domain/crm/NewcrmProcessorTest.java`

CRM 배치는 신규 시스템 추가시의 예시입니다
