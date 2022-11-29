package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.dto.DishDto;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;
import com.reggie.entity.SetmealDish;
import com.reggie.mapper.SetmealMapper;
import com.reggie.service.DishService;
import com.reggie.service.SetmealDishService;
import com.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @create: 2022/11/15 17:50
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    SetmealDishService setmealDishService;

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //获取基本信息
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        //对象拷贝
        BeanUtils.copyProperties(setmeal, setmealDto);
        //查询关联表setmeal_dish的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);

        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

        //设置套餐菜品属性
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }


//        // 根据id查询setmeal表中的基本信息
//        Setmeal setmeal = this.getById(id);
//        SetmealDto setmealDto = new SetmealDto();
//        // 对象拷贝。
//        BeanUtils.copyProperties(setmeal, setmealDto);
//        // 查询关联表setmeal_dish的菜品信息
//        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(SetmealDish::getSetmealId, id);
//        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
//        //设置套餐菜品属性
//        setmealDto.setSetmealDishes(setmealDishList);
//        return setmealDto;


    @Override
    public void updateWithDish(SetmealDto setmealDto) {

        //保存基本信息
        this.updateById(setmealDto);//this是SetmealService，套餐信息，不包含菜品列表

        //为了删除原来的菜品列表，先查找原来的setmealDto下包含的菜品列表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();//SetmealDish
        queryWrapper.eq(setmealDto != null, SetmealDish::getSetmealId, setmealDto.getId());
        //删除！
        setmealDishService.remove(queryWrapper);

        //添加新的菜品列表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();//这里获得的菜品列表并没有设置套餐id？
        //为菜品列表指定套餐id
        setmealDishes = setmealDishes.stream().map(item ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

//    // 保存setmeal表中的基本数据。
//    this.updateById(setmealDto);
//    // 先删除原来的套餐所对应的菜品数据。
//    LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
//    queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
//    setmealDishService.remove(queryWrapper);
//    // 更新套餐关联菜品信息。setmeal_dish表。
//    // Field 'setmeal_id' doesn't have a default value] with root cause
//    // 所以需要处理setmeal_id字段。
//    // 先获得套餐所对应的菜品集合。
//    List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
//    //每一个item为SetmealDish对象。
//    setmealDishes = setmealDishes.stream().map((item) -> {
//        //设置setmeal_id字段。
//        item.setSetmealId(setmealDto.getId());
//        return item;
//    }).collect(Collectors.toList());
//
//    // 重新保存套餐对应菜品数据
//    setmealDishService.saveBatch(setmealDishes);


    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存基本信息到setmeal表
        this.save(setmealDto);

        //保存菜品信息到setmeal_dish表
        Long setmealDtoId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        //为每样菜品设置套餐id
        setmealDishes.stream().map(item ->{
            item.setSetmealId(setmealDtoId);
            return item;
        }).collect(Collectors.toList());

        //保存带有套餐id的菜品
        setmealDishService.saveBatch(setmealDishes);
    }
}
