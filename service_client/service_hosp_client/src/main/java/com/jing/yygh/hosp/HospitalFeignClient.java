package com.jing.yygh.hosp;

import com.jing.yygh.model.hosp.HospitalSet;
import com.jing.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-hosp")
public interface HospitalFeignClient {
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    @GetMapping("/admin/hosp/hospitalSet/getHospitalSetByHoscode/{hoscode}")
    public HospitalSet getHospitalSet(@PathVariable("hoscode") String hoscode);
}
