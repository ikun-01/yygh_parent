package com.jing.yygh.hosp.controller.api;

import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.common.result.Result;
import com.jing.yygh.common.utils.MD5;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.hosp.service.HospitalSetService;
import com.jing.yygh.model.hosp.Hospital;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/api/hosp")
@Api(description = "医院管理Api接口")
@RestController
public class ApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;

    @PostMapping("/saveHospital")
    public Result saveHospital(Hospital hospital, HttpServletRequest request){

        //参数校验
        if (hospital == null){
            throw new YyghException(20001,"传入信息为空");
        }

        if (StringUtils.isEmpty(hospital.getHoscode())){
            throw new YyghException(20001,"医院编码不能为空");
        }
        //签名校验
        // 获取医院端传过来经过MD5加密过后的签名
        String hospKey = request.getParameter("sign");
        // 根据传递的医院编码在数据库中查找签名
        String signKey = hospitalSetService.getSignKey(hospital.getHoscode());
        //MD5加密
        signKey = MD5.encrypt(signKey);
        //进行签名校验
        if (!signKey.equalsIgnoreCase(hospKey)) {
            throw new YyghException(20001,"签名校验失败");
        }
        //将logoData中的空格替换成"+"
        hospital.setLogoData(hospital.getLogoData().replaceAll(" ","+"));
        hospitalService.save(hospital);

        return Result.ok();
    }

}
