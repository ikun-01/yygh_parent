package com.jing.yygh.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

//统一返回结果
@Data
public class R {
    //状态信息码
    private Integer code;
    //操作信息
    private String message;
    //是否成功
    private boolean success;
    //返回数据
    private Map<String,Object> data = new HashMap<>();



    public static R ok(){
        R r = new R();
        r.setCode(ResultCode.SUCCESS);
        r.setSuccess(true);
        r.setMessage("操作成功");
        return r;
    }

    public static R error(){
        R r = new R();
        r.setCode(ResultCode.ERROR);
        r.setSuccess(false);
        r.setMessage("操作失败");
        return r;
    }

    public R data(Map<String,Object> data){
        this.setData(data);
        return this;
    }

    public R data(String key,Object value){
        this.getData().put(key,value);
        return this;
    }

    public R message(String message){
        this.setMessage(message);
        return this;
    }

    public R success(boolean success){
        this.setSuccess(success);
        return this;
    }

    public R code(Integer code){
        this.setCode(code);
        return this;
    }


}
