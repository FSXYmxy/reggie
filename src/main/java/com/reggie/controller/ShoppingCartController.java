package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.ShoppingCart;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @create: 2022/11/22 10:59
 */

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart > add(@RequestBody ShoppingCart shoppingCart){

        //为购物车绑定用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查询当前菜品或套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);

        //判断添加的是菜品还是套餐
        if (dishId != null){
            //如果是菜品
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //如果是套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart cartItem = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
        if (cartItem != null){
            //如果已存在，就在原有数量上加一
            cartItem.setNumber(cartItem.getNumber() + 1);
            //记得保存。。。
            shoppingCartService.updateById(cartItem);
        } else {
            //如果不存在，就直接添加，注意使用传进来的购物车
            shoppingCart.setNumber(1);
            //设置创建时间，方便排序
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartItem = shoppingCart;
        }

        return R.success(cartItem);
    }

    /**
     * 查看购物车列表
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        //查找当前用户id，并利用id查找购物车
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        //按添加时间排序
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String > clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("已清空购物车");

    }
}
