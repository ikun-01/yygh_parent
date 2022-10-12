package com.jing.yygh.hosp.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.hosp.service.HospitalService;
import com.jing.yygh.model.hosp.Hospital;
import com.jing.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(description = "后台管理医院列列表接口")
@RestController
//@CrossOrigin//跨域请求
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    @GetMapping("/{page}/{limit}")
    @ApiOperation("医院设置列表信息")
    public R index(@PathVariable("page") Integer page,@PathVariable("limit") Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pages = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages",pages);
    }

    @PutMapping("/updateStatus/{id}/{status}")
    @ApiOperation("更新医院状态信息")
    public R updateStatus(@PathVariable("id") String id,@PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    @GetMapping("/show/{id}")
    @ApiOperation("查看医院详情")
    public R show(@PathVariable("id") String id){
        Map<String, Object> hospital = hospitalService.show(id);
        return R.ok().data("hospital",hospital);
    }
}
