package com.m4g.quartz;

import com.m4g.job.adapters.RunnableJobAdapter;
import com.m4g.task.TaskKey;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.impl.JobDetailImpl;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static com.m4g.SchedulerConstants.*;
import static org.quartz.JobBuilder.newJob;

public class QuartzUtils {
	
	private static void putTaskKeyInJobDataMap(JobDetail jobDetail, TaskKey taskKey){
		jobDetail.getJobDataMap().put(TASKKEY_KEY, taskKey);
	}
	
	public static void storeTaskId(JobDetail jobDetail, Integer taskId){
		jobDetail.getJobDataMap().put(TASK_ID_KEY, taskId);
	}
	
	private static void putRunnableInJobDataMap(JobDetail jobDetail, RunnableJobAdapter jobAdapter){
		jobDetail.getJobDataMap().put(RUNNABLE_KEY, jobAdapter.getRunnable());
	}
	
	public static JobDetail createJobDetail(RunnableJobAdapter jobAdapter, TaskKey taskKey, boolean recoveryMode) {
		JobDetail jobDetail = newJob(jobAdapter.getClass())
		.withIdentity(taskKey.getName(), taskKey.getGroup())
		.build();
		
		putRunnableInJobDataMap(jobDetail, jobAdapter);
		putTaskKeyInJobDataMap(jobDetail, taskKey);
		
		((JobDetailImpl) jobDetail).setDurability(false); 
		if (recoveryMode){
			jobDetail.requestsRecovery();
		}
		return jobDetail;
	}
	
	public static JobDetail createJobDetail(RunnableJobAdapter jobAdapter, boolean recoveryMode) {
		TaskKey taskKey =  jobAdapter.getTaskKey();
		JobDetail jobDetail = newJob(jobAdapter.getClass())
		.withIdentity(taskKey.getName(), taskKey.getGroup())
		.build();
		
		putRunnableInJobDataMap(jobDetail, jobAdapter);
		putTaskKeyInJobDataMap(jobDetail, taskKey);
		
		((JobDetailImpl) jobDetail).setDurability(false);
		if (recoveryMode){
			jobDetail.requestsRecovery();
		}
		return jobDetail;
	}
	
	public static void storePropertiesInJobDataMap(JobDetail jobDetail, Map<String, Object> parameters){
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		for (String key: parameters.keySet()){
			Object value = jobDataMap.get(key);
			jobDataMap.put(key, value);
		}
	}
	
	public static void storePropertiesInJobDataMap(JobDetail jobDetail, Properties properties){
		for(Iterator itr =  properties.keySet().iterator(); itr.hasNext(); ){
			Object key = itr.next();
			Object value = properties.get(key);
			jobDetail.getJobDataMap().put((String) key, value);
		}
	}
	
	public static Properties getParametersFromJobDataMap(JobDetail jobDetail){
		Properties properties = new Properties();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		
		for(Iterator itr = jobDataMap.keySet().iterator(); itr.hasNext();){
			Object key = itr.next();
			Object value = jobDataMap.get(key);
			properties.put(key, value);
		}
		return properties;
	}
	
	
}
