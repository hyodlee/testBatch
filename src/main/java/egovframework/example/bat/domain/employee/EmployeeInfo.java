package egovframework.example.bat.domain.employee;

import java.util.Date;

/**
 * 직원 정보를 담는 VO 클래스
 */
public class EmployeeInfo {

    /** 업무사용자ID */
    private String emplyrId;

    /** 조직ID */
    private String orgnztId;

    /** 조직명 */
    private String orgnztNm;

    /** 조직설명 */
    private String orgnztDc;

    /** 사용자명 */
    private String userNm;

    /** 성별코드 */
    private String sexdstnCode;

    /** 생일 */
    private String brthdy;

    /** 이동전화번호 */
    private String mbtlnum;

    /** 이메일주소 */
    private String emailAdres;

    /** 직위명 */
    private String ofcpsNm;

    /** 사용자상태코드 */
    private String emplyrSttusCode;

    /** 등록일자 */
    private Date regDttm;

    /** 수정일자 */
    private Date modDttm;

    public String getEmplyrId() {
        return emplyrId;
    }

    public void setEmplyrId(String emplyrId) {
        this.emplyrId = emplyrId;
    }

    public String getOrgnztId() {
        return orgnztId;
    }

    public void setOrgnztId(String orgnztId) {
        this.orgnztId = orgnztId;
    }

    public String getOrgnztNm() {
        return orgnztNm;
    }

    public void setOrgnztNm(String orgnztNm) {
        this.orgnztNm = orgnztNm;
    }

    public String getOrgnztDc() {
        return orgnztDc;
    }

    public void setOrgnztDc(String orgnztDc) {
        this.orgnztDc = orgnztDc;
    }

    public String getUserNm() {
        return userNm;
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

    public String getSexdstnCode() {
        return sexdstnCode;
    }

    public void setSexdstnCode(String sexdstnCode) {
        this.sexdstnCode = sexdstnCode;
    }

    public String getBrthdy() {
        return brthdy;
    }

    public void setBrthdy(String brthdy) {
        this.brthdy = brthdy;
    }

    public String getMbtlnum() {
        return mbtlnum;
    }

    public void setMbtlnum(String mbtlnum) {
        this.mbtlnum = mbtlnum;
    }

    public String getEmailAdres() {
        return emailAdres;
    }

    public void setEmailAdres(String emailAdres) {
        this.emailAdres = emailAdres;
    }

    public String getOfcpsNm() {
        return ofcpsNm;
    }

    public void setOfcpsNm(String ofcpsNm) {
        this.ofcpsNm = ofcpsNm;
    }

    public String getEmplyrSttusCode() {
        return emplyrSttusCode;
    }

    public void setEmplyrSttusCode(String emplyrSttusCode) {
        this.emplyrSttusCode = emplyrSttusCode;
    }

    public Date getRegDttm() {
        return regDttm;
    }

    public void setRegDttm(Date regDttm) {
        this.regDttm = regDttm;
    }

    public Date getModDttm() {
        return modDttm;
    }

    public void setModDttm(Date modDttm) {
        this.modDttm = modDttm;
    }

    @Override
    public String toString() {
        return "EmployeeInfo{" +
            "emplyrId='" + emplyrId + '\'' +
            ", userNm='" + userNm + '\'' +
            ", orgnztId='" + orgnztId + '\'' +
            '}';
    }
}
