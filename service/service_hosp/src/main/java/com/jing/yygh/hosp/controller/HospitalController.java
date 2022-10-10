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

@Api(description = "后台管理医院列列表接口")
@RestController
@CrossOrigin//跨域请求
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
}
