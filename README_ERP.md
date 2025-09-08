ERP 관련 HTTP 주소
GET /api/v1/vehicles – VehicleController가 제공하는 ERP 차량 정보 조회 엔드포인트
POST /api/batch/erp-rest-to-stg – RestToStgJobController가 ERP REST 데이터를 STG 테이블로 적재할 때 사용
POST /api/batch/erp-stg-to-rest – StgToRestJobController가 STG 데이터를 외부 REST API로 전송할 때 사용

ERP 관련 Java 패키지/폴더 구조
src/main/java/egovframework/bat/job
└─ common, erp, example, insa 등으로 잡 기능이 분류됨

src/main/java/egovframework/bat/job/erp
└─ api, config, domain, exception, processor, tasklet 하위 패키지로 구성

api: RestToStgJobController.java, StgToRestJobController.java, VehicleController.java
config: ERP 배치 설정 클래스들 (ErpFailLogTableInitializer.java, ErpRestToStgJobConfig.java, ErpStgToLocalJobConfig.java, ErpStgToRestJobConfig.java)
domain: VehicleInfo.java
exception: ErpApiException.java
processor: VehicleInfoProcessor.java
tasklet: FetchErpDataTasklet.java, SendErpDataTasklet.java, TruncateErpVehicleTasklet.java
