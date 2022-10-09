package com.jing.yygh.hosp.mongo;

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
}
