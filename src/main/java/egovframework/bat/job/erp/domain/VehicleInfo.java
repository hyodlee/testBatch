package egovframework.bat.job.erp.domain;

import java.util.Date;
import java.math.BigDecimal;

import lombok.Data;

/**
 * REST API로 수신한 차량 정보를 담는 VO 클래스
 */
@Data
public class VehicleInfo {

    private String vehicleId;     /** 차량ID (EsntlIdGenerator 등을 통해 생성) */
    private String model;         /** 모델명 */
    private String manufacturer;  /** 제조사 */
    private BigDecimal price;     /** 가격 */
    private Date regDttm;         /** 등록일시 */
    private Date modDttm;         /** 수정일시 */
}
