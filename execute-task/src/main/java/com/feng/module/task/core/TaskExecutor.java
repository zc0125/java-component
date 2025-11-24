package com.feng.module.task.core;

import com.feng.module.task.model.TaskModel;
import com.feng.module.task.model.TaskResult;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 任务执行器
 * 负责任务的生成和执行
 *
 * @param <R> 任务返回类型
 */
public class TaskExecutor<R> {

    /**
     * 任务模型
     */
    private final TaskModel<R> taskModel;

    /**
     * 构造方法
     *
     * @param taskModel 任务模型
     */
    private TaskExecutor(TaskModel<R> taskModel) {
        this.taskModel = taskModel;
    }

    /**
     * 修改参数,生成新的任务
     *
     * @param args 需要变更的参数
     * @return 新任务对象
     */
    public TaskExecutor<R> withArgs(Object... args) {
        return TaskExecutor.init(taskModel.withArgs(args));
    }

    /**
     * 任务执行
     *
     * @return 任务执行结果
     */
    public R execute() {
        return taskModel.execute();
    }

    /**
     * 任务初始化:通过传递任务模型,生成新的任务
     *
     * @param taskModel 任务模型
     * @param <R>       任务返回类型
     * @return 任务对象
     */
    public static <R> TaskExecutor<R> init(TaskModel<R> taskModel) {
        return new TaskExecutor<>(taskModel);
    }

    /**
     * 任务初始化:通过设置执行对象,执行方法,执行参数生成新的任务
     *
     * @param bean   执行对象
     * @param method 执行方法
     * @param args   执行参数
     * @param <R>    任务返回类型
     * @return 任务对象
     */
    public static <R> TaskExecutor<R> init(Object bean, Method method, Object... args) {
        return TaskExecutor.init(TaskModel.init(bean, method, args));
    }

    /**
     * 任务初始化: 无参方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R> TaskExecutor<R> init(TaskPackWithNoArgs<R> taskPack) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack));
    }

    /**
     * 任务初始化: 任意参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param args     方法参数
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R> TaskExecutor<R> init(TaskPackWithAnyArgs<R> taskPack, Object... args) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack), new Object[]{args});
    }

    /**
     * 任务初始化: 1参数方法封装
     *
     * @param taskPack 任务lambda表达式形式 例如:this::a
     * @param value    方法参数
     * @param <R>      任务返回类型
     * @return 任务对象
     */
    public static <R, T> TaskExecutor<R> init(TaskPackWith1Args<R, T> taskPack, T value) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack), value);
    }

    /**
     * 任务初始化: 2参数方法封装
     *
     * @param taskPack      任务lambda表达式形式 例如:this::a
     * @param value1,value2 方法参数
     * @param <R>           任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2> TaskExecutor<R> init(TaskPackWith2Args<R, T1, T2> taskPack, T1 value1, T2 value2) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack), value1, value2);
    }

    /**
     * 任务初始化: 3参数方法封装
     *
     * @param taskPack             任务lambda表达式形式 例如:this::a
     * @param value1,value2,value3 方法参数
     * @param <R>                  任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2, T3> TaskExecutor<R> init(TaskPackWith3Args<R, T1, T2, T3> taskPack, T1 value1, T2 value2, T3 value3) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack), value1, value2, value3);
    }

    /**
     * 任务初始化: 4参数方法封装
     *
     * @param taskPack                    任务lambda表达式形式 例如:this::a
     * @param value1,value2,value3,value4 方法参数
     * @param <R>                         任务返回类型
     * @return 任务对象
     */
    public static <R, T1, T2, T3, T4> TaskExecutor<R> init(TaskPackWith4Args<R, T1, T2, T3, T4> taskPack, T1 value1, T2 value2, T3 value3, T4 value4) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack), value1, value2, value3, value4);
    }

    /**
     * 任务初始化: 5参数方法封装
     *
     * @param taskPack                           任务lambda表达式形式 例如:this::a
     * @param value1,value2,value3,value4,value5 方法参数
     * @param <R>                                任务返回类型
     * @return 任务对象
     */

    public static <R, T1, T2, T3, T4, T5> TaskExecutor<R> init(TaskPackWith5Args<R, T1, T2, T3, T4, T5> taskPack, T1 value1, T2 value2, T3 value3, T4 value4, T5 value5) {
        return TaskExecutor.init(taskPack, getTaskMethod(taskPack), value1, value2, value3, value4, value5);
    }

    /**
     * 获取Method,通过接口函数获取唯一method
     *
     * @param taskPack 任务接口函数对象
     * @return 唯一方法execute
     */
    protected static Method getTaskMethod(Object taskPack) {
        return taskPack.getClass().getDeclaredMethods()[0];
    }


    /**
     * 异步执行多任务并获取结果
     *
     * @param tasks      任务列表
     * @param threadPool 线程池
     * @param timeout    任务超时时间
     * @param <R>        任务执行返回类型
     * @return List<TaskResult<R>>
     */
    private static <R> List<TaskResult<R>> executeTasks(List<TaskExecutor<R>> tasks, ExecutorService threadPool, long timeout) {
        List<CompletableFuture<TaskResult<R>>> futureList = tasks.stream().map(
                task -> threadPool == null ? CompletableFuture.supplyAsync(
                        () -> executeTask(task, timeout)
                ) : CompletableFuture.supplyAsync(
                        () -> executeTask(task, timeout), threadPool)
        ).collect(Collectors.toList());

        CompletableFuture<List<TaskResult<R>>> allResultsFuture = CompletableFuture.allOf(
                futureList.toArray(new CompletableFuture[0])
        ).thenApply(v -> futureList.stream()
                .map(CompletableFuture::join) // 无检查异常，安全获取结果
                .collect(Collectors.toList()));
        return allResultsFuture.join();
    }

    /**
     * 异步执行多任务并获取结果
     *
     * @param task    任务
     * @param timeout 任务超时时间
     * @param <R>     任务执行返回类型
     * @return List<TaskResult<R>>
     */
    private static <R> TaskResult<R> executeTask(TaskExecutor<R> task, long timeout) {
        R result;
        try {
            if (timeout > 0) {
                // TODO: 先不考虑超时，创建线程池有风险
                Future<R> taskFuture = Executors.newSingleThreadExecutor().submit(task::execute);
                result = taskFuture.get(timeout, TimeUnit.MILLISECONDS);
            } else {
                result = task.execute();
            }
            return TaskResult.success(result);
        } catch (Exception e) {
            return TaskResult.fail(null, e);
        }
    }

    /**
     * 异步执行多任务并获取结果
     *
     * @param tasks 任务列表
     * @param <R>   任务执行返回类型
     * @return List<TaskResult<R>>
     */
    public static <R> List<TaskResult<R>> executeTasks(List<TaskExecutor<R>> tasks) {
        return executeTasks(tasks, null, 0);
    }

    /**
     * 异步执行多任务并获取结果
     *
     * @param tasks      任务列表
     * @param threadPool 线程池
     * @param <R>        任务执行返回类型
     * @return List<TaskResult<R>>
     */
    public static <R> List<TaskResult<R>> executeTasks(List<TaskExecutor<R>> tasks, ExecutorService threadPool) {
        return executeTasks(tasks, threadPool, 0);
    }

}
