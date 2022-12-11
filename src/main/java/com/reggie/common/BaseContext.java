package com.reggie.common;

/**
 * 基于ThreadLocal工具类，保存和获取当前登录用户id
 * @create: 2022/11/15 15:11
 */

//现在没用了
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
