package com.toy.packet_capture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //기본 Thread 수
    private static int TASK_CORE_POOL_SIZE = 3;

    //최대 Thread 수
    private static int TASK_MAX_POOL_SIZE = 3;

    //QUEUE 수
    private static int TASK_QUEUE_CAPACITY = 0;

    //Thread Bean Name
    private final String EXECUTOR_BEAN_NAME = "executor1";

    private static boolean STOPFLAG = false;

    public static boolean isSTOPFLAG() {
        return STOPFLAG;
    }

    public static void setSTOPFLAG(boolean STOPFLAG) {
        AsyncConfig.STOPFLAG = STOPFLAG;
    }

    @Resource
    private ThreadPoolTaskExecutor executor1;

    @Bean(name="executor1")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TASK_CORE_POOL_SIZE);
        executor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
        executor.setQueueCapacity(TASK_QUEUE_CAPACITY);
        executor.setBeanName(EXECUTOR_BEAN_NAME);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        System.out.println("12341234");
        return executor;
    }

    /*
     * Thread Process도중 에러 발생시
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new com.toy.packet_capture.AsyncExceptionHandler();
    }

    /*
     * task 생성전에 pool이 찼는지를 체크
     */
    public boolean checkSampleTaskExecute() {
        boolean result = true;

        logger.info("활성 Task 수 :::: " + executor1.getActiveCount());

        if(executor1.getActiveCount() >= (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            result = false;
        }
        return result;
    }
}
