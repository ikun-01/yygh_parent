package com.jing.yygh.cmn.controller;

import com.jing.yygh.cmn.service.DictService;
import com.jing.yygh.common.result.R;
import com.jing.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
@CrossOrigin//跨域
public class DictController {
    @Autowired
    private DictService dictService;


    /**
     * 根据数据查找子数据列表
     * @param id
     * @return
     */
    @ApiOperation("根据id查找子数据列表")
    @GetMapping("/findChildData/{id}")
    public R findChildData(@PathVariable("id") Long id){
        List<Dict> list = dictService.findChildData(id);
        return R.ok().data("list",list);
    }

    @ApiOperation("导出")
    @GetMapping( "/exportData")
    public void exportData(HttpServletResponse response){
        dictService.exportData(response);
    }

    @ApiOperation(value = "导入")
    @PostMapping("/importData")
    public R importData(MultipartFile file) {
        dictService.importDictData(file);
        return R.ok();
    }
}
