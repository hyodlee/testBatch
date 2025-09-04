package egovframework.bat.repository;

import egovframework.bat.repository.dto.BatchJobInstanceDto;
import egovframework.bat.repository.dto.BatchJobExecutionDto;
import egovframework.bat.repository.dto.BatchStepExecutionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 스프링 배치 메타데이터를 조회하기 위한 매퍼.
 */
@Mapper
public interface BatchMetadataMapper {

    /**
     * 배치 잡 인스턴스를 조건에 따라 조회한다.
     *
     * @param jobName 잡 이름
     * @return 잡 인스턴스 목록
     */
    @Select("<script>"
        + "SELECT BJI.JOB_INSTANCE_ID, BJI.JOB_NAME "
        + "FROM BATCH_JOB_INSTANCE BJI "
        + "<where>"
        + " <if test='jobName != null and jobName != \"\"'>"
        + "   AND BJI.JOB_NAME = #{jobName}"
        + " </if>"
        + "</where>"
        + "ORDER BY BJI.JOB_INSTANCE_ID DESC"
        + "</script>")
    List<BatchJobInstanceDto> selectJobInstances(@Param("jobName") String jobName);

    /**
     * 배치 잡 실행 정보를 조건에 따라 조회한다.
     *
     * @param jobName 잡 이름
     * @param status  실행 상태
     * @param from    시작일시
     * @param to      종료일시
     * @return 잡 실행 정보 목록
     */
    @Select("<script>"
        + "SELECT BJE.JOB_EXECUTION_ID, BJE.JOB_INSTANCE_ID, BJE.START_TIME, BJE.END_TIME, "
        + "       BJE.STATUS, BJI.JOB_NAME "
        + "FROM BATCH_JOB_EXECUTION BJE "
        + "JOIN BATCH_JOB_INSTANCE BJI ON BJE.JOB_INSTANCE_ID = BJI.JOB_INSTANCE_ID "
        + "<where>"
        + " <if test='jobName != null and jobName != \"\"'>"
        + "   AND BJI.JOB_NAME = #{jobName}"
        + " </if>"
        + " <if test='status != null and status != \"\"'>"
        + "   AND BJE.STATUS = #{status}"
        + " </if>"
        + " <if test='from != null'>"
        + "   AND BJE.START_TIME <![CDATA[>=]]> #{from}"
        + " </if>"
        + " <if test='to != null'>"
        + "   AND BJE.END_TIME <![CDATA[<=]]> #{to}"
        + " </if>"
        + "</where>"
        + "ORDER BY BJE.JOB_EXECUTION_ID DESC"
        + "</script>")
    List<BatchJobExecutionDto> selectJobExecutions(
        @Param("jobName") String jobName,
        @Param("status") String status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    /**
     * 배치 스텝 실행 정보를 조건에 따라 조회한다.
     *
     * @param jobName 잡 이름
     * @param stepName 스텝 이름
     * @param status 실행 상태
     * @param from 시작일시
     * @param to 종료일시
     * @return 스텝 실행 정보 목록
     */
    @Select("<script>"
        + "SELECT BSE.STEP_EXECUTION_ID, BSE.JOB_EXECUTION_ID, BSE.STEP_NAME, "
        + "       BSE.START_TIME, BSE.END_TIME, BSE.STATUS "
        + "FROM BATCH_STEP_EXECUTION BSE "
        + "JOIN BATCH_JOB_EXECUTION BJE ON BSE.JOB_EXECUTION_ID = BJE.JOB_EXECUTION_ID "
        + "JOIN BATCH_JOB_INSTANCE BJI ON BJE.JOB_INSTANCE_ID = BJI.JOB_INSTANCE_ID "
        + "<where>"
        + " <if test='jobName != null and jobName != \"\"'>"
        + "   AND BJI.JOB_NAME = #{jobName}"
        + " </if>"
        + " <if test='stepName != null and stepName != \"\"'>"
        + "   AND BSE.STEP_NAME = #{stepName}"
        + " </if>"
        + " <if test='status != null and status != \"\"'>"
        + "   AND BSE.STATUS = #{status}"
        + " </if>"
        + " <if test='from != null'>"
        + "   AND BSE.START_TIME <![CDATA[>=]]> #{from}"
        + " </if>"
        + " <if test='to != null'>"
        + "   AND BSE.END_TIME <![CDATA[<=]]> #{to}"
        + " </if>"
        + "</where>"
        + "ORDER BY BSE.STEP_EXECUTION_ID DESC"
        + "</script>")
    List<BatchStepExecutionDto> selectStepExecutions(
        @Param("jobName") String jobName,
        @Param("stepName") String stepName,
        @Param("status") String status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}

