package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWithAnyArgs<R> {

    R excuter(Object... args) throws Exception;
}
