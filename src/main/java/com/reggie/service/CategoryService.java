package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.common.R;
import com.reggie.entity.Category;

/**
 * @create: 2022/11/15 16:03
 */
public interface CategoryService extends IService<Category> {

    R<String > removeById(Long id);
}
