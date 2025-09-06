package egovframework.bat.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import egovframework.bat.repository.dto.SchedulerJobDto;

/**
 * 스케줄러 잡 정보를 조회하고 수정하기 위한 매퍼.
 */
@Mapper
public interface SchedulerJobMapper {

    /**
     * 모든 스케줄러 잡 정보를 조회한다.
     *
     * @return 잡 설정 목록
     */
    @Select("SELECT job_name, cron_expression FROM scheduler_job WHERE use_yn = 'Y'")
    List<SchedulerJobDto> findAll();

    /**
     * 잡 정보를 저장하거나 크론 표현식을 갱신한다.
     *
     * @param jobName 잡 이름
     * @param cronExpression 크론 표현식
     */
    @Insert("INSERT INTO scheduler_job(job_name, cron_expression, use_yn) "
        + "VALUES(#{jobName}, #{cronExpression}, 'Y') "
        + "ON DUPLICATE KEY UPDATE cron_expression = VALUES(cron_expression)")
    void save(@Param("jobName") String jobName, @Param("cronExpression") String cronExpression);
}
