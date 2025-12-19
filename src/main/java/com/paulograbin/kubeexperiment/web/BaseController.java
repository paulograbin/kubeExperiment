package com.paulograbin.kubeexperiment.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.text.SimpleDateFormat;
import java.util.*;


@RestController("/")
public class BaseController {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @GetMapping
    public Map<String, Object> home(HttpServletRequest request) {
        log.info("Received request for home endpoint at {}", sdf.format(Calendar.getInstance().getTime()));

        Map<String, Object> requestDetails = new LinkedHashMap<>();

        // Basic request information
        requestDetails.put("method", request.getMethod());
        requestDetails.put("requestURI", request.getRequestURI());
        requestDetails.put("requestURL", request.getRequestURL().toString());
        requestDetails.put("contextPath", request.getContextPath());
        requestDetails.put("servletPath", request.getServletPath());
        requestDetails.put("pathInfo", request.getPathInfo());
        requestDetails.put("queryString", request.getQueryString());

        // Protocol information
        requestDetails.put("protocol", request.getProtocol());
        requestDetails.put("scheme", request.getScheme());
        requestDetails.put("serverName", request.getServerName());
        requestDetails.put("serverPort", request.getServerPort());

        // Client information
        requestDetails.put("remoteAddr", request.getRemoteAddr());
        requestDetails.put("remoteHost", request.getRemoteHost());
        requestDetails.put("remotePort", request.getRemotePort());
        requestDetails.put("remoteUser", request.getRemoteUser());

        // Local information
        requestDetails.put("localAddr", request.getLocalAddr());
        requestDetails.put("localName", request.getLocalName());
        requestDetails.put("localPort", request.getLocalPort());

        // Headers
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> headerValues = new ArrayList<>();
            Enumeration<String> values = request.getHeaders(headerName);
            while (values.hasMoreElements()) {
                headerValues.add(values.nextElement());
            }
            headers.put(headerName, headerValues);
        }
        requestDetails.put("headers", headers);

        // Parameters
        Map<String, String[]> parameters = new LinkedHashMap<>(request.getParameterMap());
        requestDetails.put("parameters", parameters);

        // Additional attributes
        requestDetails.put("characterEncoding", request.getCharacterEncoding());
        requestDetails.put("contentType", request.getContentType());
        requestDetails.put("contentLength", request.getContentLength());
        requestDetails.put("locale", request.getLocale().toString());
        requestDetails.put("secure", request.isSecure());
        requestDetails.put("authType", request.getAuthType());

        // Session information (if exists)
        if (request.getSession(false) != null) {
            Map<String, Object> sessionInfo = new LinkedHashMap<>();
            sessionInfo.put("id", request.getSession().getId());
            sessionInfo.put("creationTime", new Date(request.getSession().getCreationTime()));
            sessionInfo.put("lastAccessedTime", new Date(request.getSession().getLastAccessedTime()));
            sessionInfo.put("maxInactiveInterval", request.getSession().getMaxInactiveInterval());
            sessionInfo.put("isNew", request.getSession().isNew());
            requestDetails.put("session", sessionInfo);
        } else {
            requestDetails.put("session", null);
        }

        return requestDetails;
    }
}

