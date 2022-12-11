package com.reggie.config;

import com.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @create: 2022/11/13 10:06
 */

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射，因为默认访问路径是static
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        log.info("静态资源映射开始，这句话在/src/main/java/com/reggie/config/WebMvcConfig.java");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");

    }

    //拓展消息转换器
    //这里的意思是，将web请求的序列化和反序列化工作交由我们之前创建的自定义序列化器执行，
    //为了实现这一目的，我们将消息转化器的序列化器指定为自定义序列化器，并设为最高优先级
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //创建对象转换器，底层使用Jackson将java对象转成json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器
        converters.add(0,messageConverter);//放在最前面，优先使用

    }
}
