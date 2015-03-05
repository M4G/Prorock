package com.m4g.job.adapters;

import com.m4g.task.TaskStatus;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.transaction.annotation.Transactional;

import static com.m4g.SchedulerConstants.TASK_INSTANCE_ID;

/**
 * @author aharon_br
 * 
 *  RecoverableJobAdapter is a job adapter that has recovery abilities.<br>
 *  These abilities are (for now) read from JobDetailsMap <br>
 *  and write it own state to JobDetailsMap.  
 */
public class  RecoverableJobAdapter extends RunnableJobAdapter {
	
	@Override
	protected void setTaskStatus(JobDataMap jobDataMap, TaskStatus taskStatus) {
		Integer taskInstanceId = (Integer) jobDataMap.get(TASK_INSTANCE_ID);
		taskFacade.setTaskStatus(taskInstanceId, taskStatus);
	}
	
	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		super.execute(context);
	}
	
}
