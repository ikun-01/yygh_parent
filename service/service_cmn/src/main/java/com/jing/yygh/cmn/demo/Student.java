package com.jing.yygh.cmn.demo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student implements Serializable {
    // value 代表的是 表格标题的名称  index 代表的是第几列从 0 开始
    @ExcelProperty(value = "学生姓名",index = 0)
    private String name;
    @ExcelProperty(value = "学生年龄",index = 1)
    private Integer age;
    @ExcelProperty(value = "家庭住址",index = 2)
    private String address;
}
