# 컨트롤러별 역할과 주소

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

