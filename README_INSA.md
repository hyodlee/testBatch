인사 관련 HTTP 주소
POST /api/batch/remote1-to-stg – Remote1ToStgJobController가 Remote1 데이터를 STG 테이블로 적재할 때 사용

인사 관련 Java 패키지/폴더 구조
src/main/java/egovframework/bat/job
└─ common, erp, example, insa 등으로 잡 기능이 분류됨

src/main/java/egovframework/bat/job/insa
└─ api, config, domain, listener, tasklet 하위 패키지로 구성

api: Remote1ToStgJobController.java
config: InsaRemote1ToStgJobConfig.java, InsaStgToLocalJobConfig.java
domain: EmployeeInfo.java, Orgnztinfo.java
listener: StepCountLogger.java
tasklet: TruncateStgTablesTasklet.java, StgToLocalEmployeeTasklet.java
