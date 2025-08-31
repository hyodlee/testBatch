package egovframework.bat.management;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import egovframework.bat.management.dto.JobProgress;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 배치 작업 진행 상황을 실시간으로 전송하는 서비스.
 */
@Service
public class JobProgressService {

    /** SSE 전송을 위한 싱크 */
    private final Sinks.Many<JobProgress> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * 진행 상황을 전송한다.
     *
     * @param jobName 작업 이름
     * @param status  진행 상태
     */
    public void send(String jobName, String status) {
        sink.tryEmitNext(new JobProgress(jobName, status));
    }

    /**
     * SSE 구독을 위한 스트림을 반환한다.
     *
     * @return 진행 상황 스트림
     */
    public Flux<ServerSentEvent<JobProgress>> stream() {
        return sink.asFlux().map(data -> ServerSentEvent.builder(data).build());
    }
}
