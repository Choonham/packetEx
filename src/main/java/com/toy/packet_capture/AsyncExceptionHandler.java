package com.toy.packet_capture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        logger.error("Thread Error Exception");
        logger.error("exception Message :: " + throwable.getMessage());
        logger.error("method name :: " + method.getName());

        for(Object param : obj) {
            logger.error("param Val ::: " + param);
        }
    }
}
