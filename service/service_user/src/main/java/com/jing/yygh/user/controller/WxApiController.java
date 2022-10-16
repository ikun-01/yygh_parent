package com.jing.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.common.result.R;
import com.jing.yygh.common.utils.JwtHelper;
import com.jing.yygh.model.user.UserInfo;
import com.jing.yygh.user.service.UserInfoService;
import com.jing.yygh.user.utils.ConstantPropertiesUtil;
import com.jing.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller // 进行视图跳转 回调不返回json
@RequestMapping("/api/user/wx")
public class WxApiController {

    @Autowired
    private UserInfoService userInfoService;

    // 获取微信登录参数
    @GetMapping("/getLoginParam")
    @ResponseBody
    public R getLoginParam(){
        Map<String, Object> map = new HashMap<>();
        try {
            String redirectUri= URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
            map.put("redirectUri",redirectUri); // 微信扫码后 点击确认重定向的 url 地址,携带code
            map.put("appid",ConstantPropertiesUtil.WX_OPEN_APP_ID);
            map.put("scope","snsapi_login");
            map.put("state",System.currentTimeMillis()+"");
        } catch (UnsupportedEncodingException e) {
            throw new YyghException(20001,"字符集转换异常");
        }
        return R.ok().data(map);
    }

    @GetMapping("/callback")
    public String callback(String code){

        // 通过code 来获取 access_token  + open_id
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + ConstantPropertiesUtil.WX_OPEN_APP_ID +
                "&secret=" + ConstantPropertiesUtil.WX_OPEN_APP_SECRET +
                "&code=" + code +
                "&grant_type=authorization_code";
        // 发送请求获取

        String result;
        try {
            result = HttpClientUtils.get(url);
        } catch (Exception e) {
            throw new YyghException(20001,"获取微信请求信息失败!");
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String access_token = jsonObject.getString("access_token");
        String openId = jsonObject.getString("openid");
        // 根据 open_id查询是否存在该用户
        UserInfo userInfo = userInfoService.selectWxInfoByOpenId(openId);
        // 用户不存在,进行注册
        if (userInfo == null){
            userInfo = new UserInfo();
            // 根据open_id查询 微信用户的信息
            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="
                    + access_token +
                    "&openid=" + openId;
            String wxInfo;
            try {
                wxInfo = HttpClientUtils.get(userInfoUrl);
            } catch (Exception e) {
                throw new YyghException(20001,"获取微信用户信息异常!");
            }
            JSONObject wxObject = JSONObject.parseObject(wxInfo);
            // 获取昵称
            String nickName = wxObject.getString("nickname");

            userInfo.setNickName(nickName);
            userInfo.setOpenid(openId);
            userInfo.setStatus(1);
            userInfo.setCreateTime(new Date());
            userInfo.setUpdateTime(new Date());
            // 微信用户注册
            userInfoService.save(userInfo);
        }
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
        }
        String token = JwtHelper.createToken(userInfo.getId(),name);

        // 当userInfo中 phone字段有值时,说明已经完成了手机绑定 不传递open_id
        // userInfo 中phone字段没有值 ,未绑定手机号,传递open_id

        //重定向到一个vue视图
        try {
            return  "redirect:http://localhost:3000/weixin/callback?"+
                    "name="+URLEncoder.encode(name,"UTF-8") +
                    "&token="+token+
                    "&openid=" + ((StringUtils.isEmpty(userInfo.getPhone())) ? userInfo.getOpenid() : "");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
