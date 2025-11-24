package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWithNoArgs<R> {
    R excuter() throws Exception;
}
