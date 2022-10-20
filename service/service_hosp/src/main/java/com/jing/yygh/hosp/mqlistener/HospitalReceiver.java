package com.jing.yygh.hosp.mqlistener;

import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.rabbitmq.consts.MqConst;
import com.jing.yygh.rabbitmq.service.RabbitService;
import com.jing.yygh.vo.msm.MsmVo;
import com.jing.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 医院服务监听消息队列中 订单服务发送的信息
 */
@Component
public class HospitalReceiver {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private RabbitService rabbitService;
    /**
     * @queue 队列的名称
     * @exchange 交换机名称
     * @key 路由key的名称
     * @param orderMqVo
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER)
            ,key = MqConst.ROUTING_ORDER))
    public void receiver(OrderMqVo orderMqVo){
        // 从消息 队列中拿到消息 获取消息中的值
        Integer reservedNumber = orderMqVo.getReservedNumber();
        Integer availableNumber = orderMqVo.getAvailableNumber();
        String scheduleId = orderMqVo.getScheduleId();
        // 从mongo中查询排班
        Schedule schedule = scheduleService.getById(scheduleId);
        schedule.setAvailableNumber(availableNumber);
        schedule.setReservedNumber(reservedNumber);
        // 更新mongo中的信息
        scheduleService.update(schedule);
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (msmVo != null){
            // 向第二个消息队列中发送数据
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }
    }
}
