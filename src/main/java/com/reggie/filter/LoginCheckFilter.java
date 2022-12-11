package com.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经登录
 * @create: 2022/11/13 15:03
 */

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的uri
        String requestURI = request.getRequestURI();

        //定义不需要拦截的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2.如果不需要处理则直接放行
        boolean check = check(urls, requestURI);
        if (check){
            filterChain.doFilter(request, response);
            return;
        }

        //3.如果管理员已登录也放行
        if (request.getSession().getAttribute("employee") != null){

            //保存Id
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request, response);
            return;
        }

        //4.如果用户已登录也放行
        if (request.getSession().getAttribute("user") != null){

            //保存Id
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));
            filterChain.doFilter(request, response);
            return;
        }

        //4.都不满足，跳转到登录页
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)){
                return true;
            }
        }
        return false;
    }
}
