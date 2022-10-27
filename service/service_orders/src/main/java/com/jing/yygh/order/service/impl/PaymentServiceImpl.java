package com.jing.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.enums.OrderStatusEnum;
import com.jing.yygh.enums.PaymentStatusEnum;
import com.jing.yygh.hosp.HospitalFeignClient;
import com.jing.yygh.model.hosp.HospitalSet;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.model.order.PaymentInfo;
import com.jing.yygh.order.mapper.PaymentMapper;
import com.jing.yygh.order.service.OrderService;
import com.jing.yygh.order.service.PaymentService;
import com.jing.yygh.order.utils.HttpRequestHelper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        // 每个订单只能有一个支付记录
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderInfo.getId());
        queryWrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfo = this.getOne(queryWrapper);
        if (paymentInfo != null) return;
        // 创建交易记录
        paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);

        paymentInfo.setTotalAmount(orderInfo.getAmount());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        paymentInfo.setCreateTime(new Date());
        // 支付成功后回调显示
        //paymentInfo.setTradeNo(orderInfo);
        //paymentInfo.setCallbackTime();
        //paymentInfo.setCallbackContent();
        this.save(paymentInfo);
    }

    @Override
    public void paySuccess(String out_trade_no, Map<String, String> map) {
        // 修改平台端订单信息
        QueryWrapper<OrderInfo> wrapperOrder = new QueryWrapper<>();
        wrapperOrder.eq("out_trade_no",out_trade_no);
        OrderInfo order = orderService.getOne(wrapperOrder);

        // 更新医院端支付状态
        // 获取医院编号
        String hoscode = order.getHoscode();
        HospitalSet hospitalSet = hospitalFeignClient.getHospitalSet(hoscode);

        // 调用医院端接口 更新医院端订单信息
        String url = hospitalSet.getApiUrl() + "/order/updatePayStatus";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",hoscode);
        paramMap.put("hosRecordId",order.getHosRecordId());
        paramMap.put("timestamp",System.currentTimeMillis());
        paramMap.put("sign","");

        JSONObject result = HttpRequestHelper.sendRequest(paramMap, url);
        if (!"200".equalsIgnoreCase(result.getString("code"))){
            throw new YyghException(20001,"调用医院端接口失败");
        }
        // 更新平台端订单支付状态
        order.setOrderStatus(OrderStatusEnum.PAID.getStatus()); // 已支付
        orderService.updateById(order);

        // 更新支付记录 支付状态
        QueryWrapper<PaymentInfo> wrapperPayment = new QueryWrapper<>();
        wrapperPayment.eq("out_trade_no",out_trade_no);
        PaymentInfo paymentInfo = this.getOne(wrapperPayment);

        // 设置状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(map.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(map.toString());
        this.updateById(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        wrapper.eq("payment_type",paymentType);
        return this.getOne(wrapper);
    }
}
