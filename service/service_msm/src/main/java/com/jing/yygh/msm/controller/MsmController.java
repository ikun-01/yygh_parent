package com.jing.yygh.msm.controller;

import cn.hutool.core.util.RandomUtil;
import com.jing.yygh.common.result.R;
import com.jing.yygh.msm.service.MsmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
@Api(description = "发送短信验证码接口")
public class MsmController {
    @Autowired
    private MsmService msmService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @ApiOperation("发送短信验证码")
    @GetMapping("/send/{phone}")
    public R sendCode(@PathVariable String phone){
        // 从redis中查询  如果存在不重复发送
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)){
            return R.ok();
        }
        // 生成验证码
        code = RandomUtil.randomNumbers(6);
        // 发送短信
        boolean isSend = msmService.send(phone, code);
        if (isSend) {
            // 存入redis中设置超时时间为 5minute
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.ok();
        }
        return R.error().message("发送短信失败");
    }
}
