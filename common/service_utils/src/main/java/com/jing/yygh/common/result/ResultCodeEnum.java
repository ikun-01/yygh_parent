package com.jing.yygh.common.result;

//返回状态信息
public enum ResultCodeEnum {

    SUCCESS(20000,"操作成功!"),
    ERROR(20001,"操作失败!");

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code,String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
