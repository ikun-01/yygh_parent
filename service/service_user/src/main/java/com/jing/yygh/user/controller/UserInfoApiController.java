package com.jing.yygh.user.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.user.service.UserInfoService;
import com.jing.yygh.vo.user.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Api(description = "用户登录模块")
public class UserInfoApiController {
    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@RequestBody LoginVo loginVo){
        Map<String, Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }
}
