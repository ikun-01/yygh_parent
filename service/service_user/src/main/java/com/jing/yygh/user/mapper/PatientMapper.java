package com.jing.yygh.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jing.yygh.model.user.Patient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}
