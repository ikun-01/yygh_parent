package com.jing.yygh.cmn.demo;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyExcel api练习
 */
public class EasyExcelDemo {
    public static void main(String[] args) {

        List<Student> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Student("张" + i,i,"ShangHai"));
        }
        //将信息写出到文件中
        //sheet 对应excel中的sheet 第几个编号 和 名称  不写默认是0
        EasyExcel.write("C:\\Users\\Jing\\Desktop\\学生信息表.xlsx", Student.class).sheet("学生信息表").doWrite(list);
    }

    public static void main1(String[] args) {

        //sheet 对应的哪个sheet 中的数据 空参为第一个
        List<Student> list = new ArrayList<>();
        EasyExcel.read("C:\\Users\\Jing\\Desktop\\学生信息表.xlsx", Student.class, new StudentExcelListener()).sheet(0).doRead();
    }
}
