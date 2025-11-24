package com.feng.module.binding.annotation;

import java.lang.annotation.*;


/**
 * 任务绑定注解，可绑定到方法或者类上；
 * 绑定到类上配合{@link TaskHandler}使用
 *
 * @author zc
 * Version 1.0
 * Date 2025/03/11 21:54
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(TaskBindings.class)
public @interface TaskBinding {
    /**
     * 服务名
     */
    String service();

    /**
     * 模块名
     */
    String module() default "";
}
