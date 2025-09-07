package egovframework.bat.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProps {
    // application.yml의 scheduler.jobs를 그대로 바인딩
    private Map<String, String> jobs = new HashMap<>();

    public Map<String, String> getJobs() {
        return jobs;
    }
    public void setJobs(Map<String, String> jobs) {
        this.jobs = jobs;
    }
}
