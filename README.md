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

### Job 추가 규칙

새로운 인사 배치 Job을 추가할 때는 다음 규칙을 지킵니다.

- 설정 파일: `src/main/resources/egovframework/batch/job/insa`에 `<Source>To<Target>Job.xml` 형태로 저장합니다. 파일명은 lowerCamelCase를 사용하며 반드시 `Job.xml`으로 끝납니다.
- 관련 도메인 클래스: `src/main/java/egovframework/bat/domain/insa` 아래에 작성하고 패키지 구조를 유지합니다.
- 테스트 코드: `src/test/java/egovframework/bat/domain/insa`에 동일한 패키지 구조로 작성합니다.

## 예제 배치 잡 디렉터리(`example`)

`src/main/resources/egovframework/batch/job/example` 디렉터리는 예제 배치 Job 설정을 모아둔 곳입니다. 예제 Job과 관련된 주요 파일은 다음과 같습니다.

- `src/main/resources/egovframework/batch/job/example/mybatisToMybatisJob.xml`: MyBatis 간 데이터 이동을 정의한 배치 Job 설정 파일
- `src/main/resources/egovframework/mapper/example/bat/Egov_Example_SQL.xml`: 예제 배치를 위한 SQL 매퍼 파일
- `src/main/java/egovframework/example/bat/domain/trade/CustomerCredit.java`: 고객 신용 정보를 담는 도메인 클래스
- `src/main/java/egovframework/example/bat/domain/trade/CustomerCreditIncreaseProcessor.java`: 신용 증가 로직을 처리하는 배치 프로세서
- `src/main/java/egovframework/example/bat/scheduler/support/EgovJobLauncherDetails.java`: Quartz 스케줄러에서 배치 Job을 실행하는 지원 클래스
- `src/main/resources/egovframework/batch/context-batch-mapper.xml`: 예제 SQL 매퍼와 데이터소스가 등록된 설정 파일
- `src/main/resources/egovframework/batch/context-scheduler-job.xml`: 예제 Job을 스케줄러에 등록하기 위한 설정 파일
