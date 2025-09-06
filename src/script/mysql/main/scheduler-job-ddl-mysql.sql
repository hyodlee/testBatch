-- 스케줄러 잡 설정 테이블 생성 스크립트
DROP TABLE IF EXISTS scheduler_job;
CREATE TABLE scheduler_job (
    job_name VARCHAR(100) NOT NULL PRIMARY KEY,
    cron_expression VARCHAR(100) NOT NULL,
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    description VARCHAR(200) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
