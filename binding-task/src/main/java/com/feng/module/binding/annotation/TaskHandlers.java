package com.feng.module.binding.annotation;

import java.lang.annotation.*;

/**
 * TaskHandler复合注解
 *
 * @author zc
 * Version 1.0
 * Date 2025/03/11 22:05
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TaskHandlers {
    TaskHandler[] value();
}
