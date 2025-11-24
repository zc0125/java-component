package com.feng.module.binding.model;

import com.feng.module.task.model.TaskResult;

import java.lang.reflect.Method;

/**
 * 任务绑定的执行结果封装
 * @author zc
 * Date 2025/03/15 23:50
 * Version 1.0
 */
public class TaskBindingResult<R> {
    private final String service;
    private final String module;
    private final String function;
    private final Object bean;
    private final Method method;
    private final TaskResult<R> taskResult;

    TaskBindingResult(String service, String module, String function, Object bean, Method method, TaskResult<R> taskResult) {
        this.taskResult = taskResult;
        this.service = service;
        this.module = module;
        this.function = function;
        this.bean = bean;
        this.method = method;
    }

    public static <R> TaskBindingResult<R> init(String service, String module, String function, Object bean, Method method, TaskResult<R> taskResult) {
        return new TaskBindingResult<R>(service, module, function, bean, method, taskResult);
    }


    public String getService() {
        return service;
    }

    public String getModule() {
        return module;
    }

    public String getFunction() {
        return function;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public R getResult() {
        return taskResult.getResult();
    }

    public Boolean getSuccess() {
        return taskResult.success;
    }

    public Exception getException() {
        return taskResult.exception;
    }
}
