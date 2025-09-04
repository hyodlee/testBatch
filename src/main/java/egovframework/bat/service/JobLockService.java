package egovframework.bat.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

/**
 * 배치 작업의 중복 실행을 방지하기 위한 간단한 락 서비스.
 */
@Service
public class JobLockService {

    /** 작업별로 락을 보관하는 맵 */
    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * 주어진 작업 이름에 대해 락을 시도한다.
     *
     * @param jobName 작업 이름
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String jobName) {
        return locks.computeIfAbsent(jobName, k -> new ReentrantLock()).tryLock();
    }

    /**
     * 작업에 걸린 락을 해제한다.
     *
     * @param jobName 작업 이름
     */
    public void unlock(String jobName) {
        ReentrantLock lock = locks.get(jobName);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 해당 작업이 현재 실행 중인지 확인한다.
     *
     * @param jobName 작업 이름
     * @return 실행 중이면 true
     */
    public boolean isLocked(String jobName) {
        ReentrantLock lock = locks.get(jobName);
        return lock != null && lock.isLocked();
    }
}

