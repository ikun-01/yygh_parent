package com.jing.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jing.yygh.common.result.R;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.user.service.UserInfoService;
import com.jing.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController()
@Api(description = "后台用户管理") // 管理员使用
@RequestMapping("/admin/user")
public class UserController{
    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/{page}/{limit}")
    @ApiOperation("用户列表,后端管理员使用")
    public R getList(@PathVariable("page") Long page, @PathVariable("limit") Long limit, UserInfoQueryVo userInfoQueryVo){

        IPage<UserInfo> pageResult = userInfoService.selectPage(new Page<UserInfo>(page, limit), userInfoQueryVo);

        return R.ok().data("pageModel",pageResult);
    }

    @GetMapping("/lock/{userId}/{status}")
    @ApiOperation("锁定和解锁用户")
    public R lock(@PathVariable("userId") Long id, @PathVariable("status") Integer status){
        userInfoService.lock(id,status);
        return R.ok();
    }

    @GetMapping("/show/{userId}")
    @ApiOperation("查看用户信息")
    public R show(@PathVariable("userId")Long id){
        Map<String, Object> map = userInfoService.show(id);
        return R.ok().data(map);
    }

    @GetMapping("/approval/{userId}/{authStatus}")
    @ApiOperation("审批用户")
    public R approval(@PathVariable("userId") Long id,@PathVariable("authStatus") Integer authStatus){
        userInfoService.approval(id,authStatus);
        return R.ok();
    }
}
