package com.feng.module.task.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TaskModel<R> {
    private final Object bean;
    private final Method method;
    private final Object[] args;

    private TaskModel(Object bean, Method method, Object[] args) {
        this.bean = bean;
        this.method = method;
        this.args = args;
    }

    public TaskModel<R> withArgs(Object... args) {
        return init(bean, method, args);
    }

    public R execute() {
        return execute(bean, method, args);
    }

    public static <R> TaskModel<R> init(Object bean, Method method, Object... args) {
        return new TaskModel<R>(bean, method, args);
    }

    public static <R> TaskModel<R> init(Object bean, Method method) {
        return init(bean, method, new Object[0]);
    }


    /**
     * 通用反射执行方法
     *
     * @param bean   目标对象（静态方法传 null）
     * @param method 要执行的方法对象
     * @param args   方法参数（无参传 null 或空数组）
     * @param <R>    返回值泛型类型（需与方法实际返回类型一致）
     * @return 方法执行结果
     * @throws IllegalArgumentException 参数非法（bean/method 为空、参数不匹配等）
     */
    @SuppressWarnings("unchecked")
    private static <R> R execute(Object bean, Method method, Object[] args) {
        // 步骤 1：校验核心参数（bean 和 method 非空校验）
        // method 必须非空
        if (method == null) {
            throw new IllegalArgumentException("[Execute-Task] Bean Method cannot be empty.");
        }
        // 非静态方法：bean 必须非空（静态方法 bean 可传 null）
        if (!method.getDeclaringClass().isAssignableFrom(bean.getClass()) && !java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("[Execute-Task] The target object bean of a non-static method cannot be empty and must be an instance of the method declaring class.");
        }
        // 步骤 2：处理参数数组（null 转为空数组，避免 invoke 传 null 报错）
        args = (args == null) ? new Object[0] : args;

        // 步骤 3：校验参数数量匹配（避免参数个数不匹配异常）
        validateParamCount(method, args);

        // 步骤 4：设置方法可访问性（突破私有/保护方法的访问限制）
        method.setAccessible(true);

        // 步骤 5：执行方法（捕获并封装反射相关异常）
        Object resultObj;
        try {
            // 执行方法：静态方法 bean 传 null，实例方法 bean 为目标对象
            return (R) method.invoke(bean, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            if (e instanceof IllegalAccessException) {
                throw new IllegalArgumentException(
                        String.format(e.getMessage())
                );
            }
            throw new RuntimeException("[Execute-Task] Method execution throws a business exception.", e);
        }
    }

    /**
     * 校验参数数量：实际传入的参数个数必须与方法声明的参数个数一致
     */
    private static void validateParamCount(Method method, Object[] actualArgs) {
        int declaredParamCount = method.getParameterCount(); // 方法声明的参数个数
        int actualParamCount = actualArgs.length; // 实际传入的参数个数

        if (declaredParamCount != actualParamCount) {
            throw new IllegalArgumentException(
                    String.format("[Execute-Task] argument count mismatch: method declared %d arguments, passed in %d.",
                            declaredParamCount, actualParamCount)
            );
        }
    }


}
