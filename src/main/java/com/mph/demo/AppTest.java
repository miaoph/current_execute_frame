package com.mph.demo;

import java.util.List;
import java.util.Random;

import com.mph.sub.FrameJobPool;
import com.mph.sub.TaskResult;
import com.mph.util.SleepTools;

/**
 *@author Mark老师   享学课堂 https://enjoy.ke.qq.com 
 *
 *类说明：模拟一个应用程序，提交工作和任务，并查询任务进度
 */
public class AppTest {
	
	private final static String JOB_NAME = "计算数值";
	private final static int JOB_LENGTH = 1000;
	
	//查询任务进度的线程
	private static class QueryResult implements Runnable{
		
		private FrameJobPool pool;

		public QueryResult(FrameJobPool pool) {
			super();
			this.pool = pool;
		}

		@Override
		public void run() {
			int i=0;//查询次数
			while(i<350) {
				List<TaskResult<String>> taskDetail = pool.getTaskDetail(JOB_NAME);
				if(!taskDetail.isEmpty()) {
					System.out.println(pool.getTaskProgress(JOB_NAME));
					System.out.println(taskDetail);					
				}
				SleepTools.currentSleep(100);
				i++;
			}
		}
		
	}

	public static void main(String[] args) {
		MyTask myTask = new MyTask();
		//拿到框架的实例
		FrameJobPool pool = FrameJobPool.getInstance();
		//注册job
		pool.registerJob(JOB_NAME, JOB_LENGTH, myTask,1000*5);
		Random r = new Random();
		for(int i=0;i<JOB_LENGTH;i++) {
			//依次推入Task
			pool.pusTask(JOB_NAME, r.nextInt(1000));
		}
		Thread t = new Thread(new QueryResult(pool));
		t.start();
	}
}
