package egovframework.bat.domain.insa;

import lombok.Data;

/**
 * 조직 정보를 담는 VO 클래스
 */
@Data
public class Orgnztinfo {

    private String orgnztId;		/** 조직ID */
    private String orgnztNm;		/** 조직명 */
    private String orgnztDc;		/** 조직설명 */

}
