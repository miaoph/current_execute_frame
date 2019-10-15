package com.mph.sub;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类说明：提交给框架执行的工作实体类,
 * 工作：表示本批次需要处理的同性质任务(Task)的一个集合
 * @param <R>
 */
public class JobInfo<R> {
    //区分唯一的工作
    private  final String jobName;
    //工作任务的个数
    private  final  int jobLength;
    //这个工作的任务处理器
    private final ITaskProcessor<?,?> taskProcessor;
    //成功处理的任务数
    private final AtomicInteger successCount;
    //已经处理的任务数
    private  final AtomicInteger taskProcessorCount;
    //结果队列，拿结果从头拿，放结果加入队列尾部
    private final LinkedBlockingDeque<TaskResult<R>> taskResultDetailQueue;
    //工作完成保存的时间，超过时间自动清除
    private final  long expireTime;

    public JobInfo(String jobName, int jobLength, ITaskProcessor<?, ?> taskProcessor, long expireTime) {
        this.jobName = jobName;
        this.jobLength = jobLength;
        this.taskProcessor = taskProcessor;
        this.successCount = new AtomicInteger(0);
        this.taskProcessorCount = new AtomicInteger(0);
        //定义结果队列，长度与任务队列长度相同
        this.taskResultDetailQueue = new LinkedBlockingDeque<TaskResult<R>>(jobLength);
        this.expireTime = expireTime;
    }

    //获取任务
   public ITaskProcessor<?,?> getTaskProcessor(){
        return taskProcessor;
   }

   //获取返回成功的结果数
    public  int getSuccessCount(){
        return  successCount.get();
    }
    //获取当前已经处理的结果数
    public  int  getTaskProcessorCount(){
        return taskProcessorCount.get();
    }

    //提供工作中失败的次数
    public  int getTaskFailCount(){
        return  taskProcessorCount.get()-successCount.get();
    }

    public String getTotalProcess() {
        return "Success["+successCount.get()+"]/Current["
                + taskProcessorCount.get()+"] Total["+jobLength+"]";
    }

    //获取工作中 每个任务的处理详情
    public List<TaskResult<R>> getTaskDetail(){
        List<TaskResult<R>> taskList = new LinkedList<>();
        TaskResult taskResult;
        //从阻塞队列中取，反复取，一直取到null为止，说明目前队列中最新的任务结果已经处理完，
        while ((taskResult=taskResultDetailQueue.pollFirst()) != null){
            taskList.add(taskResult);
        }
        return taskList;
    }

    //放任务结果，
    public  void  addTaskResult(TaskResult<R> taskResult, CheckJobProcessor checkJob){
        if(TaskResultType.Success.equals(taskResult.getTaskResultType()) ){
            successCount.incrementAndGet();
        }
        //将处理的任务添加到结果队列的队尾
        taskResultDetailQueue.addLast(taskResult);
        //已处理任务添加1
        taskProcessorCount.incrementAndGet();
        if(taskProcessorCount.get() ==jobLength){
            checkJob.putJob(jobName,expireTime);
        }
    }
}
