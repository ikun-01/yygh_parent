package com.jing.yygh.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jing.yygh.common.result.R;
import com.jing.yygh.enums.OrderStatusEnum;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.order.service.OrderService;
import com.jing.yygh.order.utils.AuthContextHolder;
import com.jing.yygh.vo.order.OrderCountQueryVo;
import com.jing.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order/orderInfo")
@Api(description = "订单接口")
public class OrderApiController {
    @Autowired
    private OrderService orderService;
    @ApiOperation("提交订单")
    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    @SentinelResource(value = "submitOrder",blockHandler = "submitOrderBlockHandler")
    public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        // 并发测试
        //Long orderId = 1L;
        return R.ok().data("orderId",orderId);
    }


    public R submitOrderBlockHandler(String scheduled, Long patientId, BlockException blockException){
        return R.error().message("系统业务繁忙,请稍后下单");
    }

    @GetMapping("/auth/getOrders/{orderId}")
    @ApiOperation("获取订单详情信息")
    public R getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    @GetMapping("/auth/{page}/{limit}")
    @ApiOperation("获取当前登录用户的订单信息")
    public R list(@PathVariable Long page, @PathVariable Long limit, OrderQueryVo orderQueryVo, HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        orderQueryVo.setUserId(userId);
        IPage<OrderInfo> pages = orderService.selectPage(new Page<>(page, limit), orderQueryVo);
        return R.ok().data("pageModel",pages);
    }

    @GetMapping("/auth/getStatusList")
    @ApiOperation("获取订单状态列表")
    public R getStatusList() {
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }

    @GetMapping("/auth/cancelOrder/{orderId}")
    @ApiOperation("取消预约")
    public R cancelOrder(@PathVariable("orderId")Long orderId){
        boolean isCancel = orderService.cancelOrder(orderId);
        return R.ok().data("flag",isCancel);
    }

    @PostMapping("/inner/getCountMap")
    @ApiOperation("获取指定医院的预约统计信息")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderService.getCountMap(orderCountQueryVo);
    }
}
