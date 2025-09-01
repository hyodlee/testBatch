-- 배치 잡 스케줄 테이블 생성
CREATE TABLE BATCH_JOB_SCHEDULE (
    job_name VARCHAR(100) NOT NULL PRIMARY KEY,
    cron_expression VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);
