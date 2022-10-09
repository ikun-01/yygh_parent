package com.jing.yygh.hosp.service.impl;

import com.jing.yygh.hosp.repository.HospitalRepository;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;
    @Override
    public void save(Hospital hospital) {
        //判断医院信息是否已经添加
        Hospital exitHospital = hospitalRepository.getByHoscode(hospital.getHoscode());

        //如果已经添加,则执行修改操作
        if (exitHospital!=null){
            hospital.setId(exitHospital.getId());
            hospital.setCreateTime(hospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(1);
        } else {
            hospital.setCreateTime(new Date());
            hospital.setStatus(1);
            hospital.setUpdateTime(new Date());
        }
        //执行添加操作
        hospitalRepository.save(hospital);
    }
}
