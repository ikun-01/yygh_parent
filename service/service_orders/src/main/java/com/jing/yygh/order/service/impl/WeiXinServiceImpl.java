package com.jing.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.jing.yygh.enums.PaymentTypeEnum;
import com.jing.yygh.enums.RefundStatusEnum;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.model.order.PaymentInfo;
import com.jing.yygh.model.order.RefundInfo;
import com.jing.yygh.order.service.OrderService;
import com.jing.yygh.order.service.PaymentService;
import com.jing.yygh.order.service.RefundInfoService;
import com.jing.yygh.order.service.WeiXinService;
import com.jing.yygh.order.utils.ConstantPropertiesUtils;
import com.jing.yygh.order.utils.HttpClient;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeiXinServiceImpl implements WeiXinService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundInfoService refundInfoService;

    @Override
    public Map createNative(Long orderId) {
        // 从 redis中查看是否已有支付链接,如果有直接返回
        Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
        if (payMap != null){
            return payMap;
        }

        // 根据id获取订单信息
        OrderInfo orderInfo = orderService.getById(orderId);
        // 保存交易记录
        paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());

        // 调用微信 下单接口
        // 封装请求参数
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", ConstantPropertiesUtils.APPID); // APPID
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER); // 商户id
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
        // 就诊安排日期
        Date reserveDate = orderInfo.getReserveDate();
        String reserveDateStr = new DateTime(reserveDate).toString("yyyy/MM/dd");
        String body = reserveDateStr + "就诊" + orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        paramMap.put("total_fee", "1");
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type", "NATIVE");

        // 讲请求参数转成 xml 格式,微信端接口调用使用  携带sign签名
        String xmlParam = null;
        try {
            xmlParam = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            // 统一调用下单接口
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder",paramMap);
            httpClient.setXmlParam(xmlParam); // 设置请求参数
            httpClient.setHttps(true); // 设置https
            httpClient.post(); // 使用post请求

            // 返回调用结果
            String content = httpClient.getContent();
            // 将返回结果转成map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            String return_code = resultMap.get("return_code"); // 支付状态码
            String code_url = resultMap.get("code_url");  // 支付链接

            // 封装返回
            payMap = new HashMap<>();
            payMap.put("orderId",orderId);
            payMap.put("totalFee",orderInfo.getAmount());
            payMap.put("returnCode",return_code);
            payMap.put("codeUrl",code_url);

            // 将订单信息暂存到redis中 5分钟
            redisTemplate.opsForValue().set(orderId.toString(),payMap,5, TimeUnit.MINUTES);
            return payMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    @Override
    public Map queryPayStatus(Long orderId) {
        OrderInfo order = orderService.getById(orderId);

        // 封装请求参数
        Map paramMap = new HashMap();
        paramMap.put("appid",ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);
        paramMap.put("out_trade_no",order.getOutTradeNo());
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
//        paramMap.put("sign",ConstantPropertiesUtils.PARTNERKEY);

        // 将请求参数转换成xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            //  设置请求,调用微信查询接口
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setXmlParam(xmlParam);
            httpClient.setHttps(true);
            httpClient.post();

            // 调用返回结果
            String result = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Boolean refund(Long orderId) {
        // 查询 支付记录
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        // 根据 支付记录创建 退款记录
        RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
        // 判断是否 已经退款
        if (RefundStatusEnum.REFUND.getStatus().intValue() == refundInfo.getRefundStatus().intValue()){
            return true;
        }
        // 封装退款参数
        Map<String,String>  paramMap = new HashMap<>();
        paramMap.put("appid",ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("out_trade_no", paymentInfo.getOutTradeNo());
        paramMap.put("transaction_id",paymentInfo.getTradeNo());
        paramMap.put("out_refund_no","tk" + paymentInfo.getOutTradeNo());
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");

        // 将参数转换为xml
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            httpClient.setXmlParam(paramXml);
            httpClient.setCertPassword(ConstantPropertiesUtils.PARTNER);
            httpClient.setCert(true);
            httpClient.setHttps(true);
            httpClient.post();

            String result = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);

            // 更新退款记录状态
            if (resultMap != null && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))){
                refundInfo.setTradeNo(resultMap.get("refund_id")); // 微信退款单号
                refundInfo.setCallbackTime(new Date());
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));

                refundInfoService.updateById(refundInfo);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
