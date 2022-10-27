package com.jing.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    /**
     * 保存支付订单信息
     * @param orderInfo 订单信息
     * @param paymentType 支付类型
     */
    void savePaymentInfo(OrderInfo orderInfo,Integer paymentType);

    /**
     * 支付完成后更新支付状态
     *
     * @param out_trade_no
     * @param map
     */
    void paySuccess(String out_trade_no, Map<String, String> map);

    /**
     * 获取支付记录
     */
    PaymentInfo getPaymentInfo(Long orderId,Integer paymentType);

}
