package com.jing.yygh.order.service;

import java.util.Map;

public interface WeiXinService {
    /**
     * 根据 订单编号创建支付链接
     * @param orderId
     * @return
     */
    Map createNative(Long orderId);

    /**
     * 根据订单id 去微信查询支付状态
     * @param orderId
     * @return
     */
    Map queryPayStatus(Long orderId);

    /**
     * 根据订单编号进行退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

}
