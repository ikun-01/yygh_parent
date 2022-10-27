package com.jing.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.enums.RefundStatusEnum;
import com.jing.yygh.model.order.PaymentInfo;
import com.jing.yygh.model.order.RefundInfo;
import com.jing.yygh.order.mapper.RefundInfoMapper;
import com.jing.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo){
        // 查询对应支付记录的退款记录是否存在
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",paymentInfo.getOrderId());
        queryWrapper.eq("payment_type",paymentInfo.getPaymentType());
        RefundInfo refundInfo = this.getOne(queryWrapper);
        if (refundInfo != null){
            return refundInfo;
        }
        // 添加退款记录
        refundInfo = new RefundInfo();
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setCreateTime(new Date());

        this.save(refundInfo);
        return refundInfo;
    }
}
