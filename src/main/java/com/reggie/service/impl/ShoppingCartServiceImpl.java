package com.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.ShoppingCart;
import com.reggie.mapper.ShoppingCartMapper;
import com.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @create: 2022/11/22 10:58
 */

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
