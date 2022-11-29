package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;


public interface DishService extends IService<Dish> {
    //新增菜品的同时插入数据，需要操作dish和dish_flavor表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品基本信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品，带上口味
    public void updateWithFlavor(DishDto dishDto);
}
