package com.jing.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jing.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    //根据数据id查找子数据列表
    List<Dict> findChildData(Long id);

    /**
     * 导出
     * @param response
     */
    void exportData(HttpServletResponse response);
    /**
     * 导入数据字典
     * @param file
     */
    void importDictData(MultipartFile file) ;

    /**
     * 通过dictCode和value查询对应数据的name值
     * @param dictCode
     * @param value
     * @return
     */
    String getNameByParentDictCodeAndValue(String dictCode,String value);

}
