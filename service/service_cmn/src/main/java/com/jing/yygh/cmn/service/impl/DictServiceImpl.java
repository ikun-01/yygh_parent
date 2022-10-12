package com.jing.yygh.cmn.service.impl;


import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jing.yygh.cmn.listener.DictListener;
import com.jing.yygh.cmn.mapper.DictMapper;
import com.jing.yygh.cmn.service.DictService;
import com.jing.yygh.common.exception.YyghException;
import com.jing.yygh.model.cmn.Dict;
import com.jing.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Autowired
    private  DictMapper dictMapper;

    @Autowired
    private DictListener dictListener;

    /**
     * 根据数据id 查找子数据列表
     * @param id
     * @return
     */
    @Cacheable(value = "dict",key = "'cache_'+#id")
    @Override
    public List<Dict> findChildData(Long id) {
        //设置查询条件,所有父id是当前id的就是子数据
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",id);

        List<Dict> dictList = dictMapper.selectList(queryWrapper);
        //遍历所有数据
        dictList.forEach(dict -> {
            //当前数据是否还有子数据
            Long dictId = dict.getId();
            boolean hasChildren = this.hasChildren(dictId);
            dict.setHasChildren(hasChildren);
        });

        return dictList;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel"); //指示响应内容的格式
            response.setCharacterEncoding("utf-8");

            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            // 指示响应内容以附件形式下载
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            List<Dict> dictList = dictMapper.selectList(null);

            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());

            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictVo);
                dictVoList.add(dictVo);
            }

            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //清除所有dict缓存
    @CacheEvict(value = "dict",allEntries = true)
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, String value) {
        // 查询条件
        QueryWrapper<Dict> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(dictCode)){
            //查询省市时 不需要字典编码
            query.eq("value",value);
            Dict dict = dictMapper.selectOne(query);
            return dict.getName();
        }
        query.eq("parent_id",this.getDict(dictCode).getId()).eq("value",value);
        Dict dict = dictMapper.selectOne(query);
        return dict.getName();

    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        if (StringUtils.isEmpty(dictCode)) {
            throw new YyghException(20001,"字典编码不能为空");
        }
        //获取Province的id
        Long id = this.getDict(dictCode).getId();
        //查找所有的省
        return this.findChildData(id);
    }

    /**
     * 根据 dictCode查询当前字典数据信息
     * @param dictCode
     * @return
     */
    private Dict getDict(String dictCode){
        QueryWrapper<Dict> query = new QueryWrapper<>();
        query.eq("dict_code",dictCode);
        return dictMapper.selectOne(query);
    }


    //判断当前数据是否还有子数据   true 还有 false 没有
    private boolean hasChildren(Long id){
        //设置查询条件,所有父id是当前id的就是子数据
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",id);
        Integer count = dictMapper.selectCount(queryWrapper);
        return count > 0;
    }
}
