package com.jing.yygh.msm.service;

public interface MsmService {
    //发送验证码
    boolean send(String phone,String code);
}
