package com.jing.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.common.utils.JwtHelper;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.user.mapper.UserInfoMapper;
import com.jing.yygh.user.service.UserInfoService;
import com.jing.yygh.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        if (loginVo == null){
            throw new YyghException(20001,"登录信息为null");
        }
        //获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(20001,"手机号或验证码为空");
        }
        // 校验手机号和验证码
        String redis_code = redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(redis_code)){
            throw new YyghException(20001,"请先发送验证码");
        }
        if (! redis_code.equals(code)){
            throw new YyghException(20001,"验证码错误,请重新输入");
        }

        // openid为空说明手机号 + 验证码登录
        if (StringUtils.isEmpty(openid)){
            // 判断该手机号是否完成注册
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone",phone);
            UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
            if (userInfo == null){
                // 用户没有注册 进行注册
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                userInfo.setCreateTime(new Date());
                userInfo.setUpdateTime(new Date());
                userInfoMapper.insert(userInfo);
            }
            // 判断是否被锁定
            if (userInfo.getStatus() == 0){
                throw new YyghException(20001,"该用户已被锁定,无法登录");
            }
//            String name = "";
//            // 用户名为空
//            name = userInfo.getName();
//            if (StringUtils.isEmpty(name)){
//                // 使用微信昵称
//                name = userInfo.getNickName();
//                if (StringUtils.isEmpty(name)){
//                    // 使用手机号
//                    name = userInfo.getPhone();
//                }
//                HashMap<String, Object> map = new HashMap<>();
//                // 生成token
//                String token = JwtHelper.createToken(userInfo.getId(), name);
//                map.put("name",name);
//                map.put("token",token);
//                return map;
//            }
            return get(userInfo);
        }else {
            // 先扫码,然后callback方法回调 ->绑定手机号
            //根据openid可以从数据库中查询用户    -一定查询到  先走callback方法保存用户信息
            UserInfo userInfo_wx = selectWxInfoByOpenId(openid);
            // 根据手机号查找该手机号是否被 注册
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone",phone);
            UserInfo userInfo_phone = userInfoMapper.selectOne(queryWrapper);
            if (userInfo_phone == null){
                // 没有被注册
                userInfo_wx.setPhone(phone);
                userInfo_wx.setUpdateTime(new Date());
                userInfoMapper.updateById(userInfo_wx);
            } else {
                // 手机号被使用过 userInfo_phone 和userInfo_wx合并
                userInfo_wx.setCertificatesNo(userInfo_phone.getCertificatesNo());
                userInfo_wx.setName(userInfo_phone.getName());
                userInfo_wx.setPhone(userInfo_phone.getPhone());
                userInfo_wx.setCertificatesUrl(userInfo_phone.getCertificatesUrl());
                userInfo_wx.setCertificatesType(userInfo_phone.getCertificatesType());
                // 删除手机号用户
                userInfoMapper.deleteById(userInfo_phone.getId());
                // 更新
                userInfoMapper.updateById(userInfo_wx);
            }
            return get(userInfo_wx);
        }

    }

    @Override
    public UserInfo selectWxInfoByOpenId(String openId) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openId);
        return userInfoMapper.selectOne(queryWrapper);
    }


    private Map<String,Object> get(UserInfo userInfo){
        HashMap<String, Object> map = new HashMap<>();
        // 用户名为空
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)){
            // 使用微信昵称
            name = userInfo.getNickName();
            if (StringUtils.isEmpty(name)){
                // 使用手机号
                name = userInfo.getPhone();
            }

        }
        // 生成token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("name",name);
        map.put("token",token);
        return map;
    }
}
