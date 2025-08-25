package egovframework.bat.erp.api;

import egovframework.bat.erp.domain.VehicleInfo;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERP 서비스용 차량 정보를 제공하는 REST 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1")
public class VehicleController {

    /**
     * 간단한 차량 목록을 반환한다.
     *
     * @return 샘플 차량 정보 목록
     */
    @GetMapping("/vehicles")
    public List<VehicleInfo> getVehicles() {
        VehicleInfo sample = new VehicleInfo();
        sample.setVehicleId("SAMPLE-0001");
        sample.setModel("샘플 모델");
        sample.setManufacturer("샘플 제조사");
        sample.setPrice(new BigDecimal("1000"));
        sample.setRegDttm(new Date());
        sample.setModDttm(new Date());
        return Collections.singletonList(sample);
    }
}

