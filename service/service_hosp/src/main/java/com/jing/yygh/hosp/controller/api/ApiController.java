package com.jing.yygh.hosp.controller.api;

import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.common.result.Result;
import com.jing.yygh.common.utils.MD5;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.hosp.service.HospitalSetService;
import com.jing.yygh.hosp.service.ScheduleService;
import com.jing.yygh.model.hosp.Department;
import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.model.hosp.Schedule;
import com.jing.yygh.vo.hosp.DepartmentQueryVo;
import com.jing.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/api/hosp")
@Api(description = "医院管理Api接口")
@RestController
public class ApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/saveHospital")
    @ApiOperation("医院设置上传")
    public Result saveHospital(Hospital hospital, HttpServletRequest request){

        //参数校验
        if (hospital == null){
            throw new YyghException(20001,"传入信息为空");
        }

        if (StringUtils.isEmpty(hospital.getHoscode())){
            throw new YyghException(20001,"医院编码不能为空");
        }
        //签名校验
        // 获取医院端传过来经过MD5加密过后的签名
        String hospKey = request.getParameter("sign");
        // 根据传递的医院编码在数据库中查找签名
        String signKey = hospitalSetService.getSignKey(hospital.getHoscode());
        //MD5加密
        signKey = MD5.encrypt(signKey);
        //进行签名校验
        if (!signKey.equalsIgnoreCase(hospKey)) {
            throw new YyghException(20001,"签名校验失败");
        }
        //将logoData中的空格替换成"+"
        hospital.setLogoData(hospital.getLogoData().replaceAll(" ","+"));
        hospitalService.save(hospital);

        return Result.ok();
    }

    @PostMapping("/hospital/show")
    @ApiOperation("查询医院信息")
    public Result hospital(@RequestParam("hoscode") String hoscode){
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }


    @PostMapping("/saveDepartment")
    @ApiOperation("上传科室信息")
    public Result saveDepartment(Department department){
        departmentService.save(department);
        return Result.ok();
    }

    @PostMapping("/department/list")
    @ApiOperation("查询科室信息")
    public Result department(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo){
        Page<Department> pages = departmentService.selectPage(page, limit, departmentQueryVo);
        return Result.ok(pages);
    }

    @PostMapping("/department/remove")
    @ApiOperation("删除科室信息")
    public Result removeDepartment(String hoscode,String depcode){
        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }

    @PostMapping("/saveSchedule")
    @ApiOperation("上传排班信息")
    public Result saveSchedule(Schedule schedule){
        scheduleService.save(schedule);
        return Result.ok();
    }

    @PostMapping("/schedule/list")
    @ApiOperation("查询排班信息")
    public Result schedule(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo){
        Page<Schedule> pages = scheduleService.selectPage(page, limit, scheduleQueryVo);
        return Result.ok(pages);
    }

    @PostMapping("/schedule/remove")
    @ApiOperation("删除排班信息")
    public Result removeSchedule(String hoscode,String hosScheduleId){
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

}
