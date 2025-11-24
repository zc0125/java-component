package com.feng.module.binding.annotation;

import java.lang.annotation.*;

/**
 * 任务绑定注解（适用于{@link TaskBinding}绑定到类上的情况）
 *
 * @author zc
 * Version 1.0
 * Date 2025/03/11 22:02
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(TaskHandlers.class)
public @interface TaskHandler {
    /**
     * 方法绑定名
     */
    String function() default "";
}
