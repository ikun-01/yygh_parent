package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

public interface ScheduleService {
    //上传排班信息
    void save(Schedule schedule);
    //查询排班信息
    Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo);
    //删除排班信息
    void remove(String hoscode,String hosScheduleId);
}
