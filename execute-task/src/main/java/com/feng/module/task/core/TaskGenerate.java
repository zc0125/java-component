package com.feng.module.task.core;

import com.feng.module.task.model.TaskModel;

import java.lang.reflect.Method;

/**
 * 方法封装器
 * 将方法逻辑封装成Task
 * 在需要执行时动态传入参数,形成TaskExecutor,即可调用执行
 *
 * @param <R>
 */
public class TaskGenerate<R> {
    /**
     * 任务模型
     */
    private final TaskModel<R> taskModel;

    /**
     * 构造方法
     *
     * @param taskModel 任务模型
     */
    public TaskGenerate(TaskModel<R> taskModel) {
        this.taskModel = taskModel;
    }

    /**
     * 设置参数转成TaskExecutor
     *
     * @param args 任意参数
     * @return TaskExecutor任务对象
     */
    public TaskExecutor<R> withArgs(Object... args) {
        return TaskExecutor.init(taskModel.withArgs(args));
    }

    /**
     * 任务封装初始化:通过传递任务模型
     *
     * @param taskModel 任务模型
     * @param <R>       任务返回类型
     * @return 任务对象
     */
    public static <R> TaskGenerate<R> init(TaskModel<R> taskModel) {
        return new TaskGenerate<>(taskModel);
    }

    /**
     * 任务封装初始化: 通过设置执行对象,执行方法
     *
     * @param bean   执行对象
     * @param method 执行方法
     * @param <R>    任务返回类型
     * @return 任务对象
     */
    public static <R> TaskGenerate<R> init(Object bean, Method method) {
        return TaskGenerate.init(TaskModel.init(bean, method));
    }

    /**
     * 任务封装初始化: 无参方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R> TaskGenerate<R> init(TaskPackWithNoArgs<R> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }

    /**
     * 任务封装初始化: 任意参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R> TaskGenerate<R> init(TaskPackWithAnyArgs<R> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }

    /**
     * 任务封装初始化: 1参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R, T> TaskGenerate<R> init(TaskPackWith1Args<R, T> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }

    /**
     * 任务封装初始化: 2参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2> TaskGenerate<R> init(TaskPackWith2Args<R, T1, T2> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }

    /**
     * 任务封装初始化: 3参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2, T3> TaskGenerate<R> init(TaskPackWith3Args<R, T1, T2, T3> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }

    /**
     * 任务封装初始化: 4参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2, T3, T4> TaskGenerate<R> init(TaskPackWith4Args<R, T1, T2, T3, T4> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }

    /**
     * 任务封装初始化: 5参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2, T3, T4, T5> TaskGenerate<R> init(TaskPackWith5Args<R, T1, T2, T3, T4, T5> taskPack) {
        return TaskGenerate.init(taskPack, TaskExecutor.getTaskMethod(taskPack));
    }
}
