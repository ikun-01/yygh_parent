package com.jing.yygh.user.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.model.user.Patient;
import com.jing.yygh.user.service.PatientService;
import com.jing.yygh.user.utils.AuthContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/patient")
@Api(description = "就诊人管理接口")
public class PatientController {
    @Autowired
    private PatientService patientService;


    //获取就诊人列表
    @GetMapping("/auth/findAll")
    @ApiOperation("获取就诊人列表")
    public R findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.findListByUserId(userId);
        return R.ok().data("list",list);
    }

    //添加就诊人信息
    @PostMapping("/auth/save")
    @ApiOperation("添加就诊人信息")
    public R savePatient(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //根据id获取就诊人信息
    @GetMapping("/auth/get/{id}")
    @ApiOperation("根据id获取就诊人信息")
    public R getPatientById(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return R.ok().data("patient",patient);
    }

    //修改就诊人信息
    @PutMapping("/auth/update")
    @ApiOperation("修改就诊人信息")
    public R update(@RequestBody Patient patient){
        boolean update = patientService.updateById(patient);
        return update ? R.ok() : R.error();
    }

    //删除就诊人信息
    @DeleteMapping("/auth/remove/{id}")
    @ApiOperation("删除就诊人信息")
    public R remove(@PathVariable("id") Long id){
        boolean remove = patientService.removeById(id);
        return remove ? R.ok() : R.error();
    }


}
