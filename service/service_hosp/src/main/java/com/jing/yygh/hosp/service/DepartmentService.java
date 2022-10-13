package com.jing.yygh.hosp.service;

import com.jing.yygh.model.hosp.Department;
import com.jing.yygh.vo.hosp.DepartmentQueryVo;
import com.jing.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DepartmentService {
    // 上传科室信息
    void save(Department department);

    // 查询科室信息
    Page<Department> selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo);

    // 删除科室
    void remove(String hoscode,String depcode);

    // 查询所有的科室
    List<DepartmentVo> findDepTree(String hoscode);

    // 查询科室名称 根据医院编号和科室编号
    String getDepName(String hoscode, String depcode);
}
