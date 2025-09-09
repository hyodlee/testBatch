예제 관련 HTTP 주소
POST /api/batch/mybatis – ExampleJobController가 MyBatis 간 데이터 이동 잡 실행할 때 사용

예제 관련 Java 패키지/폴더 구조
src/main/java/egovframework/bat/job
└─ common, erp, example, insa 등으로 잡 기능이 분류됨

src/main/java/egovframework/bat/job/example
└─ api, config, domain, processor 하위 패키지로 구성

api: ExampleJobController.java
config: MybatisToMybatisJobConfig.java
domain: CustomerCredit.java
processor: CustomerCreditIncreaseProcessor.java
