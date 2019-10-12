package com.mph.sub;

/**
 * 框架使用任务接口，因为任务性质在调用时才会知道，故使用泛型传入
 * @param <T>
 * @param <R>
 */
public interface ITaskProcesser<T,R> {
    /**
     * @param data 调用方法需要使用的业务数据
     * @return 方法执行后业务方法的结果
     */
    TaskResult<R> taskExecute(T data);
}
