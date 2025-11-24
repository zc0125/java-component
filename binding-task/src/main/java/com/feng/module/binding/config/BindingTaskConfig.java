package com.feng.module.binding.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置类
 * @author zc
 * Date 2025/03/16 10:40
 * Version 1.0
 */
public class BindingTaskConfig {

    private final ThreadPoolExecutor THREAD_POOL;

    // 私有化
    private BindingTaskConfig() {
        // TODO:改成配置
        this.THREAD_POOL = new ThreadPoolExecutor(
                5, 10, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(50),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 构建单例实例
     */
    private static class GenSingleton {
        //静态常量：类加载时初始化，仅一次
        private static final BindingTaskConfig INSTANCE = new BindingTaskConfig();
    }

    /**
     * 获取单例实例
     *
     * @return TaskBindingManage
     */
    private static BindingTaskConfig getInstance() {
        return BindingTaskConfig.GenSingleton.INSTANCE;
    }

    public static ThreadPoolExecutor getThreadPoll() {
        return getInstance().THREAD_POOL;
    }
}
