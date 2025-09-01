package egovframework.bat.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 배치 잡 스케줄을 조회하는 리포지토리.
 */
@Repository
public interface BatchJobScheduleRepository extends JpaRepository<BatchJobSchedule, String> {
}
