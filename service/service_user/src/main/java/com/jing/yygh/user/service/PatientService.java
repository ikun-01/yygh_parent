package com.jing.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    // 获取就诊人列表 根据当前用户信息
    List<Patient> findListByUserId(Long userId);

    // 查询就诊人详细信息
    Patient getPatientById(Long id);
}
