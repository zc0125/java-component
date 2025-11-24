package com.feng.module.task.model;

public class TaskResult<T> {
    /**
     * 执行结果
     */
    private final T result;
    /**
     * 异常
     */
    public final Exception exception;
    /**
     * 是否成功
     */
    public final Boolean success;

    private TaskResult(T result, Exception exception, Boolean success) {
        this.result = result;
        this.exception = exception;
        this.success = success;
    }

    public static <T> TaskResult<T> fail(T result, Exception exception) {
        return new TaskResult<>(result, exception, false);
    }

    public static <T> TaskResult<T> success(T result) {
        return new TaskResult<>(result, null, true);
    }

    public T getResult() {
        return result;
    }
}
