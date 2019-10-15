package com.mph.sub;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 框架的主体类，也是调用者主要使用的类
 */
public class FrameJobPool {
    //定义核心线程数   保守估计
    private static final int THREAD_COUNTS = Runtime.getRuntime().availableProcessors();
    //任务队列
    private static BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(1000);

    //线程池 固定大小 有界队列
    private static ExecutorService taskExecutor = new ThreadPoolExecutor(THREAD_COUNTS, THREAD_COUNTS, 60,
            TimeUnit.SECONDS, taskQueue);

    //job 的存放容器
    private static ConcurrentHashMap<String, JobInfo<?>> jobInfoMap = new ConcurrentHashMap<>();

    private static CheckJobProcessor checkJob = CheckJobProcessor.getInstance();

    public static Map<String, JobInfo<?>> getMap() {
        return jobInfoMap;
    }

    //单例模式 --
    public FrameJobPool() {
    }

    private static class JobPoolHolder {
        public static FrameJobPool pool = new FrameJobPool();
    }

    public static FrameJobPool getInstance() {
        return JobPoolHolder.pool;
    }
    //单例模式--

    //对工作中的任务进行包装，提交给线程池使用，并处理任务的结果，写入缓存一共查询

    private static class FrameTask<T, R> implements Runnable {
        private JobInfo<R> jobInfo;
        private T processData;

        public FrameTask(JobInfo<R> jobInfo, T processData) {
            this.jobInfo = jobInfo;
            this.processData = processData;
        }

        @Override
        public void run() {
            R r = null;
            ITaskProcessor<T, R> taskProcessor = (ITaskProcessor<T, R>) jobInfo.getTaskProcessor();
            TaskResult<R> taskResult = null;
            //调用自己业务自己的实现方法
            try {
                taskResult = taskProcessor.taskExecute(processData);
                if (taskResult == null) {
                    taskResult = new TaskResult<R>(TaskResultType.Exception, r, "result is null");
                }

                if (taskResult.getTaskResultType() == null) {
                    if (taskResult.getReason() == null) {
                        taskResult = new TaskResult<R>(TaskResultType.Exception, r, "reason is null");
                    } else {
                        taskResult = new TaskResult<R>(TaskResultType.Exception, r, "result is null but reason not null,reason :" + taskResult.getReason());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                taskResult = new TaskResult<R>(TaskResultType.Exception, r,
                        e.getMessage());
            } finally {
                jobInfo.addTaskResult(taskResult, checkJob);
            }

        }
    }

    //根据工作名称检索工作
    private <R> JobInfo<R> getJob(String jobName) {
        JobInfo<R> jobInfo = (JobInfo<R>) jobInfoMap.get(jobName);
        if (jobInfo == null) {
            throw new RuntimeException(jobName + "是个非法任务。");
        }

        return jobInfo;
    }

    //调用者提交到工作中的任务
    public <T, R> void pusTask(String jobName, T t) {
        JobInfo<R> jobInfo = getJob(jobName);
        FrameTask<T, R> task = new FrameTask<T, R>(jobInfo, t);
        taskExecutor.execute(task);
    }

    //调用者注册工作，

    public <R> void registerJob(String jobName, int jobLength,
                                ITaskProcessor<?, ?> iTaskProcessor, long expireTime) {
        JobInfo<Object> jobInfo = new JobInfo<>(jobName, jobLength, iTaskProcessor, expireTime);
        //将新job 推送进 队列，如果已经存在则 返回
        if (jobInfoMap.putIfAbsent(jobName, jobInfo) != null) {
            throw new RuntimeException(jobName + "已经注册了！");
        }
    }

    //获取每个任务执行的详情
    public <R> List<TaskResult<R>> getTaskDetail(String jobName){
        JobInfo<R> jobInfo = getJob(jobName);
        return jobInfo.getTaskDetail();
    }

    //获取工作的整体处理进度
    public <R> String getTaskProgress(String jobName){
        JobInfo<Object> job = getJob(jobName);
        return  job.getTotalProcess();
    }



}
