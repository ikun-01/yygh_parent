package com.jing.yygh.hosp.service.impl;

import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.hosp.repository.ScheduleRepository;
import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Override
    public void save(Schedule schedule) {
        // 参数校验
        if (schedule == null){
            throw new YyghException(20001,"排班信息为空");
        }
        if (StringUtils.isEmpty(schedule.getHoscode())
        || StringUtils.isEmpty(schedule.getDepcode())
        || StringUtils.isEmpty(schedule.getHosScheduleId())){
            throw new YyghException(20001,"参数不能为空");
        }

        //根据医院编号和排班号进行查询
        Schedule exitSchedule =
                scheduleRepository.getByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());

        //根据是否存在来决定是新增还是修改
        if (exitSchedule == null) {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
        } else {
            schedule.setId(exitSchedule.getId());
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(exitSchedule.getCreateTime());
            schedule.setStatus(1);
        }
        scheduleRepository.save(schedule);
    }

    @Override
    public Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo) {
        if (page == null || page < 0){
            page = 0;
        }
        if (limit == null || limit < 1){
            limit = 10;
        }
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);

        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching().
                withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        //条件
        Example<Schedule> example = Example.of(schedule,matcher);
        //排序
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //分页
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        return scheduleRepository.findAll(example, pageable);
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        if (StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编号为空");
        }
        if (StringUtils.isEmpty(hosScheduleId)){
            throw new YyghException(20001,"排班编号为空");
        }
        Schedule schedule = scheduleRepository.getByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule == null){
            throw new YyghException(20001,"排班信息不存在");
        }
        scheduleRepository.deleteById(schedule.getId());
    }
}
