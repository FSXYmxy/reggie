package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.mapper.DishMapper;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @create: 2022/11/15 17:48
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    DishFlavorService dishFlavorService;

    /**
     * 新增菜品同时保存口味
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存基本信息
        this.save(dishDto);

        //保存口味信息，关联菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品基本信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息并保存到DishDto中
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询口味信息(已经创建了dishDto，查询口味后再添加上)
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * 更新菜品，带上口味
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新基本信息
        this.updateById(dishDto);

        //删除当前口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前添加的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        //给口味链接到菜品id
        flavors = flavors.stream().map(item ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
