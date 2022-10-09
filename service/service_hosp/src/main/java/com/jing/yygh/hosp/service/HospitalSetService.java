package com.jing.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.hosp.HospitalSet;

public interface HospitalSetService extends IService<HospitalSet> {
    /**
     * 根据医院编码获取SignKey
     * @param hoscode
     * @return
     */
    String getSignKey(String hoscode);
}
