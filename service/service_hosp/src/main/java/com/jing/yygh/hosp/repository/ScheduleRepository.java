package com.jing.yygh.hosp.repository;

import com.jing.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    //根据医院编号和排班编号获取排班信息
    Schedule getByHoscodeAndHosScheduleId(String hoscode,String hosScheduleId);

    // 查询当前日期该科室的排班列表
    List<Schedule> getByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date date);
}
