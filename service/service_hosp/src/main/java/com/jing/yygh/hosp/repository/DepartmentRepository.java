package com.jing.yygh.hosp.repository;

import com.jing.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    /**
     * 根据 医院编号和科室编号查询科室信息
     * @param hoscode
     * @param depcode
     * @return
     */
    Department getByHoscodeAndDepcode(String hoscode,String depcode);

}
