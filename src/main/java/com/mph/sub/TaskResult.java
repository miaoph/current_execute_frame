package com.mph.sub;

/**
 * 处理任务返回结果实体类
 * @param <R>
 */
public class TaskResult<R> {
    //方法本身运行是否正确的结果类型
    private final  TaskResultType taskResultType;
    //方法的业务结果数据
    private final R returnValue;
    //原因
    private final String reason;

    public TaskResult(TaskResultType taskResultType, R returnValue, String reason) {
        super();
        this.taskResultType = taskResultType;
        this.returnValue = returnValue;
        this.reason = reason;
    }

    public TaskResult(TaskResultType taskResultType, R returnValue) {
        super();
        this.taskResultType = taskResultType;
        this.returnValue = returnValue;
        this.reason = "Success";
    }

    public  TaskResultType getTaskResultType(){
        return  taskResultType;
    }

    public  R getReturnValue(){
        return  returnValue;
    }
    public String getReason() {
        return reason;
    }
    @Override
    public String toString() {
        return "TaskResult{" +
                "taskResultType=" + taskResultType +
                ", returnValue=" + returnValue +
                ", reason='" + reason + '\'' +
                '}';
    }
}
