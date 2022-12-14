package com.jing.yygh.hosp.service.impl;

import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.hosp.repository.DepartmentRepository;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.model.hosp.Department;
import com.jing.yygh.vo.hosp.DepartmentQueryVo;
import com.jing.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Override
    public void save(Department department) {
        //参数校验
        if (department == null) {
            throw new YyghException(20001,"参数不能为空");
        }

        if (StringUtils.isEmpty(department.getHoscode())
                || StringUtils.isEmpty(department.getDepcode())){
            throw new YyghException(20001,"医院编号和科室编号不能为空");
        }

        //根据医院编号和科室编号查询科室是否存在
        Department exitDepartment =
                departmentRepository.getByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        //不存在进行添加
        if (exitDepartment == null){
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
        }else {
            // 存在进行修改
            department.setCreateTime(exitDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setId(exitDepartment.getId());
        }
        departmentRepository.save(department);
    }

    @Override
    public Page<Department> selectPage(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {
        if (page == null || page < 0){
            page = 0;
        }
        if (limit == null || limit < 1){
            limit = 10;
        }

        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        //模糊匹配
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        //条件
        Example<Department> example = Example.of(department,matcher);
        //排序
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //分页
        Pageable pageable = PageRequest.of(page-1,limit,sort);//Mongodb默认从0开始
        return departmentRepository.findAll(example, pageable);
    }

    @Override
    public void remove(String hoscode, String depcode) {
        if (StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编号为空");
        }
        if (StringUtils.isEmpty(depcode)){
            throw new YyghException(20001,"科室编号为空");
        }
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode, depcode);
        if (department == null){
            throw new YyghException(20001,"该科室信息不存在");
        }
        departmentRepository.deleteById(department.getId());
    }


    /**
     * 查询所有的科室信息 (大科室)
     * @param hoscode 医院编码
     * @return
     */
    @Override
    public List<DepartmentVo> findDepTree(String hoscode) {

        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001,"医院编码为空");
        }
        //查询所有的小科室信息
        List<Department> smallDeptList = departmentRepository.getAllByHoscode(hoscode);

        //分组找出大科室信息
        //key 大科室的编号
        //value 同一个大科室下小科室的集合
        Map<String, List<Department>> map = smallDeptList.stream().collect
                (Collectors.groupingBy(Department::getBigcode));

        //大科室的集合
        ArrayList<DepartmentVo> bigList = new ArrayList<>();

        map.forEach((bigDepCode,smallDep) -> {
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            bigDepartmentVo.setDepcode(bigDepCode);//大科室的编码
            bigDepartmentVo.setDepname(smallDep.get(0).getBigname());//大科室的名称
            // 下级的小科室
            bigDepartmentVo.setChildren(transferSmallList(smallDep));
            bigList.add(bigDepartmentVo);
        });

        return bigList;
    }

    // 查询科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode, depcode);
        return department.getDepname();
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getByHoscodeAndDepcode(hoscode,depcode);
    }

    /**
     * 将Department 转换成 DepartmentVo
     * @param smallList
     * @return
     */

    private List<DepartmentVo> transferSmallList(List<Department> smallList){
        List<DepartmentVo> list = new ArrayList<>();
        smallList.forEach(department -> {
            DepartmentVo departmentVo = new DepartmentVo();
            BeanUtils.copyProperties(department,departmentVo);
            list.add(departmentVo);
        });
        return list;
    }
}
