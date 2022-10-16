package com.jing.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.vo.user.LoginVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    UserInfo selectWxInfoByOpenId(String openId);
}
