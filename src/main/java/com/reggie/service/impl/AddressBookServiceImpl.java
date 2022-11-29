package com.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.AddressBook;
import com.reggie.mapper.AddressBookMapper;
import com.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * @create: 2022/11/21 10:08
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
