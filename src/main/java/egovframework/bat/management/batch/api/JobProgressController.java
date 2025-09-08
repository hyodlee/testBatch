package egovframework.bat.management.batch.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.bat.management.batch.service.JobProgressService;
import egovframework.bat.management.batch.dto.JobProgress;
import reactor.core.publisher.Flux;
import org.springframework.http.codec.ServerSentEvent;

/**
 * 배치 진행 상황을 SSE로 제공하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/management/batch")
@RequiredArgsConstructor
public class JobProgressController {

    private final JobProgressService jobProgressService;

    /**
     * 배치 진행 상황 스트림을 반환한다.
     *
     * @return SSE 스트림
     */
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<JobProgress>> progress() {
        return jobProgressService.stream();
    }
}
