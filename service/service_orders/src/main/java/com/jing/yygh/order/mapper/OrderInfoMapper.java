package com.jing.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.vo.order.OrderCountQueryVo;
import com.jing.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    /**
     * 统计每天平台预约数据
     */
    List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);
}
