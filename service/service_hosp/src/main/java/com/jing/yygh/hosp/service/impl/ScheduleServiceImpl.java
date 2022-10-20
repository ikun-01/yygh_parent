package com.jing.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.hosp.repository.ScheduleRepository;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.BookingRule;
import com.jing.yygh.model.hosp.Department;
import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.BookingScheduleRuleVo;
import com.jing.yygh.vo.hosp.ScheduleOrderVo;
import com.jing.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        HashMap<String, Object> resultMap = new HashMap<>();
        // 获取医院预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        BookingRule bookingRule = hospital.getBookingRule();
        // 获取分页日期
        IPage<Date> iPage = getListDate(page, limit, bookingRule);

        // 获取日期集合
        List<Date> dateList = iPage.getRecords();
        // 获取总记录数
        //long total = iPage.getTotal();
        // 获取总页数
        //long pages = iPage.getPages();
        // 获取当前页
        //long current = iPage.getCurrent();
        // 每页显示多少条
        //long size = iPage.getSize();

        // 对排班数据 按照workDate进行分组
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        // 查询这些日期内的排班
        //List<Schedule> scheduleList = mongoTemplate.find(new Query(criteria), Schedule.class);
        // 进行分组查询
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"));

        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        // 获取分组后的结果
        // 有哪些日期就有对应的对象
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();
        // 将结果转换成map key是workDate  value是对象本身
        Map<Date, BookingScheduleRuleVo> map = bookingScheduleRuleVoList.stream()
                .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, bookingScheduleRuleVo -> bookingScheduleRuleVo));


        // 连续日期对应的
        // 确保日期是连续的BookingScheduleRuleVo
        List<BookingScheduleRuleVo> list = new ArrayList<>();
        // 遍历所有的连续的日期
        for (int i = 0; i < dateList.size(); i++){
            Date workDate = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = map.get(workDate);
            // 连续日期中不存在该 排班对象
            if (bookingScheduleRuleVo == null){
                // 如果当前日期没有排班
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setReservedNumber(-1);
                bookingScheduleRuleVo.setAvailableNumber(-1);// 没有排班
                bookingScheduleRuleVo.setWorkDate(workDate);
            }
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(workDate)));
            bookingScheduleRuleVo.setWorkDateMd(workDate);

            // 最有一页的最后一条 设置为即将放号
            if (page == iPage.getPages() && i == dateList.size() - 1){
                bookingScheduleRuleVo.setStatus(1); //即将放号
            } else {
                bookingScheduleRuleVo.setStatus(0); //正常放号
            }

            // 当天停止挂号 , 第一页的第一条数据
            // 当天停止挂号时间
            if (page == 1 && i == 0){
                String stopTime = new DateTime().toString("yyyy-MM-dd") + " " +bookingRule.getStopTime();
                DateTime stop = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(stopTime);
                if (stop.isBeforeNow()){
                    // 已经停止
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }

            list.add(bookingScheduleRuleVo);
        }

        // 封装返回结果

        HashMap<String, Object> baseMap = new HashMap<>();

        baseMap.put("hosname",hospitalService.getByHoscode(hoscode).getHosname());
        baseMap.put("bigname",departmentService.getDepartment(hoscode,depcode).getBigname());
        baseMap.put("depname",departmentService.getDepName(hoscode,depcode));
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        baseMap.put("stopTime", bookingRule.getStopTime());

        resultMap.put("bookingScheduleList", list);
        resultMap.put("total", iPage.getTotal());
        resultMap.put("baseMap", baseMap);

        return resultMap;
    }

    @Override
    public Schedule getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        this.packSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        // 查询排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        // 获取预约规则
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        BookingRule bookingRule = hospital.getBookingRule();
        // 为scheduleOrderVo赋值
        scheduleOrderVo.setHoscode(hospital.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());

        Department department = departmentService.getDepartment(hospital.getHoscode(), schedule.getDepcode());
        scheduleOrderVo.setDepcode(department.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        // 退号截止天数 0为当天 -1 为前一天
        //Integer quitDay = bookingRule.getQuitDay();
        //Date date = new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate();
        //String s = new DateTime(date).toString("yyyy-MM-dd") + " " + bookingRule.getQuitTime();

        scheduleOrderVo.setQuitTime(this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(bookingRule.getQuitDay()).toDate(),bookingRule.getQuitTime()).toDate());
        // 预约开始时间
        scheduleOrderVo.setStartTime(this.getDateTime(new Date(), bookingRule.getReleaseTime()).toDate());
        // 截至预约时间
        scheduleOrderVo.setEndTime(this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(),bookingRule.getStopTime()).toDate());
        // 当天停止挂号时间
        scheduleOrderVo.setStopTime(this.getDateTime(new Date(), bookingRule.getStopTime()).toDate());

        return scheduleOrderVo;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }


    /**
     * 获取所有需要查询的预约日期并进行分页 使用MybatisPlus分页插件封装返回
     * @param page
     * @param limit
     * @param bookingRule
     * @return
     */
    private IPage<Date> getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        // 获取预约周期
        Integer cycle = bookingRule.getCycle();
        // 当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(),bookingRule.getReleaseTime());
        // 如果当天已经放号
        if (releaseTime.isBeforeNow()){
            // 预约周期+1
            cycle++;
        }
        // 创建cycle个日期对象
        List<Date> workDateList = new ArrayList<>();
        for (Integer i = 0; i < cycle; i++) {
            String dateTimeString = new DateTime().toString("yyyy-MM-dd");
            // 每创建一个 +1天
            Date date = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dateTimeString).plusDays(i).toDate();
            workDateList.add(date);
        }

        // 对日期进行分页
        List<Date> pageDateList = new ArrayList<>();
        // 开始日期
        int start = (page - 1) * limit;
        // 结束日期
        int end = (page - 1) * limit + limit;

        // 如果结束日期大于总的日期对象
        if (end > workDateList.size()){
            end = workDateList.size();
        }

        for (int i = start; i < end; i++) {
            pageDateList.add(workDateList.get(i));
        }

        // 使用Mybatis中的 分页 封装
        IPage<Date> pageResult = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, workDateList.size());
        // 将分页数据进行封装
        pageResult.setRecords(pageDateList);
        return pageResult;
    }

    /**
     * 将当天的放号时间 转换成yyyy-MM-dd HH:mm
     * @param releaseTimeString
     * @return
     */
    private DateTime getDateTime(Date date,String releaseTimeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + releaseTimeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
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
