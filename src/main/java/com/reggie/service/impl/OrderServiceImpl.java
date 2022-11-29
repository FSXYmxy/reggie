package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.BaseContext;
import com.reggie.entity.*;
import com.reggie.mapper.OrderMapper;
import com.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @create: 2022/11/22 12:29
 */

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Transactional
    @Override
    public void submit(Orders orders) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询用户id对应的购物车
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartLambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list();

        //查找用户信息和地址信息(因为前端并没有发送过来)
        User user = userService.getById(userId);

        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        //向订单表插入数据，一条数据
        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        //获取总金额
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(item->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());



        //向明细表插入数据，多条数据
        orders.setNumber(String.valueOf(orderId));
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(user.getPhone());
        orders.setAddress(
                  (addressBook.getProvinceName() != null? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() != null? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() != null? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() != null? "" : addressBook.getDetail())
                );
        this.save(orders);

        //保存订单明细
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车数据
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }
}
