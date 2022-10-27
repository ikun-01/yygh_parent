package com.jing.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.vo.order.OrderCountQueryVo;
import com.jing.yygh.vo.order.OrderQueryVo;

import java.util.Map;

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

    /**
     * 获取订单列表
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);


    /**
     * 取消 预约
     * @param orderId
     * @return
     */
    boolean cancelOrder(Long orderId);


    /**
     * 就诊提醒
     */
    void patientTips();

    /**
     * 订单统计
     */
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

}
