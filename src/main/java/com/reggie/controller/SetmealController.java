package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Category;
import com.reggie.entity.Setmeal;
import com.reggie.entity.SetmealDish;
import com.reggie.service.CategoryService;
import com.reggie.service.DishService;
import com.reggie.service.SetmealDishService;
import com.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @create: 2022/11/16 15:12
 */
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 查询套餐分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        setmealLambdaQueryWrapper.orderByAsc(Setmeal::getName);

        setmealService.page(setmealPage, setmealLambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(setmealPage, dtoPage, "records");
        //加入套餐分类信息
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> list =  records.stream().map(item ->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();

                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 停售或启售套餐
     * @param code
     * @param ids
     * @return
     */
    @PostMapping("/status/{code}")
    public R<String > forbid(@PathVariable Integer code, Long[] ids){
        for (Long id : ids){

            Setmeal setmeal = setmealService.getById(id);
            switch (code){
                case 0:
                    setmeal.setStatus(0);
                    setmealService.updateById(setmeal);
                    break;
                case 1:
                    setmeal.setStatus(1);
                    setmealService.updateById(setmeal);
                    break;
            }
        }

        return R.success("修改成功");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @Transactional
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)//删除该分类下的所有缓存
    public R<String > delete(Long[] ids){

        for (Long id : ids){
            Setmeal setmeal = setmealService.getById(id);
            if (setmeal.getStatus() == 1){
                return R.error("有套餐正在出售");
            }
            //删除套餐的基本信息
            setmealService.removeById(setmeal);
            //按套餐id删除菜品列表
            Long setmealId = setmeal.getId();
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish ::getSetmealId, setmealId);

            //删除菜品信息
            setmealDishService.remove(queryWrapper);
        }

        return R.success("删除成功");
    }


    /**
     * 根据id获取套餐
     * (回显)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        Long setmealDtoId = setmealDto.getId();

        return R.success(setmealDto);
    }


    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)//删除该分类下的所有缓存

    public R<String > update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);

        return R.success("修改成功");
    }

    /**
     * 保存套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CachePut(value = "setmealCache", key = "#setmealDto.id")
    public R<String > save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);

        return R.success("保存成功");
    }

    /**
     * 前台查找套餐表
     * @param categoryId
     * @param status
     * @return
     */
    @Cacheable(value = "setmealCache", key = "#categoryId + '_' + #status")
    @GetMapping("/list")
    public R<List<SetmealDto>> list(Long categoryId, Integer status){
        //先查找在售的id相同的套餐的基本信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId).eq(Setmeal::getStatus, status);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        //创建传输容器，复制套餐基本信息后，添加菜品表
        List<SetmealDto> setmealDtoList = setmealList.stream().map(item ->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            //为dto设置菜品表
            LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
            dishQueryWrapper.eq(SetmealDish::getSetmealId, item.getId());

            List<SetmealDish> setmealDishes = setmealDishService.list(dishQueryWrapper);
            setmealDto.setSetmealDishes(setmealDishes);

            //返回套餐
            return setmealDto;

        }).collect(Collectors.toList());

        return R.success(setmealDtoList);
    }


    /**
     * 获取带有菜品列表的套餐信息
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<SetmealDto> getDishById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }
}