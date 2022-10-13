package com.jing.yygh.hosp.service.impl;

import com.jing.yygh.cmn.client.DictFeignClient;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.hosp.repository.HospitalRepository;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired //openFeign远程调用
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Hospital hospital) {
        //判断医院信息是否已经添加
        Hospital exitHospital = hospitalRepository.getByHoscode(hospital.getHoscode());

        //如果已经添加,则执行修改操作
        if (exitHospital!=null){
            hospital.setId(exitHospital.getId());
            hospital.setCreateTime(exitHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(1);
        } else {
            hospital.setCreateTime(new Date());
            hospital.setStatus(1);
            hospital.setUpdateTime(new Date());
        }
        //执行添加操作
        hospitalRepository.save(hospital);
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        if (StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编码为空");
        }

        return hospitalRepository.getByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        if (page == null || page < 0){
            page = 0;
        }
        if (limit == null || limit < 1){
            limit = 10;
        }
        Hospital hospital = new Hospital();
        //模糊匹配
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);

        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        //条件
        Example<Hospital> example = Example.of(hospital,matcher);
        //排序
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //分页
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        Page<Hospital> pageResult = hospitalRepository.findAll(example, pageable);
        pageResult.getContent().forEach(hosp -> {
            packHospital(hosp);
        });
        return pageResult;
    }

    /**
     * 修改医院状态信息
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(String id, Integer status) {
        if (StringUtils.isEmpty(id)){
            throw new YyghException(20001,"id为null");
        }
        if (status.intValue()!=0 && status.intValue()!=1){
            throw new YyghException(20001,"状态错误");
        }
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    /**
     * 查看医院详细信息
     * @param id
     * @return
     */
    @Override
    public Map<String, Object> show(String id) {
        if (StringUtils.isEmpty(id)){
            throw new YyghException(20001,"id为null");
        }
        HashMap<String, Object> map = new HashMap<>();
        Hospital hospital = hospitalRepository.findById(id).get();
        this.packHospital(hospital);
        //医院基本信息
        map.put("hospital",hospital);
        //预约规则
        map.put("bookingRule",hospital.getBookingRule());

        return map;
    }


    /**
     *  封装 医院的其他参数
     * @param hospital
     * @return
     */
    private Hospital packHospital(Hospital hospital){
        // 医院类型名称
        String hosTypeName = dictFeignClient.getName("hostype", hospital.getHostype());
        hospital.getParam().put("hosType",hosTypeName);
        //市
        String cityName = dictFeignClient.getName(hospital.getCityCode());
        //省
        String provinceName = dictFeignClient.getName(hospital.getProvinceCode());
        //区
        String districtName = dictFeignClient.getName(hospital.getDistrictCode());
        String fullAddress = provinceName + cityName + districtName + hospital.getAddress();
        hospital.getParam().put("fullAddress",fullAddress);
        return hospital;
    }

}
