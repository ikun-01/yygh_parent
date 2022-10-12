package com.jing.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.common.result.R;
import com.jing.yygh.hosp.service.HospitalSetService;
import com.jing.yygh.model.hosp.HospitalSet;
import com.jing.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
//@CrossOrigin//解决跨域请求
@RequestMapping("/admin/hosp/hospitalSet")
@Api(description = "医院设置接口")
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    /**
     * 查找所有医院设置
     * @return
     */
    @ApiOperation(value = "医院设置列表")
    @GetMapping("/findAll")
    public R findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list",list);
    }

    /**
     * 根据id逻辑删除
     * @param id id
     * @return
     */
    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("/{id}")
    public R removeById(@ApiParam(name="id",value="医院设置编号") @PathVariable("id") Long id){
        boolean flag = hospitalSetService.removeById(id);
        return flag ? R.ok() : R.error().message("信息不存在");
    }

    /**
     * 分页查询所有
     * @param page 查询第几页
     * @param limit 每页几条数据
     * @return 所有数据信息和记录数
     */
    @ApiOperation("分页查询所有")
    @GetMapping("/{page}/{limit}")
    public R pageList(@ApiParam(name = "page",value = "当前第几页") @PathVariable(value = "page",required = false) Long page,
                      @ApiParam(name = "limit",value = "每页的记录数") @PathVariable(value = "limit",required = false) Long limit){
        if (page == null) page = 1L;
        if (limit == null) limit = 10L;
        //设置分页条件
        Page<HospitalSet> pageParam = new Page<>(page,limit);
        hospitalSetService.page(pageParam);
        //查询所有数据信息和总记录数
        List<HospitalSet> rows = pageParam.getRecords();
        long total = pageParam.getTotal();
        return R.ok().data("rows",rows).data("total",total);
    }


    /** 根据条件分页查询
     * @param page 查询第几页
     * @param limit 每页的记录数
     * @param hospitalSetQueryVo 查询条件
     * @return
     */
    @ApiOperation("根据条件分页查询")
    @PostMapping("/{page}/{limit}")
    public R pageQuery(@ApiParam(name = "page",value = "当前第几页") @PathVariable(value = "page",required = false) Long page,
                       @ApiParam(name = "limit",value = "每页的记录数") @PathVariable(value = "limit",required = false) Long limit,
                       @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){
        if (page == null) page = 1L;
        if (limit == null) limit = 10L;
        //设置分页条件
        Page<HospitalSet> pageParam = new Page<>(page,limit);
        //构建条件查询
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if (hospitalSetQueryVo != null) {
            String hoscode = hospitalSetQueryVo.getHoscode();
            String hosname = hospitalSetQueryVo.getHosname();
            if (!StringUtils.isEmpty(hoscode)) {
                queryWrapper.eq("hoscode",hoscode);
            }
            if (!StringUtils.isEmpty(hosname)) {
                queryWrapper.like("hosname",hosname);
            }
        }
        hospitalSetService.page(pageParam,queryWrapper);
        //查询所有数据信息和总记录数
        List<HospitalSet> rows = pageParam.getRecords();
        long total = pageParam.getTotal();
        return R.ok().data("rows",rows).data("total",total);
    }

    /**
     * 开通医院设置
     * @param hospitalSet
     * @return
     */
    @ApiOperation("设置医院信息")
    @PostMapping("/saveHospSet")
    public R save(@RequestBody HospitalSet hospitalSet){
        hospitalSet.setStatus(1);
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }

    /**
     * 根据id查找
     * @param id
     * @return
     */
    @ApiOperation("根据id查找医院信息")
    @GetMapping("/getHospSet/{id}")
    public R getById(@PathVariable("id") Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return hospitalSet == null ? R.error().message("查找信息不存在") : R.ok().data("item",hospitalSet);
    }

    /**
     * 根据id修改
     * @param hospitalSet
     * @return
     */
    @PutMapping("/updateHospSet")
    @ApiOperation("根据id修改")
    public R update(@RequestBody HospitalSet hospitalSet){
        if (hospitalSet == null || hospitalSet.getId() == null){
            throw new YyghException(20001,"参数不能为空");
        }
        boolean flag = hospitalSetService.updateById(hospitalSet);
        return flag ? R.ok() : R.error();
    }

    /**
     * 根据id批量删除
     * @param idList
     * @return
     */
    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public R batchRemove(@RequestBody List<Long> idList){
        boolean b = hospitalSetService.removeByIds(idList);
        return b ? R.ok() : R.error();
    }

    /**
     * 锁定医院和解锁
     * @param id
     * @param status 0 锁定 1 解锁
     * @return
     */
    @ApiOperation("锁定医院和解锁")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable("id") Long id, @PathVariable("status") Integer status){
        if (status != 0 && status != 1 ) {
            throw new YyghException(20001,"status只能为0或1");
        }
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        if (hospitalSet == null){
            throw new YyghException(20001,"医院设置不存在");
        }
        hospitalSet.setStatus(status);
        boolean b = hospitalSetService.updateById(hospitalSet);
        return b ? R.ok() : R.error();
    }

}
