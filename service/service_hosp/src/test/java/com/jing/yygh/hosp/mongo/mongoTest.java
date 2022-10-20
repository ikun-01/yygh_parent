package com.jing.yygh.hosp.mongo;

import com.jing.yygh.hosp.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;


/**
 * Mongodb测试
 */
@SpringBootTest
public class mongoTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ScheduleService scheduleService;
    @Test
    public void test1(){
        User user = new User();
        user.setId("1");
        user.setName("张三");
        user.setAge(18);
        user.setEmail("12344@qq.com");
        user.setCreateDate(new Date().toString());
        user = mongoTemplate.insert(user);
        System.out.println(user);
    }

    @Test
    public void groupTest(){
        scheduleService.getScheduleRule(0L,0L,"10000","200040878");
    }

    @Test
    public void getBookingScheduleRule(){
        scheduleService.getBookingScheduleRule(1,7,"10000","200040878");
    }
}
