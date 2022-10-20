package com.jing.yygh.order;

import com.jing.yygh.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Test
    public void test(){
       orderService.saveOrder("6342d6a40d70760a4d5c9214",9L);
    }
}
