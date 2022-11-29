package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;

/**
 * @create: 2022/11/15 17:49
 */
public interface SetmealService extends IService<Setmeal> {

    //根据id查找带有菜品列表的套餐
    public SetmealDto getByIdWithDish(Long id);

    //保存带有菜品列表的套餐
    public void updateWithDish(SetmealDto setmealDto);

    public void saveWithDish(SetmealDto setmealDto);
}
