package com.jing.yygh.hosp.repository;

import com.jing.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {

    //通过 医院编号查找医院信息
    Hospital getByHoscode(String hoscode);
}
