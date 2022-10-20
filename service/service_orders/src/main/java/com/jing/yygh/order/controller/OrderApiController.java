package com.jing.yygh.order.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/orderInfo")
@Api(description = "订单接口")
public class OrderApiController {
    @Autowired
    private OrderService orderService;
    @ApiOperation("提交订单")
    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return R.ok().data("orderId",orderId);
    }

    @GetMapping("/auth/getOrders/{orderId}")
    @ApiOperation("获取订单详情信息")
    public R getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }
}
