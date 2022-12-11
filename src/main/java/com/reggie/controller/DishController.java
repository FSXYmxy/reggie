package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @create: 2022/11/16 14:41
 */

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    DishFlavorService dishFlavorService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查找菜品分页
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //创建分页
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //查询框是输入菜名，所以我自己选择按菜名查找
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //如果传了名字的话
        if (name != null){
            dishLambdaQueryWrapper.like(Dish::getName, name);
        }
        dishLambdaQueryWrapper.orderByAsc(Dish::getName);

        //把查询结果装到页面上
        dishService.page(dishPage, dishLambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        List<Dish> records = dishPage.getRecords();

        List<DishDto> list = records.stream().map((item) ->{

            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id

            //根据id查询分类
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            return dishDto;

        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }


//    /**
//     * 按id查找菜品
//     * @param id
//     * @return
//     */
//    @GetMapping("/{id}")
//    public R<Dish> getById(@PathVariable Long id){
//        log.info("查找菜品{}", id);
//
//        Dish dish = dishService.getById(id);
//        if (dish != null) {
//            return R.success(dish);
//        }
//
//        return R.error("查找的菜品并不存在");
//
//    }

    /**
     * 停售或启售菜品
     * @param code
     * @param ids
     * @return
     */
    @PostMapping("/status/{code}")
    public R<Dish> forbid(@PathVariable Integer code, Long ids){
        Dish dish = dishService.getById(ids);
        Integer status = dish.getStatus();

        switch (code){
            case 0:
                if (status == 1){
                    dish.setStatus(0);
                    dishService.updateById(dish);
                    return R.success(dish);
                }else if (status == 0){
                    return R.error("菜品已经停售");
                }
                break;
            case 1:
                if (status == 0){
                    dish.setStatus(1);
                    dishService.updateById(dish);
                    return R.success(dish);
                }else if (status == 1){
                    return R.error("菜品已经启售");
                }
                break;
        }

        return R.error("修改失败");
    }

    /**
     * 按id删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R<String > delete(@PathVariable Long ids){
        dishService.removeById(ids);

        return R.success("删除成功");
    }

    /**
     * 添加菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String > save(@RequestBody DishDto dishDto){

        //清理菜品缓存

        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return R.success("菜品添加成功");
    }

    /**
     * 获取菜品，带上口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String > update(@RequestBody DishDto dishDto){

//        //清理所有的菜品缓存
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //根据分类清理缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        return R.success("菜品修改成功");
    }

//    /**
//     * 查找套餐分类
//     * @param categoryId
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Category>> list(Long categoryId){
//        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Category::getId, categoryId);
//
//        List<Category> list = categoryService.list(queryWrapper);
//
//        return R.success(list);
//    }

//    /**
//     * 查找同一分类下的菜品列表
//     * @param categoryId
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Long categoryId){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//
//        //按照分类id查找,需要是在售的菜品
//        queryWrapper.eq(Dish::getCategoryId, categoryId).eq(Dish::getStatus, 1);
//
//        //得到菜品列表
//        List<Dish> dishList = dishService.list(queryWrapper);
//
//        return R.success(dishList);
//    }

    //添加缓存菜品功能
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        Long categoryId = dish.getCategoryId();
        List<DishDto> dishDtoList = null;

        //构造key
        String key = "dish_" + categoryId + "_" + dish.getStatus();

        //先从redis获取缓存，有则直接返回，没有则查询
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null){
            return R.success(dishDtoList);
        }


        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查找指定菜品分类中在售的菜品
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId).eq(Dish::getStatus, 1);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //得到菜品单，但此时缺少口味信息
        List<Dish> dishList = dishService.list(queryWrapper);

        //创建新容器，在保存菜品单的同时添加口味信息
        dishDtoList = dishList.stream().map(item ->{//item是dishList中每次遍历得到的对象
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //设置菜品分类名称（dish对象没有这个属性）
            dishDto.setCategoryName(categoryService.getById(categoryId).getName());

            //获取菜品的id，再根据id在口味表查找口味信息
            LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
            flavorQueryWrapper.eq(DishFlavor::getDishId, item.getId());

            //得到口味列表p
            List<DishFlavor> flavors = dishFlavorService.list(flavorQueryWrapper);
            //为容器添加口味列表
            dishDto.setFlavors(flavors);

            return dishDto;
        }).collect(Collectors.toList());

        //如果redis中不存在缓存，将查询到的数据缓存到redis中
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }



}
