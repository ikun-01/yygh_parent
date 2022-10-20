package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.ScheduleOrderVo;
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

    /**
     * 预约详情页面  预约排班信息
     */
    Map<String,Object> getBookingScheduleRule(Integer page,Integer limit,String hoscode,String depcode);

    /**
     * 获取排班信息
     * @param id
     * @return
     */
    Schedule getById(String id);

    //根据排班id获取预约下单数据（医院+排班数据）

    /**
     * 提供给订单服务调用
     * @param scheduleId
     * @return
     */
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    void update(Schedule schedule);
}
