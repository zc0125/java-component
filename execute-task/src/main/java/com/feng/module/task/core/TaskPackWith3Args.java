package com.feng.module.task.core;


@FunctionalInterface
public interface TaskPackWith3Args<R, T1, T2, T3> {

    R excuter(T1 value1, T2 value2, T3 value3) throws Exception;
}
