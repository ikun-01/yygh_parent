package com.jing.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.order.OrderInfo;

public interface OrderService extends IService<OrderInfo> {

    /**
     * 保存订单
     * @param scheduleId
     * @param patientId
     * @return
     */
    Long saveOrder(String scheduleId, Long patientId);

    /**
     * 获取订单详情,提交订单后跳转新页面获取订单详情
     * @param id
     * @return
     */
    OrderInfo getOrderInfo(Long id);
}
