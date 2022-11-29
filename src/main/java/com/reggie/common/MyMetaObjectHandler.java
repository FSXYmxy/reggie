package com.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @create: 2022/11/15 12:57
 */

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作自动填充
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充insert");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        //当前用户不能直接获得
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新操作自动填充
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充update");

        metaObject.setValue("updateTime", LocalDateTime.now());
        //当前用户不能直接获得
        metaObject.setValue("updateUser", BaseContext.getCurrentId());


    }
}
