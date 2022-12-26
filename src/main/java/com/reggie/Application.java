package com.reggie;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @create: 2022/11/13 9:53
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan
@Transactional
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
