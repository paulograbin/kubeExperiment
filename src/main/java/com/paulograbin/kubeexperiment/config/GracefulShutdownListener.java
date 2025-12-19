package com.paulograbin.kubeexperiment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class GracefulShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownListener.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("========== Graceful Shutdown Initiated ==========");
        log.info("Application is shutting down gracefully...");
        log.info("Waiting for in-flight requests to complete...");
        log.info("================================================");
    }
}
