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
//@CrossOrigin//跨域
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

    @ApiOperation("查询医院等级名称")
    @GetMapping("/getName/{parentDictCode}/{value}")
    public String getName(@PathVariable("parentDictCode") String parentDictCode,@PathVariable("value") String value){
        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }

    @ApiOperation("查询省市区名称")
    @GetMapping("/getName/{value}")
    public String getName(@PathVariable("value") String value){
        return dictService.getNameByParentDictCodeAndValue("", value);
    }

    @ApiOperation("查找所有的省")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable("dictCode") String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }

}
