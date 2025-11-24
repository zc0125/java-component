package com.feng.module.binding.annotation;

import java.lang.annotation.*;

/**
 * TaskBinding复合注解
 *
 * @author zc
 * Version 1.0
 * Date 2025/03/11 22:01
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TaskBindings {

    TaskBinding[] value();

}
