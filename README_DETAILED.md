# Java 파일의 간단 역할 설명

BatchApiController.java : 배치 잡 목록 조회, 실행 이력/상세, 에러 로그, 재시작·중지 등 관리 API 제공
BatchManagementController.java : DTO 기반 관리용 API로 잡 목록·이력·에러 로그·재시작/중지 기능 제공
BatchPageController.java : 배치 작업 리스트·상세·로그 화면을 렌더링
JobRunController.java : 공통 잡 실행 엔드포인트로 지정된 잡을 실행
Remote1ToStgJobController.java : Remote1 데이터를 STG로 적재하는 배치 실행
ExampleJobController.java : 마이바티스 예제 배치 잡을 실행
StgToRestJobController.java : STG 데이터를 외부 REST API로 전송
RestToStgJobController.java : ERP REST 데이터를 STG 테이블로 적재
VehicleController.java : ERP 서비스용 차량 정보를 조회


