package egovframework.bat.job.risk.domain;

import lombok.Data;

/**
 * 위험 카테고리 정보를 담는 VO 클래스.
 */
@Data
public class RiskCategory {

    private String categoryId;        /** 카테고리 고유 ID */
    private String categoryName;      /** 카테고리 명칭 */
    private String categoryDescription; /** 카테고리 설명 */
}
