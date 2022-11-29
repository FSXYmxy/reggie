package com.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.DishFlavor;
import com.reggie.mapper.DishFlavorMapper;
import com.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * @create: 2022/11/17 11:26
 */

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
