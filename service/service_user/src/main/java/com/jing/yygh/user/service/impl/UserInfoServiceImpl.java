package com.jing.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.common.utils.JwtHelper;
import com.jing.yygh.enums.AuthStatusEnum;
import com.jing.yygh.model.user.Patient;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.user.mapper.UserInfoMapper;
import com.jing.yygh.user.service.PatientService;
import com.jing.yygh.user.service.UserInfoService;
import com.jing.yygh.vo.user.LoginVo;
import com.jing.yygh.vo.user.UserAuthVo;
import com.jing.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;
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

    @Override
    public void userAuth(Long id, UserAuthVo userAuthVo) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        if (userInfo == null){
            throw new YyghException(20001,"用户不存在");
        }
        if (userAuthVo == null){
            throw new YyghException(20001,"用户数据为null");
        }
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        // 修改认证状态为认证中
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null){
            throw new YyghException(20001,"用户不存在");
        }
        // 设置 认证状态 0：未认证 1：认证中 2：认证成功 -1：认证失败
        String authStatusString = AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus());

//        AuthStatusEnum[] values = AuthStatusEnum.values();
//        // 遍历状态枚举集合 获取name
//        for (AuthStatusEnum value : values) {
//            if (value.getStatus().intValue() == userInfo.getStatus().intValue()){
//                authStatusString = value.getName();
//            }
//        }
        userInfo.getParam().put("authStatusString",authStatusString);
        return userInfo;
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        if (userInfoQueryVo == null) {
            Page<UserInfo> result = userInfoMapper.selectPage(pageParam, queryWrapper);
            result.getRecords().forEach(userInfo -> {
                pack(userInfo);
            });
            return result;
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getKeyword())) {
            queryWrapper.like("name",userInfoQueryVo.getKeyword());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getStatus())) {
            queryWrapper.like("status",userInfoQueryVo.getStatus());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getAuthStatus())) {
            queryWrapper.eq("auth_status",userInfoQueryVo.getAuthStatus());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getCreateTimeBegin())) {
            queryWrapper.ge("create_time",userInfoQueryVo.getCreateTimeBegin());
        }
        if (!StringUtils.isEmpty(userInfoQueryVo.getCreateTimeEnd())) {
            queryWrapper.le("create_time",userInfoQueryVo.getCreateTimeEnd());
        }
        // 调用分页查询
        Page<UserInfo> userInfoPage = userInfoMapper.selectPage(pageParam, queryWrapper);
        // 封装参数
        userInfoPage.getRecords().forEach(userInfo -> {
            pack(userInfo);
        });

        return userInfoPage;
    }

    @Override
    public void lock(Long id, Integer status) {
        if (status.intValue() != 1 && status.intValue() != 0){
            throw new YyghException(20001,"状态信息只能为0或1");
        }
        UserInfo userInfo = userInfoMapper.selectById(id);
        if (userInfo == null){
            throw new YyghException(20001,"用户不存在");
        }
        userInfo.setStatus(status);
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public Map<String, Object> show(Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null){
            throw new YyghException(20001,"用户信息不存在");
        }
        HashMap<String, Object> map = new HashMap<>();
        pack(userInfo);
        map.put("userInfo",userInfo);

        // 查询所有的就诊人
        List<Patient> list = patientService.findListByUserId(userInfo.getId());
        map.put("patientList",list);

        return map;
    }

    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus == 2 || authStatus == -1){
            UserInfo userInfo = userInfoMapper.selectById(userId);
            if (userInfo == null){
                throw new YyghException(20001,"用户信息不存在");
            }
            userInfo.setAuthStatus(authStatus);
            userInfoMapper.updateById(userInfo);
        }
    }

    private void pack(UserInfo userInfo) {
        Integer status = userInfo.getStatus();
        Integer authStatus = userInfo.getAuthStatus();
        userInfo.getParam().put("statusString",status.intValue() == 0 ? "锁定" : "正常");
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(authStatus));
    }


    // 封装返回结果,生成token
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
