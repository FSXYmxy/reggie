package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @create: 2022/11/13 11:57
 */
@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1.对密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3.如果没查到就返回失败结果
        if (emp == null){
            return R.error("登陆失败");
        }

        //4.密码比对，不一致则返回失败
        if (! emp.getPassword().equals(password)){
            return R.error("密码不正确");
        }

        //5.查看员工状况，如果被禁用就返回被禁用结果，0禁用1正常
        if (emp.getStatus() == 0){
            return R.error("用户已被禁用");
        }

        //6.登陆成功，将员工id存入session并返回登陆成功结果
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清除当前Session中保存的用户id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工：" + employee);

        //设置初始密码，需要进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登陆用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        //保存用户
        employeeService.save(employee);

        return R.success("新增用户成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}", page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件查询器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name );
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String > update(HttpServletRequest request, @RequestBody Employee employee){
        log.info(employee.toString());

//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((Long)request.getSession().getAttribute("employee"));

        //员工状态已经发送过来，不要再改
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);

        if (employee != null) {
            return R.success(employee);
        }

        return R.error("没有查询到该员工");
    }


}
