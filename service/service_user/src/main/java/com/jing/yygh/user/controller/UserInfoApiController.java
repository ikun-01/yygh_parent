package com.jing.yygh.user.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.user.service.UserInfoService;
import com.jing.yygh.user.utils.AuthContextHolder;
import com.jing.yygh.vo.user.LoginVo;
import com.jing.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("/auth/userAuth")
    @ApiOperation("用户认证")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){
        // 从请求头中获取token
        Long userId = AuthContextHolder.getUserId(request);
        userInfoService.userAuth(userId,userAuthVo);
        return R.ok();
    }

    @GetMapping("/auth/getUserInfo")
    @ApiOperation("获取用户信息")
    public R getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getUserInfo(userId);
        return R.ok().data("userInfo",userInfo);
    }
}
