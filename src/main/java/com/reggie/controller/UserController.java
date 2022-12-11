package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.R;
import com.reggie.entity.User;
import com.reggie.service.UserService;
import com.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @create: 2022/11/20 10:23
 */

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String > sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
            //生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            //发送验证码
            log.info("code={}", code);
//            SMSUtils.sendMessage(phone, "毛线衣", "您的登录验证码是${code}，请不要透露给别人", code);

            //将生成的验证码保存到Session(弃用)
//            session.setAttribute("phone", code);

            //将生成的验证码缓存到redis中并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("验证码发送成功");
        }

        return R.error("短信发送失败");
    }


    @PostMapping("/login")
    public R<String > login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取得到的电话与验证码
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");

        //获取session中的电话与验证码，并进行比对（弃用）
//        String codeInSession = (String) session.getAttribute("phone");

        //获取缓存中的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
//        System.out.println(codeInSession);

        if (code != null && code.equals(codeInSession)){
            //验证码正确
            //判断当前用户是否为新用户，如果是的话就自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);

            if (user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);

                userService.save(user);

                //将用户id存到session中，用于跳转首页
                session.setAttribute("user", user.getId());

                return R.success("注册成功");
            }

            //将用户id存到session中，用于跳转首页
            session.setAttribute("user", user.getId());

            //登陆成功，删除缓存中的验证码
            redisTemplate.delete(phone);

            return R.success("登陆成功");

        } else {
            return R.error("验证码错误");
        }

    }

}
