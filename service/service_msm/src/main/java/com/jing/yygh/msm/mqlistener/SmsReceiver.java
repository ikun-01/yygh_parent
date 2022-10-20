package com.jing.yygh.msm.mqlistener;

import com.jing.yygh.rabbitmq.consts.MqConst;
import com.jing.yygh.vo.msm.MsmVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SmsReceiver {
  
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo) {
        //模拟向指定手机号发送短信通知
        System.out.println("模拟给就诊人发送短信通知");
        System.out.println(msmVo.getPhone() + msmVo.getParam().get("message"));
    }
}