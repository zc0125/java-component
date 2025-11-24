package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWith1Args<R, T> {

    R excuter(T value) throws Exception;
}
