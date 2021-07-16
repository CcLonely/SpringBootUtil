package com.example.bootutil.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //注意： 拦截器需按顺序进行配置
        registry.addInterceptor(authenticationInterceptor())
                .addPathPatterns("/**");


    }

    @Bean
    public PreventionRepeatInterceptor authenticationInterceptor() {
        return new PreventionRepeatInterceptor();
    }


}