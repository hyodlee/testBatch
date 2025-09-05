배치 관리 컨트롤러 상세 설명
컨트롤러별 역할과 주소
BatchApiController
배치 잡 목록 조회, 실행 이력/상세, 에러 로그, 재시작·중지 등 핵심 배치 관리 API를 /api/batch 하위에서 제공한다.

BatchManagementController
관리용 DTO를 사용해 잡 목록·실행 이력·에러 로그·재시작/중지 기능을 /api/batch/management 경로로 제공한다.

JobProgressController
배치 작업의 실시간 진행 상황을 SSE(Server‑Sent Events) 스트림으로 /api/batch/progress에 노출한다.

왜 세 개로 나뉘어 있나?
BatchApiController: 배치 잡 상태 관리의 기본 REST API.

BatchManagementController: DTO 기반 관리 기능(관리 UI 등에 최적화).

JobProgressController: 실시간 진행률 전송(SSE) 전용.

기능별 책임을 분리하기 위해 컨트롤러가 나뉘어 있으며, BatchApiController와 BatchManagementController는 일부 기능이 중복되지만 반환 형식과 목적이 다르다. JobProgressController는 실시간 스트리밍만 담당한다.

주소(엔드포인트) 정리
컨트롤러	기본 경로	주요 하위 경로 예시
BatchApiController	/api/batch	/jobs, /jobs/{jobName}/executions, /executions/{execId}, /error-log, /executions/{execId}/restart, /executions/{execId}(DELETE)
BatchManagementController	/api/batch/management	/jobs, /jobs/{jobName}/executions, /executions/{jobExecutionId}/errors, /executions/{jobExecutionId}/restart, /executions/{jobExecutionId}/stop
JobProgressController	/api/batch	/progress (SSE 스트림)
BatchApiController와 JobProgressController는 같은 기본 경로를 공유하지만 하위 경로가 달라 충돌 없이 공존하며, BatchManagementController는 별도의 하위 경로(/api/batch/management)를 사용한다. 따라서 모두 배치 관리와 관련된 기능을 제공하되, 접근 경로는 명확하게 구분되어 있다.
