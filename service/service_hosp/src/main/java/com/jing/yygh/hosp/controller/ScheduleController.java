package com.jing.yygh.hosp.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
@Api(description = "排班列表")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    @ApiOperation("获取指定科室的排班信息--日期")
    public R getScheduleRule(@PathVariable("page") Long page,@PathVariable("limit") Long limit,
                             @PathVariable("hoscode") String hoscode,@PathVariable("depcode") String depcode){
        Map<String, Object> scheduleRule = scheduleService.getScheduleRule(page, limit, hoscode, depcode);
        return R.ok().data(scheduleRule);
    }

    @GetMapping("/getDetailSchedule/{hoscode}/{depcode}/{workDate}")
    @ApiOperation("获取当前日期的排班列表")
    public R getDetailSchedule(@PathVariable("hoscode") String hoscode,@PathVariable("depcode") String depcode,@PathVariable("workDate") String workDate){
        List<Schedule> detailSchedule = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return R.ok().data("list",detailSchedule);
    }
}
