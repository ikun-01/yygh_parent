package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Hospital;

public interface HospitalService {
    //第三方医院调用,实现上传医院信息
    void save(Hospital hospital);
}
