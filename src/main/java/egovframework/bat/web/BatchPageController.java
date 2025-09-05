package egovframework.bat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 배치 작업 리스트 페이지를 제공하는 컨트롤러
@Controller
@RequestMapping("/batch")
public class BatchPageController {
    // `/batch/list` 요청 시 templates/batch/list.html 렌더링
    @GetMapping("/list")
    public String list() {
        return "batch/list";
    }

    // `/batch/detail` 요청 시 templates/batch/detail.html 렌더링
    @GetMapping("/detail")
    public String detail() {
        return "batch/detail";
    }

    // `/batch/log` 요청 시 templates/batch/log.html 렌더링
    @GetMapping("/log")
    public String log() {
        return "batch/log";
    }
}
