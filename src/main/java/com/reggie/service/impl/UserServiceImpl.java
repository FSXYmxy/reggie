package com.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.User;
import com.reggie.mapper.UserMapper;
import com.reggie.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @create: 2022/11/20 10:22
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
