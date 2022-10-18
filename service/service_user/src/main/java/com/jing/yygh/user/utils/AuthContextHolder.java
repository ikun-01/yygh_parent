package com.jing.yygh.user.utils;

import com.jing.yygh.common.utils.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求头中获取用户信息的工具类
 */
public class AuthContextHolder {

    /**
     * 获取用户id
     * @param request
     * @return
     */
    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        return JwtHelper.getUserId(token);
    }

    /**
     * 获取用户姓名
     * @param request
     * @return
     */
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        return JwtHelper.getUserName(token);
    }

}
