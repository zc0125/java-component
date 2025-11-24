package com.feng.module.binding.core;

import com.feng.module.binding.annotation.TaskBinding;
import com.feng.module.binding.annotation.TaskHandler;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务绑定的数据池
 * @author zc
 * Version 1.0
 * Date 2025/03/13 21:33
 */
public class TaskBindingPoll {
    private final ConcurrentMap<MethodData, MethodData> methodDataSet = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<TaskBindingData>> taskBindingPollMap = new ConcurrentHashMap<>();

    // 私有化
    private TaskBindingPoll() {
    }

    /**
     * 构建单例实例
     */
    private static class GenSingleton {
        //静态常量：类加载时初始化，仅一次
        private static final TaskBindingPoll INSTANCE = new TaskBindingPoll();
    }

    /**
     * 获取单例实例
     *
     * @return TaskBindingManage
     */
    public static TaskBindingPoll getInstance() {
        return GenSingleton.INSTANCE;
    }

    /**
     * 设置任务和方法实现绑定
     *
     * @param taskBinding 任务绑定注解实例
     * @param taskHandler handler绑定实例（可能为空）
     * @param method      绑定的方法
     * @param bean        方法的实例
     * @param beanName    实例名字
     */
    public void setTaskBinding(TaskBinding taskBinding, TaskHandler taskHandler, Method method, Object bean, String beanName) {
        MethodData methodData = new MethodData(bean, method, beanName);
        methodData = methodDataSet.computeIfAbsent(methodData, this::buildMethodData);
        TaskBindingData taskBindingData = new TaskBindingData(taskBinding, taskHandler, methodData);
        taskBindingPollMap.computeIfAbsent(taskBindingData.getKey(), this::buildTaskBindingDataSet).add(taskBindingData);
    }

    /**
     * 获取绑定的任务组
     *
     * @param serviceName  服务名
     * @param moduleName   模块名
     * @param functionName 功能名
     * @return Set<TaskBindingData>
     */
    public Set<TaskBindingData> getTaskBindingGroup(String serviceName, String moduleName, String functionName) {
        return taskBindingPollMap.getOrDefault(generateKey(serviceName, moduleName, functionName), null);
    }

    /**
     * 安全构建MethodData，直接返回当前对象
     *
     * @param methodData MethodData对象
     * @return methodData
     */
    private MethodData buildMethodData(MethodData methodData) {
        return methodData;
    }

    /**
     * 安全构建Set<TaskBindingData>
     *
     * @param key generateKey生成
     * @return 空的Set<TaskBindingData>
     */
    private Set<TaskBindingData> buildTaskBindingDataSet(String key) {
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * 生成方法绑定唯一key
     *
     * @param serviceName  服务名
     * @param moduleName   模块名
     * @param functionName 功能名
     * @return key
     */
    private String generateKey(String serviceName, String moduleName, String functionName) {
        return String.format(
                "Task:%s-%s-%s",
                serviceName == null ? "" : serviceName,
                moduleName == null ? "" : moduleName,
                functionName == null ? "" : functionName
        );
    }

    /**
     * 任务绑定的元数据
     */
    public static class TaskBindingData {
        private final TaskBinding taskBinding;
        private final TaskHandler taskHandler;
        private final MethodData methodData;
        private final String key;

        private TaskBindingData(TaskBinding taskBinding, TaskHandler taskHandler, MethodData methodData) {
            this.taskBinding = taskBinding;
            this.taskHandler = taskHandler;
            this.methodData = methodData;
            this.key = getInstance().generateKey(taskBinding.service(), taskBinding.module(), taskHandler == null ? "" : taskHandler.function());
        }

        /**
         * 获取key
         */
        private String getKey() {
            return key;
        }

        /**
         * 获取绑定的TaskBinding
         *
         * @return TaskBinding
         */
        public TaskBinding getTaskBinding() {
            return taskBinding;
        }

        /**
         * 获取绑定的TaskHandler
         *
         * @return TaskHandler
         */
        public TaskHandler getTaskHandler() {
            return taskHandler;
        }

        /**
         * 获取方法实例的bean
         *
         * @return bean
         */
        public Object getBean() {
            return methodData.bean;
        }

        /**
         * 获取方法实例的method
         *
         * @return method
         */
        public Method getMethod() {
            return methodData.method;
        }

        @Override
        public int hashCode() {
            return (key.hashCode() + "-" + methodData.hashCode()).hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TaskBindingData) {
                TaskBindingData target = (TaskBindingData) obj;
                return this.key.equals(target.key) && this.methodData.equals(target.methodData);
            }
            return false;
        }
    }

    /**
     * 方法封装的元数据
     */
    static class MethodData {
        private final Object bean;
        private final Method method;
        private final String beanName;
        private final String methodName;

        MethodData(Object bean, Method method, String beanName) {
            this.bean = bean;
            this.method = method;
            this.beanName = beanName;
            this.methodName = method.getName();
        }

        @Override
        public int hashCode() {
            return (bean.hashCode() + "-" + method.hashCode()).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof MethodData) {
                MethodData target = (MethodData) obj;
                return this.beanName.equals(target.beanName) && methodName.equals(target.methodName) && bean.equals(target.bean) && method.equals(target.method);
            }
            return false;
        }
    }
}
