package com.jing.yygh.order.listener;

import com.jing.yygh.order.service.OrderService;
import com.jing.yygh.rabbitmq.consts.MqConst;
import com.jing.yygh.vo.msm.MsmVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component // 监听定时器队列
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = MqConst.QUEUE_TASK_8,durable = "true"),
    exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),key = MqConst.ROUTING_TASK_8))
    public void receiver(MsmVo msmVo){
        if (msmVo != null){
            // 提醒
            orderService.patientTips();
        }
    }
}
