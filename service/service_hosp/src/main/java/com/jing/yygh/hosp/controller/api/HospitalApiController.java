package com.jing.yygh.hosp.controller.api;


import com.jing.yygh.common.result.R;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.DepartmentVo;
import com.jing.yygh.vo.hosp.HospitalQueryVo;
import com.jing.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
@Api(description = "前台展示医院信息")
public class HospitalApiController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/{page}/{limit}")
    @ApiOperation("用户页面展示医院信息")
    public R index(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages",hospitals);
    }


    @GetMapping("/findByHosname/{hosname}")
    @ApiOperation("根据医院名称模糊查询")
    public R findByHosname(@PathVariable("hosname") String hosname){
        List<Hospital> list = hospitalService.findByName(hosname);
        return R.ok().data("list",list);
    }

    @ApiOperation("获取医院的详细信息")
    @GetMapping("/{hoscode}")
    public R item(@PathVariable("hoscode") String hoscode){
        Map<String, Object> item = hospitalService.item(hoscode);
        return R.ok().data(item);
    }

    @ApiOperation("显示医院科室列表信息")
    @GetMapping("/department/{hoscode}")
    public R index(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> depTree = departmentService.findDepTree(hoscode);
        return R.ok().data("list",depTree);
    }

    //日期列表
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    @ApiOperation("查询用户页面日期列表")
    public R getBookingSchedule(@PathVariable Integer page, @PathVariable Integer limit,
                                @PathVariable String hoscode,@PathVariable String depcode) {

        Map<String, Object> map = scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);
        return R.ok().data(map);
    }

    //根据医院编号+科室编号+日期  查询排班列表
    @ApiOperation("查询用户页面排班列表")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(@PathVariable String hoscode,@PathVariable String depcode,@PathVariable String workDate) {
        List<Schedule> scheduleList = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return R.ok().data("scheduleList",scheduleList);
    }

    @GetMapping("/getSchedule/{id}")
    @ApiOperation("根据id获取排班信息")
    public R findScheduleById(@PathVariable("id") String id){
        Schedule schedule = scheduleService.getById(id);
        return R.ok().data("schedule",schedule);
    }

    @GetMapping("/inner/getScheduleOrderVo/{scheduleId}")
    @ApiOperation("用户预约订单服务调用")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

}

