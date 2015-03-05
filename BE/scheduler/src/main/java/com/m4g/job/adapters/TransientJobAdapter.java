package com.m4g.job.adapters;

import com.m4g.exceptions.JobInterruptedException;
import com.m4g.job.LifeCycleAware;
import com.m4g.task.TaskStatus;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static com.m4g.SchedulerConstants.*;

public class TransientJobAdapter extends RunnableJobAdapter {
	public TransientJobAdapter(){
		super();
	}

	@Override
	protected void setTaskStatus(JobDataMap jobDataMap, TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}
	
	@Override
	 public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			if (runnable instanceof LifeCycleAware){
				if (!cancel){
					beforeExecution();
					execute();
					afterExecution();
				}
			}else{
				if (!cancel){
					execute();
				}
			}			
		} catch (JobInterruptedException e){
			if (runnable != null){
				logger.warn(JOB_CANCELD_BY_USER_RUNNABLE + runnable.getClass().getName(), e);
			}else{
				logger.warn(JOB_CANCELD_BY_USER, e);
			}
		}catch (Exception e) {
			if (runnable != null){
				logger.error(EXCEPTION_HAS_OCCURS_WHILE_JOB_EXECUTING + runnable.getClass().getName(), e);
			}else{
				logger.error(EXCEPTION_WHILE_EXECUTING, e);
			}
		}
	}	
	
	
}
