package com.jing.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.enums.OrderStatusEnum;
import com.jing.yygh.hosp.HospitalFeignClient;
import com.jing.yygh.model.hosp.HospitalSet;
import com.jing.yygh.model.order.OrderInfo;
import com.jing.yygh.model.user.Patient;
import com.jing.yygh.order.mapper.OrderInfoMapper;
import com.jing.yygh.order.service.OrderService;
import com.jing.yygh.order.service.WeiXinService;
import com.jing.yygh.order.utils.HttpRequestHelper;
import com.jing.yygh.rabbitmq.consts.MqConst;
import com.jing.yygh.rabbitmq.service.RabbitService;
import com.jing.yygh.user.client.PatientFeignClient;
import com.jing.yygh.vo.hosp.ScheduleOrderVo;
import com.jing.yygh.vo.msm.MsmVo;
import com.jing.yygh.vo.order.OrderCountQueryVo;
import com.jing.yygh.vo.order.OrderCountVo;
import com.jing.yygh.vo.order.OrderMqVo;
import com.jing.yygh.vo.order.OrderQueryVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
    @Autowired
    private PatientFeignClient patientFeignClient;
    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private WeiXinService weiXinService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;


    /**
     * 创建订单
     * @param scheduleId
     * @param patientId
     * @return
     */
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        // 获取创建订单所需要的数据
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        // 获取就诊人
        Patient patient = patientFeignClient.getPatientById(patientId);
        HospitalSet hospitalSet = hospitalFeignClient.getHospitalSet(scheduleOrderVo.getHoscode());
        // 向医院端发送请求
        String url = hospitalSet.getApiUrl() + "/order/submitOrder";
        // 封装请求参数
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",scheduleOrderVo.getHoscode());
        paramMap.put("depcode",scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate",new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount",scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        paramMap.put("sign", "");
        
        // 发送请求 调用医院端接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, url);
        // 医院端返回
        if ("200".equals(result.getString("code"))){
            // 获取医院端请求数据
            JSONObject data = result.getJSONObject("data");

            String hosRecordId = data.getString("hosRecordId"); // 医院端预约记录唯一标识 - 主键
            Integer number = data.getInteger("number"); // 预约序号
            String fetchTime = data.getString("fetchTime"); // 取号时间
            String fetchAddress = data.getString("fetchAddress"); // 取号地址

            // 封装平台对象
            OrderInfo orderInfo = new OrderInfo();
            BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
            // 交易订单号,唯一
            orderInfo.setOutTradeNo(System.currentTimeMillis() + "" + new Random().nextInt(100));
            orderInfo.setScheduleId(scheduleId); // mongodb中的排班
            orderInfo.setPatientId(patientId); // 就诊人id
            orderInfo.setUserId(patient.getUserId());
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setHosRecordId(hosRecordId); // 预约记录 医院端主键id
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            // 平台端 存储订单
            this.save(orderInfo);
            // TODO (MQ) 使用消息中间件 更新mongdb中的数据 并向用户发送短信通知
            Integer reservedNumber = data.getInteger("reservedNumber"); // 排班可预约数
            Integer availableNumber = data.getInteger("availableNumber"); // 排班剩余预约数
            // 用户挂号成功后 => 第一个队列发送信息 => 医院服务接收信息 => 向mongodb中更新数据 => 向第二个队列中发送信息 => 短信服务接收信息 => 向用户发送通知短信
            afterSaveOrder(scheduleId, patient, reservedNumber, availableNumber);

            // 返回订单id
            return orderInfo.getId();
        } else {
            throw new YyghException(20001,"医院端发送请求异常");
        }

    }

    private void afterSaveOrder(String scheduleId, Patient patient, Integer reservedNumber, Integer availableNumber) {
        // 封装消息对象
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(scheduleId);
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setAvailableNumber(availableNumber);
        // 封装挂号成功后发送给用户的信息
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(patient.getPhone());
        msmVo.getParam().put("message","挂号成功");
        orderMqVo.setMsmVo(msmVo);
        // 发送消息
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
    }

    @Override
    public OrderInfo getOrderInfo(Long id) {
        OrderInfo orderInfo = this.getById(id);
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        if (orderQueryVo == null){
            return this.page(pageParam);
        }

        Long userId = orderQueryVo.getUserId();
        Long patientId = orderQueryVo.getPatientId(); // 就诊人id
        String patientName = orderQueryVo.getPatientName();
        String orderStatus = orderQueryVo.getOrderStatus(); // 订单状态
        String reserveDate = orderQueryVo.getReserveDate(); // 安排时间
        String hosname = orderQueryVo.getKeyword(); // 医院名称
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        String outTradeNo = orderQueryVo.getOutTradeNo();

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(userId)){
            queryWrapper.eq("user_id",userId);
        }
        if (!StringUtils.isEmpty(hosname)) {
            queryWrapper.like("hosname",hosname);
        }
        if (!StringUtils.isEmpty(patientId)) {
            queryWrapper.eq("patient_id",patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            queryWrapper.eq("order_status",orderStatus);
        }
        if (!StringUtils.isEmpty(outTradeNo)) {
            queryWrapper.eq("out_trade_no",outTradeNo);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            queryWrapper.ge("reserve_date",reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time",createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time",createTimeEnd);
        }
        IPage<OrderInfo> pages = this.page(pageParam, queryWrapper);
        Stream<OrderInfo> stream = pages.getRecords().stream();
        pages.getRecords().forEach(orderInfo -> {
            orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        });


        return pages;
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        // 查询平台端订单信息
        OrderInfo order = this.getById(orderId);
        // 判断是否已过退号时间
        DateTime quiteTime = new DateTime(order.getQuitTime());
        if (quiteTime.isBeforeNow()) {
            throw new YyghException(20001,"退号时间已过");
        }
        HospitalSet hospitalSet = hospitalFeignClient.getHospitalSet(order.getHoscode());
        // 调用医院端接口 进行退号处理
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",order.getHoscode());
        paramMap.put("hosRecordId",order.getHosRecordId());
        paramMap.put("timestamp",System.currentTimeMillis());
        paramMap.put("sign","");

        JSONObject result = HttpRequestHelper.sendRequest(paramMap, hospitalSet.getApiUrl() + "/order/updateCancelStatus");
        if (result.getInteger("code").intValue() != 200){
            throw new YyghException(20001,"退号失败");
        } else {
            // 判断订单是否已经支付
            if (order.getOrderStatus() == OrderStatusEnum.PAID.getStatus()){
                // 已经支付 需要微信端退款
                boolean isRefund = weiXinService.refund(orderId);
                if (!isRefund){
                    throw new YyghException(20001,"退款失败");
                }
            }
            // 更改平台端订单信息
            order.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            this.updateById(order);
            // 向MQ发送信息 更新号源数量和向用户发送信息
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(order.getScheduleId());
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(order.getPatientPhone());
            msmVo.getParam().put("message","取消预约成功");
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
        }

        return true;
    }

    @Override
    public void patientTips() {
        // 查询当天就诊的信息
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        List<OrderInfo> list = this.list(queryWrapper);
        list.forEach(orderInfo -> {
            // 短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.getParam().put("message","请及时就诊");
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        });
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {

        Map<String,Object> resultMap = new HashMap<>();

        List<OrderCountVo> orderCountList = orderInfoMapper.selectOrderCount(orderCountQueryVo);

        // 获取日期列表
        List<String> reserveDateList = orderCountList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());

        // 统计列表
        List<Integer> countList = orderCountList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());

        resultMap.put("dateList",reserveDateList);
        resultMap.put("countList",countList);

        return resultMap;
    }
}
