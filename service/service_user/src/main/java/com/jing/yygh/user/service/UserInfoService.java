package com.jing.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.vo.user.LoginVo;
import com.jing.yygh.vo.user.UserAuthVo;
import com.jing.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    UserInfo selectWxInfoByOpenId(String openId);

    /**
     * 用户认证
     * @param id
     * @param userAuthVo
     */
    void userAuth(Long id, UserAuthVo userAuthVo);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    UserInfo getUserInfo(Long userId);

    /**
     * 查询所有的用户信息 分页
     * @param pageParam
     * @param userInfoQueryVo
     * @return
     */
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    /**
     * 锁定用户
     * @param id
     * @param status
     */
    void lock(Long id,Integer status);

    /**
     * 查看用户详情
     * @param userId
     * @return
     */
    Map<String,Object> show(Long userId);

    /**
     * 用户认证审批
     * 2 通过  -1 不通过
     * @param userId
     * @param authStatus
     */
    void approval(Long userId, Integer authStatus);
}
