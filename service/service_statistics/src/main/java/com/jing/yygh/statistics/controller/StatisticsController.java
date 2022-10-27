package com.jing.yygh.statistics.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.order.client.OrderFeignClient;
import com.jing.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
@Api(description = "统计模块")
public class StatisticsController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/getCountMap")
    @ApiOperation("统计指定医院的预约信息")
    public R getCountMap(OrderCountQueryVo orderCountQueryVo){
        Map<String, Object> countMap = orderFeignClient.getCountMap(orderCountQueryVo);
        return R.ok().data(countMap);
    }

}


