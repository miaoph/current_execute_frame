package com.mph.sub;

import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;

/**
 * 类说明
 * 任务完成后，在一定的时间提供查询，之后为释放资源节约内存，需要定期处理过期的任务
 */
public class CheckJobProcessor {
    private static DelayQueue<ItemVo<String>> queue = new DelayQueue<>();//存放已完成等待过期的队列

    //单例模式---
    private CheckJobProcessor() {
    }

    private static class ProcessorHolder {
        public static CheckJobProcessor processor = new CheckJobProcessor();
    }

    public static CheckJobProcessor getInstance() {
        return ProcessorHolder.processor;
    }
    //单例模式---

    //处理队列中到期的任务
    private static class FetchJob implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    //拿到已经过期的任务
                    ItemVo<String> item = queue.take();
                    String jobName = item.getDate();
                    FrameJobPool.getMap().remove(jobName);
                    System.out.println(jobName + " is out of date,remove from map!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 任务完成之后将任务放入队列，经过expireTime时间后，从整个框架中移除
     */
    public void putJob(String jobName, long expireTime) {
        ItemVo<String> item = new ItemVo<>(expireTime, jobName);
        queue.offer(item);
        System.out.println("Job["+jobName+"已经放入了过期检查缓存，过期时长："+expireTime);
    }

    @PostConstruct
    public  void  init(){
        Thread thread = new Thread(new FetchJob());
        thread.setDaemon(true);
        thread.start();
        System.out.println("开启任务过期检查守护线程................");
    }
}
