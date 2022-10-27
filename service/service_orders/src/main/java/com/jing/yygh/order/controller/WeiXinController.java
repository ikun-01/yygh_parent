package com.jing.yygh.order.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.order.service.PaymentService;
import com.jing.yygh.order.service.WeiXinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
@Api(description = "支付模块")
public class WeiXinController {
    @Autowired
    private WeiXinService weiXinService;
    @Autowired
    private PaymentService paymentService;

    @ApiOperation("下单支付生成 支付二维码")
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId") Long orderId){
        Map map = weiXinService.createNative(orderId);
        return R.ok().data(map);
    }

    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable("orderId") Long orderId){
        Map<String,String> map = weiXinService.queryPayStatus(orderId);
        if (map == null){
            return R.error().message("支付出错");
        }
        // 支付成功
        if ("SUCCESS".equals(map.get("trade_state"))){
            String out_trade_no = map.get("out_trade_no");
            // 修改订单和支付记录的支付状态
            paymentService.paySuccess(out_trade_no,map);
            return R.ok().message("支付成功");
        }
        return R.ok().message("支付中");
    }
}
