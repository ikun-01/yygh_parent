package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

public interface HospitalService {
    //第三方医院调用,实现上传医院信息
    void save(Hospital hospital);
    // 查找医院信息 根据医院编码
    Hospital getByHoscode(String hoscode);
    // 带条件的分页查询,后台管理系统使用
    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);
}
