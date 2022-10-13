package com.jing.yygh.hosp.controller;

import com.jing.yygh.common.result.R;
import com.jing.yygh.hosp.service.DepartmentService;
import com.jing.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
@Api(description = "科室接口")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/getDeptList/{hoscode}")
    public R getDeptList(@PathVariable("hoscode") String hoscode){

        List<DepartmentVo> depTree = departmentService.findDepTree(hoscode);

        return R.ok().data("list",depTree);
    }
}


