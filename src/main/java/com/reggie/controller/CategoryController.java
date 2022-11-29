package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Category;
import com.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @create: 2022/11/15 16:21
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @PostMapping
    public R<String > save(@RequestBody Category category){
        log.info("新增菜品{}", category);
        categoryService.save(category);

        return R.success("保存成功");
    }


    /**
     * 查询菜品分页
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize){
        //分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件排序，按照sort排序
        queryWrapper.orderByAsc(Category::getSort );

        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }


    /**
     * 根据id删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String > deleteById(Long ids){
        log.info("删除分类{}", ids);
        categoryService.removeById(ids);

        return R.success("删除成功");
    }


    /**
     * 更新分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String > update(@RequestBody Category category){
        log.info("更新分类{}", category);
        categoryService.updateById(category);

        return R.success("修改分类成功");
    }

    /**
     * 按照分类查询
     * @param type
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Integer type){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件
        queryWrapper.eq(type != null, Category::getType, type);

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);


        return R.success(list);
    }


}
