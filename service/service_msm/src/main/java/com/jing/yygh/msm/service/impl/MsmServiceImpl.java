package com.jing.yygh.msm.service.impl;

import com.jing.yygh.msm.service.MsmService;
import com.jing.yygh.msm.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {

    private static final String host = "http://dingxin.market.alicloudapi.com";
    private static final String path = "/dx/sendSms";
    private static final String method = "POST";
    private static final String appcode = "636dac4c782846e8a22bdce3cd610b0f";

    @Override
    public boolean send(String phone, String code) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phone);
        querys.put("param", "code:"+code);
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
