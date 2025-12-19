package com.paulograbin.kubeexperiment.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        log.info("========== Incoming HTTP Request ==========");
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getRequestURI());
        log.info("Query String: {}", request.getQueryString());
        log.info("Remote Address: {}", request.getRemoteAddr());
        log.info("Handler: {}", handler);

        log.info("==========================================");

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("Request completed - Status: {}", response.getStatus());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        log.info("========== HTTP Request Completed ==========");
        log.info("URI: {}", request.getRequestURI());
        log.info("Status Code: {}", response.getStatus());
        log.info("Execution Time: {} ms", executionTime);

        if (ex != null) {
            log.error("Request completed with exception: ", ex);
        }

        log.info("==========================================");
    }
}
