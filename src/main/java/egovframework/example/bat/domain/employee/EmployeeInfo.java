package egovframework.example.bat.domain.employee;

import java.util.Date;

import lombok.Data;

/**
 * 직원 정보를 담는 VO 클래스
 */
@Data
public class EmployeeInfo {

    private String emplyrId;		/** 업무사용자ID */
    private String orgnztId;		/** 조직ID */
    private String orgnztNm;		/** 조직명 */
    private String orgnztDc;		/** 조직설명 */
    private String userNm;			/** 사용자명 */
    private String sexdstnCode;		/** 성별코드 */
    private String brthdy;			/** 생일 */
    private String mbtlnum;			/** 이동전화번호 */
    private String emailAdres;		/** 이메일주소 */
    private String ofcpsNm;			/** 직위명 */
    private String emplyrSttusCode;	/** 사용자상태코드 */
    private Date regDttm;			/** 등록일자 */
    private Date modDttm;			/** 수정일자 */

}
