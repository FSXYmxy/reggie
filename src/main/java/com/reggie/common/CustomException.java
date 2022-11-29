package com.reggie.common;

/**
 * 自定义业务异常类
 * @create: 2022/11/15 18:36
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
