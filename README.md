# 빌드 가이드

이 프로젝트는 환경별 설정을 Maven 프로필로 관리합니다. 아래 명령을 사용하여 원하는 환경으로 빌드할 수 있습니다.

```bash
mvn -Plocal package   # 로컬 환경 빌드
mvn -Pdev package     # 개발 환경 빌드
mvn -Pprod package    # 운영 환경 빌드
```

각 프로필은 해당 환경의 `globals-<프로필>.properties` 파일을 사용하여 `globals.properties`를 생성합니다.

배치 잡 실행 시 `sourceSystem` 파라미터를 생략하면 `LND` 프리픽스로 ESNTL_ID가 생성됩니다.

## 인사 배치 잡 디렉터리(`insa`)

`src/main/resources/egovframework/batch/job/insa` 디렉터리는 인사 관련 배치 Job 설정을 모아두는 곳입니다. 현재 포함된 Job은 다음과 같습니다.

- `remote1ToStgJob`
- `stgToLocalJob`

다음은 관련된 주요 파일들입니다.

- 잡 설정:
  - `src/main/resources/egovframework/batch/job/insa/remote1ToStgJob.xml`: 원격 시스템에서 스테이징으로 데이터를 전송하는 Job 설정 파일
  - `src/main/resources/egovframework/batch/job/insa/stgToLocalJob.xml`: 스테이징에서 로컬로 데이터를 이동하는 Job 설정 파일
- 매퍼 파일:
  - `src/main/resources/egovframework/mapper/insa/insa_remote1_to_stg.xml`: 원격→스테이징 데이터 이동을 위한 SQL 매퍼
  - `src/main/resources/egovframework/mapper/insa/insa_stg_to_local.xml`: 스테이징→로컬 데이터 이동을 위한 SQL 매퍼
- 도메인 및 유틸 클래스:
  - `src/main/java/egovframework/bat/domain/insa/SourceSystemPrefix.java`: 시스템 구분을 위한 접두어 상수 정의 클래스
  - `src/main/java/egovframework/bat/domain/insa/EmployeeInfoProcessor.java`: 직원 정보를 처리하는 배치 프로세서
  - `src/main/java/egovframework/bat/domain/insa/EsntlIdGenerator.java`: ESNTL_ID를 생성하는 유틸리티 클래스
  - `src/main/java/egovframework/bat/domain/insa/EmployeeInfo.java`: 직원 정보를 담는 도메인 클래스
  - `src/main/java/egovframework/bat/domain/insa/Orgnztinfo.java`: 조직 정보를 표현하는 도메인 클래스
- 테스트 코드:
  - `src/test/java/egovframework/bat/domain/insa/EsntlIdGeneratorTest.java`: ESNTL_ID 생성 로직을 검증하는 테스트

### Job 추가 규칙

새로운 인사 배치 Job을 추가할 때는 다음 규칙을 지킵니다.

- 설정 파일: `src/main/resources/egovframework/batch/job/insa`에 `<Source>To<Target>Job.xml` 형태로 저장합니다. 파일명은 lowerCamelCase를 사용하며 반드시 `Job.xml`으로 끝납니다.
- 관련 도메인 클래스: `src/main/java/egovframework/bat/domain/insa` 아래에 작성하고 패키지 구조를 유지합니다.
- 테스트 코드: `src/test/java/egovframework/bat/domain/insa`에 동일한 패키지 구조로 작성합니다.

## 예제 배치 잡 디렉터리(`example`)

`src/main/resources/egovframework/batch/job/example` 디렉터리는 예제 배치 Job 설정을 모아둔 곳입니다. 예제 Job과 관련된 주요 파일은 다음과 같습니다.

- `src/main/resources/egovframework/batch/job/example/mybatisToMybatisJob.xml`: MyBatis 간 데이터 이동을 정의한 배치 Job 설정 파일
- `src/main/resources/egovframework/mapper/example/Egov_Example_SQL.xml`: 예제 배치를 위한 SQL 매퍼 파일
- `src/main/java/egovframework/example/domain/trade/CustomerCredit.java`: 고객 신용 정보를 담는 도메인 클래스
- `src/main/java/egovframework/example/domain/trade/CustomerCreditIncreaseProcessor.java`: 신용 증가 로직을 처리하는 배치 프로세서
- `src/main/java/egovframework/example/scheduler/support/EgovJobLauncherDetails.java`: Quartz 스케줄러에서 배치 Job을 실행하는 지원 클래스
- `src/main/resources/egovframework/batch/context-batch-mapper.xml`: 예제 SQL 매퍼와 데이터소스가 등록된 설정 파일
- `src/main/resources/egovframework/batch/context-scheduler-job.xml`: 예제 Job을 스케줄러에 등록하기 위한 설정 파일

## 새로운 배치 작업 추가 매뉴얼

1. Job 설정 파일 작성: `src/main/resources/egovframework/batch/job/erp/NewErpJob.xml` - 새 작업의 단계와 흐름을 정의합니다.
2. 매퍼 XML 작성: `src/main/resources/egovframework/mapper/erp/erp_new_sample.xml` - 데이터 조회와 저장 SQL을 작성합니다.
3. 도메인 클래스 생성: `src/main/java/egovframework/bat/domain/erp/NewErp.java` - 배치에서 사용할 데이터 구조를 정의합니다.
4. 프로세서 클래스 구현: `src/main/java/egovframework/bat/domain/erp/NewErpProcessor.java` - 도메인 데이터를 가공하는 로직을 구현합니다.
5. 테스트 코드 추가: `src/test/java/egovframework/bat/domain/erp/NewErpProcessorTest.java` - 주요 기능이 예상대로 동작하는지 검증합니다.

예시 파일 구조:

- `src/main/resources/egovframework/batch/job/erp/NewErpJob.xml`
- `src/main/resources/egovframework/mapper/erp/erp_new_sample.xml`
- `src/main/java/egovframework/bat/domain/erp/NewErp.java`
- `src/main/java/egovframework/bat/domain/erp/NewErpProcessor.java`
- `src/test/java/egovframework/bat/domain/erp/NewErpProcessorTest.java`

ERP 배치는 자원관리 도메인에 초점을 맞추며, 주요 필드로 자원ID, 자원명, 사용량 등이 포함됩니다. 이는 사번, 조직 정보 등 인사 중심 배치와 구별되는 특징입니다.
