package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWith2Args<R, T1, T2> {

    R excuter(T1 value1, T2 value2) throws Exception;
}
