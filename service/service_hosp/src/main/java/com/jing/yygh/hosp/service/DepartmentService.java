package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Department;
import com.jing.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.data.domain.Page;

public interface DepartmentService {
    // 上传科室信息
    void save(Department department);

    // 查询科室信息
    Page<Department> selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo);

    // 删除科室
    void remove(String hoscode,String depcode);
}
