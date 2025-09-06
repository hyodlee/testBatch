package egovframework.bat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 스케줄러 관리 페이지를 제공하는 컨트롤러.
 */
@Controller
@RequestMapping("/scheduler")
public class SchedulerPageController {

    /**
     * 스케줄러 잡 목록 페이지를 렌더링한다.
     */
    @GetMapping("/list")
    public String list() {
        return "scheduler/list";
    }
}
