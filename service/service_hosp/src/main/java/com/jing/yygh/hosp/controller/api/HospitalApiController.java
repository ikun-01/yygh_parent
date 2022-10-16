package com.jing.yygh.hosp.controller.api;


import com.jing.yygh.common.result.R;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.vo.hosp.DepartmentVo;
import com.jing.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
@Api(description = "前台展示医院信息")
public class HospitalApiController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/{page}/{limit}")
    @ApiOperation("用户页面展示医院信息")
    public R index(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages",hospitals);
    }


    @GetMapping("/findByHosname/{hosname}")
    @ApiOperation("根据医院名称模糊查询")
    public R findByHosname(@PathVariable("hosname") String hosname){
        List<Hospital> list = hospitalService.findByName(hosname);
        return R.ok().data("list",list);
    }

    @ApiOperation("获取医院的详细信息")
    @GetMapping("/{hoscode}")
    public R item(@PathVariable("hoscode") String hoscode){
        Map<String, Object> item = hospitalService.item(hoscode);
        return R.ok().data(item);
    }

    @ApiOperation("显示医院科室列表信息")
    @GetMapping("/department/{hoscode}")
    public R index(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> depTree = departmentService.findDepTree(hoscode);
        return R.ok().data("list",depTree);
    }

}

