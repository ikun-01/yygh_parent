package com.jing.yygh.hosp.controller;

import com.jing.yygh.common.result.R;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hosp")
//@CrossOrigin  //跨域
public class UserLoginController {
    //登录
    @PostMapping("/user/login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    @GetMapping("/user/info")
    public R info(){
        return R.ok().data("roles","admin")
                .data("introduction","I am a super administrator")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","Super Admin");
    }

}
