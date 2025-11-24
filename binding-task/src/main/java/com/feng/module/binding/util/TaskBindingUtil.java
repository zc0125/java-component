package com.feng.module.binding.util;

import com.feng.module.binding.annotation.TaskBinding;
import com.feng.module.binding.annotation.TaskHandler;
import com.feng.module.binding.config.BindingTaskConfig;
import com.feng.module.binding.core.TaskBindingPoll;
import com.feng.module.binding.model.TaskBindingResult;
import com.feng.module.task.core.TaskExecutor;
import com.feng.module.task.model.TaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 任务绑定执行工具
 *
 * @author zc
 * Date 2025/03/15 22:13
 * Version 1.0
 */
public class TaskBindingUtil {

    private static List<TaskBindingPoll.TaskBindingData> getTaskBindingGroup(String serviceName, String moduleName, String functionName, Boolean isOne) {
        Set<TaskBindingPoll.TaskBindingData> taskBindingGroup = TaskBindingPoll.getInstance().getTaskBindingGroup(serviceName, moduleName, functionName);
        if (taskBindingGroup == null || taskBindingGroup.isEmpty()) {
            throw new RuntimeException(String.format("[Binding-Task] Task does not exist. Please check:{service=%s,module=%s,function=%s}", serviceName, moduleName, functionName));
        }
        if (Boolean.TRUE.equals(isOne) && taskBindingGroup.size() > 1) {
            throw new RuntimeException(String.format("[Binding-Task] Task count is greater than 1. Please check :{service=%s,module=%s,function=%s}", serviceName, moduleName, functionName));
        }
        return new ArrayList<>(taskBindingGroup);
    }

    /**
     * 多任务异步执行并获取返回值
     *
     * @param serviceName  {@link TaskBinding}服务名
     * @param moduleName   {@link TaskBinding} 模块名
     * @param functionName {@link TaskHandler} 模块名
     * @param args         执行参数
     * @param <T>          任务返回类型
     * @return List<TaskBindingResult<T>>
     */
    public static <T> List<TaskBindingResult<T>> executeAll(String serviceName, String moduleName, String functionName, Object... args) {
        List<TaskBindingPoll.TaskBindingData> taskBindingGroup = getTaskBindingGroup(serviceName, moduleName, functionName, false);
        List<TaskExecutor<T>> taskExecutors = taskBindingGroup.stream()
                .map(taskBinding -> TaskExecutor.<T>init(taskBinding.getBean(), taskBinding.getMethod(), args))
                .collect(Collectors.toList());
        List<TaskResult<T>> taskResults = TaskExecutor.executeTasks(taskExecutors, BindingTaskConfig.getThreadPoll());
        return IntStream.range(0, taskBindingGroup.size()).mapToObj(i ->
                TaskBindingResult.init(serviceName, moduleName, functionName,
                        taskBindingGroup.get(i).getBean(),
                        taskBindingGroup.get(i).getMethod(),
                        taskResults.get(i))
        ).collect(Collectors.toList());
    }

    /**
     * 任务执行并获取返回值
     *
     * @param serviceName  {@link TaskBinding}服务名
     * @param moduleName   {@link TaskBinding} 模块名
     * @param functionName {@link TaskHandler} 模块名
     * @param args         执行参数
     * @param <T>          任务返回类型
     * @return T
     */
    public static <T> T executeOne(String serviceName, String moduleName, String functionName, Object... args) {
        TaskBindingPoll.TaskBindingData taskBinding = getTaskBindingGroup(serviceName, moduleName, functionName, true).get(0);
        return TaskExecutor.<T>init(taskBinding.getBean(), taskBinding.getMethod(), args).execute();
    }


}
