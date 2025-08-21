package egovframework.bat.crm.domain;

import java.util.Date;

import lombok.Data;

/**
 * REST API로 수신한 고객 정보를 담는 VO 클래스
 */
@Data
public class CustomerInfo {

    private String customerId;    /** 고객ID (EsntlIdGenerator 등을 통해 생성) */
    private String name;          /** 고객명 */
    private String email;         /** 이메일 */
    private String phone;         /** 전화번호 */
    private Date regDttm;         /** 등록일시 */
    private Date modDttm;         /** 수정일시 */
}
