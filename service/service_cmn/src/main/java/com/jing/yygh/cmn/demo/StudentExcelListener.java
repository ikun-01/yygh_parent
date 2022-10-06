package com.jing.yygh.cmn.demo;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentExcelListener extends AnalysisEventListener<Student> {

    List<Student> list = new ArrayList<>();

    // 一次读取一行
    @Override
    public void invoke(Student data, AnalysisContext context) {
        list.add(data);
        System.out.println("读取到数据");
    }


    //数据读取完成之后的操作
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println(list);
    }
}
