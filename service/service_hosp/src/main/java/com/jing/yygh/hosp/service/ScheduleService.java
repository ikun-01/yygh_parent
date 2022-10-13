package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    //上传排班信息
    void save(Schedule schedule);
    //查询排班信息
    Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo);
    //删除排班信息
    void remove(String hoscode,String hosScheduleId);
    //查询排班信息,根据日期分组
    Map<String,Object> getScheduleRule(Long page, Long limit, String hoscode, String depcode);

    //查询排班列表
    List<Schedule> getDetailSchedule(String hoscode,String depcode,String workDate);
}
