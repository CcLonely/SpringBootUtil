package com.example.bootutil.base;


import com.example.bootutil.exception.ResponseException;
import com.example.bootutil.vo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @Description: ExceptionController
 * @Date: 2019-12-13 11:43
 * @Author: JASON
 * @Version: V1.0
 * 异常响应
 */
@ControllerAdvice
public class ExceptionController {

    @ResponseBody
    @ExceptionHandler(ResponseException.class)
    public Result handleResponseException(Exception e) {
        return Result.err(e.getMessage());
    }
}
