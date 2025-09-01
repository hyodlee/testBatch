package egovframework.bat.service;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import egovframework.bat.service.dto.JobExecutionDto;
import org.springframework.stereotype.Repository;

/**
 * 배치 메타데이터 조회를 위한 매퍼 인터페이스.
 */
@Mapper
@Repository
public interface BatchManagementMapper {

    /**
     * 등록된 배치 잡 이름 목록을 조회한다.
     *
     * @return 잡 이름 목록
     */
    List<String> selectJobNames();

    /**
     * 특정 잡의 실행 이력을 조회한다.
     *
     * @param jobName 잡 이름
     * @return 실행 이력 목록
     */
    List<JobExecutionDto> selectJobExecutions(String jobName);

    /**
     * 특정 잡 실행의 에러 로그를 조회한다.
     *
     * @param jobExecutionId 잡 실행 ID
     * @return 에러 로그 목록
     */
    List<String> selectErrorLogs(Long jobExecutionId);
}

