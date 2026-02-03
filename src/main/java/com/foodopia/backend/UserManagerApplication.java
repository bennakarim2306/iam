package com.foodopia.backend;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class UserManagerApplication {

    @Value("${application.version}")
    private String version;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${logging.include-stacktrace:false}")
    private boolean includeStackTrace;

    public static void main(String[] args) {
        SpringApplication.run(UserManagerApplication.class, args);
    }

    @PostConstruct
    public void logStartup() {
        log.info("Application {} version {} started successfully", appName, version);
        setupGlobalExceptionHandler();
    }

    private void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Uncaught exception in thread={} message={}",
                    thread.getName(),
                    throwable.getMessage());

            if (includeStackTrace) {
                log.error("stacktrace", throwable);
            }
        });
    }
}
