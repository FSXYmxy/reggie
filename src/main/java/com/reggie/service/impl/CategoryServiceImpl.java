package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.CustomException;
import com.reggie.common.R;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.Setmeal;
import com.reggie.mapper.CategoryMapper;
import com.reggie.service.CategoryService;
import com.reggie.service.DishService;
import com.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @create: 2022/11/15 16:03
 */

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除菜品类型(category:类型)
     * @param id
     * @return
     */
    @Override
    public R<String > removeById(Long id){

        //查询当前分类是否关联了菜品，如果已经关联则抛出异常
        //添加查询条件，根据分类id查询
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dishQueryWrapper);
        if (count > 0) {
            throw new CustomException("已关联菜品，无法删除");
        }

        //查询当前分类是否关联了套餐，如果是则抛出异常
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count1 = setmealService.count(setmealQueryWrapper);
        if (count1 > 0){
            throw new CustomException("已关联套餐，无法删除");
        }

        //正常删除分类
        super.removeById(id);

        return R.success("删除成功");
    }

}
