package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWith4Args<R, T1, T2,T3,T4> {

    R excuter(T1 value1, T2 value2,T3 value3, T4 value4) throws Exception;
}
