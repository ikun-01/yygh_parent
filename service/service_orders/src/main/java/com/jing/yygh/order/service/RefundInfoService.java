package com.jing.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.order.PaymentInfo;
import com.jing.yygh.model.order.RefundInfo;

public interface RefundInfoService extends IService<RefundInfo> {
    /**
     * 保存退款记录
     * @param paymentInfo
     * @return
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
