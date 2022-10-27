package com.jing.yygh.task.task;

import com.jing.yygh.rabbitmq.consts.MqConst;
import com.jing.yygh.rabbitmq.service.RabbitService;
import com.jing.yygh.vo.msm.MsmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling
public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天八点 执行 就诊提醒
     */
    //@Scheduled(cron = "0 0 8 * * ?") // 秒 分 时 日 月 周
    @Scheduled(cron = "0/5 * * * * ?") // 从0秒开始 每五秒一次
    public void task(){
        System.out.println(new Date().toLocaleString());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,new MsmVo());
    }
}
