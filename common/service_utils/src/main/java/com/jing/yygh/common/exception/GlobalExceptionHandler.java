package com.jing.yygh.common.exception;

import com.jing.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//全局异常处理类
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e){
        e.printStackTrace();
        return R.error();
    }


    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R error(ArithmeticException e){
        e.printStackTrace();
        return R.error().message("出现了ArithmeticException异常");
    }


    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public R error(YyghException e){
        e.printStackTrace();
        log.error(e.getMessage());
        return R.error().message(e.getMessage()).code(e.getCode());
    }


}
