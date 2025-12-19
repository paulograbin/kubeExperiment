package com.paulograbin.kubeexperiment.config;

import com.paulograbin.kubeexperiment.interceptor.HttpRequestLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final HttpRequestLoggingInterceptor httpRequestLoggingInterceptor;

    @Autowired
    public WebConfig(HttpRequestLoggingInterceptor httpRequestLoggingInterceptor) {
        this.httpRequestLoggingInterceptor = httpRequestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpRequestLoggingInterceptor)
                .addPathPatterns("/**"); // Intercept all paths
    }
}
