package egovframework.bat.scheduler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 배치 잡 스케줄 정보를 저장하는 엔티티.
 * DB 테이블 BATCH_JOB_SCHEDULE과 매핑된다.
 */
@Entity
@Table(name = "BATCH_JOB_SCHEDULE")
public class BatchJobSchedule {

    /** 잡 이름 (JobDetail의 빈 이름) */
    @Id
    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    /** 크론 표현식 */
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /** 설명 (선택) */
    @Column(name = "description")
    private String description;

    /** 기본 생성자 */
    public BatchJobSchedule() {}

    public BatchJobSchedule(String jobName, String cronExpression, String description) {
        this.jobName = jobName;
        this.cronExpression = cronExpression;
        this.description = description;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
