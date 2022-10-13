package com.jing.yygh.hosp.service.impl;

import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.hosp.repository.ScheduleRepository;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.BookingScheduleRuleVo;
import com.jing.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;
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

    @Override
    public Map<String, Object> getScheduleRule(Long page, Long limit, String hoscode, String depcode) {

        Map<String,Object> result = new HashMap<>();

        //mongoTemplate条件查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),// 匹配查询条件
                Aggregation.group("workDate") //根据工作日期分组
                        .first("workDate").as("workDate") //取出每组中的第一条数据的workDate 别名为workDate
                        .count().as("docCount") // 统计每组的排班数 也是医生的数量
                        .sum("reservedNumber").as("reservedNumber") // 计算每组总预约数
                        .sum("availableNumber").as("availableNumber"), // 计算每组可以预约的数量
                // 根据工作日期进行排序
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                // 分页
                Aggregation.skip(limit * (page - 1)), //跳过前几个 从0开始
                Aggregation.limit(limit)
        );

        //参数1:聚合对象
        //参数2: 查询的实体类
        //参数3: 查询结果封装类 别名要和类的属性名 相同
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        //得到分组结果
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        //遍历分组结果 根据workDate计算星期
        bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        });

        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        // 总记录数
        Integer totalSize = totalSize(hoscode, depcode);
        result.put("total",totalSize);
        // 医院的名称
        String hosname = hospitalService.getByHoscode(hoscode).getHosname();

        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosname);

        result.put("baseMap",baseMap);

        return result;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {

        DateTime dateTime = new DateTime(workDate);
        Date date = dateTime.toDate();
        List<Schedule> list = scheduleRepository.getByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);

        //每个schedule附加属性
        list.forEach(schedule -> {
            this.packSchedule(schedule);
        });

        return list;
    }

    /**
     * 将日期转换成这个星期的第几天
     * @param dateTime
     * @return
     */

    private String getDayOfWeek(DateTime dateTime){
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "星期天";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "星期一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "星期二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "星期三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "星期四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "星期五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "星期六";
                break;
            default:
                break;
        }
        return dayOfWeek;
    }

    /**
     * 记录总记录数
     * @param hoscode
     * @param depcode
     * @return
     */
    private Integer totalSize(String hoscode,String depcode){
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria), Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        return aggregate.getMappedResults().size();
    }

    private Schedule packSchedule(Schedule schedule){
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();

        String hosname = hospitalService.getByHoscode(hoscode).getHosname();
        String depname = departmentService.getDepName(hoscode,depcode);

        Date workDate = schedule.getWorkDate();
        String dayOfWeek = getDayOfWeek(new DateTime(workDate));

        schedule.getParam().put("hosname",hosname);
        schedule.getParam().put("depname",depname);
        schedule.getParam().put("dayOfWeek",dayOfWeek);
        return schedule;
    }
}
