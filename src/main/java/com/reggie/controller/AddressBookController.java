package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.AddressBook;
import com.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



import java.util.List;

/**
 * @create: 2022/11/21 10:12
 */

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {


    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String > save(@RequestBody AddressBook addressBook){
        //设置创建地址的用户id
        addressBook.setUserId(BaseContext.getCurrentId());

        addressBookService.save(addressBook);
        return R.success("保存成功");
    }

    /**
     *设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook > setDefault(@RequestBody AddressBook addressBook){
        //先把传进来的所有同用户的地址设置为非默认，
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();//注意是updateWrapper

        updateWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault, 0);

        addressBookService.update(updateWrapper);

        //再将传进来的地址设置为默认
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }

    /**
     * 根据id查找地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);

        if (addressBook != null){
            return R.success(addressBook);
        }else {
            return R.error("没有找到该地址");
        }
    }


    /**
     * 查找默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //先获取当前登录用户的id
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook != null){
            return R.success(addressBook);
        } else {
            return R.error("没有找到默认地址");
        }

    }

    /**
     * 查找登录用户创建过的的地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseContext.getCurrentId() != null, AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(AddressBook::getUpdateTime);

        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);

        if(addressBooks != null){
            return R.success(addressBooks);
        } else {
            return R.error("该用户暂未设置地址");
        }
    }

    /**
     * 查找地址分页
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<AddressBook>> page(Integer page, Integer pageSize){
        //新建分页
        Page<AddressBook> addressBookPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(AddressBook::getCreateTime);

        //查找分页，后面只是排序条件
        addressBookService.page(addressBookPage, queryWrapper);

        return R.success(addressBookPage);
    }

}
