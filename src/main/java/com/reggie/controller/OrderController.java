package com.reggie.controller;

import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.dto.OrdersDto;
import com.reggie.entity.Orders;
import com.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @create: 2022/11/22 12:31
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public R<String > submit(@RequestBody Orders orders){
        orderService.submit(orders);


        return R.success("支付成功");
    }
}
