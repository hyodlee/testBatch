package egovframework.bat.service.dto;

import java.time.LocalDateTime;

/**
 * 배치 잡 실행 정보를 담는 DTO.
 */
public class JobExecutionDto {

    // 실행 ID
    private Long id;
    // 시작 시간
    private LocalDateTime startTime;
    // 종료 시간
    private LocalDateTime endTime;
    // 실행 상태
    private String status;
    // 종료 코드
    private String exitCode;
    // 종료 메시지
    private String exitDescription;
    // 생성 시간
    private LocalDateTime createTime;
    // 최종 수정 시간
    private LocalDateTime lastUpdated;

    /** 기본 생성자 */
    public JobExecutionDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public String getExitDescription() {
        return exitDescription;
    }

    public void setExitDescription(String exitDescription) {
        this.exitDescription = exitDescription;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
