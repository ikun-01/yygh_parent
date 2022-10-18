package com.jing.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.cmn.client.DictFeignClient;
import com.jing.yygh.enums.DictEnum;
import com.jing.yygh.model.user.Patient;
import com.jing.yygh.user.mapper.PatientMapper;
import com.jing.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService{
    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private DictFeignClient dictFeignClient;


    @Override
    public List<Patient> findListByUserId(Long userId) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id",userId);

        List<Patient> patientList = patientMapper.selectList(queryWrapper);

        patientList.forEach(patient -> {
            packPatient(patient);
        });
        return patientList;
    }

    @Override
    public Patient getPatientById(Long id) {
        Patient patient = patientMapper.selectById(id);
        packPatient(patient);
        return patient;
    }


    private void packPatient(Patient patient) {
        // 省市区
        String cityName = dictFeignClient.getName(patient.getCityCode());
        String provinceName = dictFeignClient.getName(patient.getProvinceCode());
        String districtName = dictFeignClient.getName(patient.getDistrictCode());
        // 就诊人身份类型
        String certificatesType = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        // 联系人身份类型
        String contactsCertificatesType = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getContactsCertificatesType());

        patient.getParam().put("certificatesTypeString",certificatesType);
        patient.getParam().put("contactsCertificatesTypeString",contactsCertificatesType);
        patient.getParam().put("provinceString",provinceName);
        patient.getParam().put("cityString",cityName);
        patient.getParam().put("districtString",districtName);
        patient.getParam().put("fullAddress",provinceName + cityName + districtName + patient.getAddress());
    }
}
