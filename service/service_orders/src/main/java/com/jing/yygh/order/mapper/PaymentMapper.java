package com.jing.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jing.yygh.model.order.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<PaymentInfo> {
}
