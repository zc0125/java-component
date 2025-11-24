package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWith5Args<R, T1, T2, T3, T4, T5> {

    R excuter(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5) throws Exception;
}
