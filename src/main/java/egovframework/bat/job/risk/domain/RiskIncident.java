package egovframework.bat.job.risk.domain;

import java.util.Date;

import lombok.Data;

/**
 * 위험 사건(리스크 이슈) 정보를 담는 VO 클래스.
 */
@Data
public class RiskIncident {

    private String incidentId;    /** 로컬 시스템에서 사용하는 사건 ID */
    private String incidentNo;    /** 원천 시스템의 사건 식별자 */
    private String categoryId;    /** 소속 카테고리 ID */
    private String title;         /** 사건 제목 */
    private String riskLevel;     /** 위험 등급 */
    private String status;        /** 처리 상태 */
    private String ownerId;       /** 담당자 ID */
    private String description;   /** 사건 상세 설명 */
    private Date occurredAt;      /** 발생 일시 */
    private Date updatedAt;       /** 수정 일시 */
}
