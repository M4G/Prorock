package com.m4g.job.adapters;

import com.google.common.base.Preconditions;
import com.m4g.task.TaskStatus;
import com.m4g.task.dao.TaskInstanceDao;
import com.m4g.task.entities.TaskInstanceEntity;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.m4g.SchedulerConstants.TASK_INSTANCE_ID;

/**
 * @author aharon_br
 *
 * DurableJobAdapter is a JobAdapter that save it state at DB.
 */
@Component("durableJobAdapter")
public class DurableJobAdapter extends RunnableJobAdapter {

	protected TaskInstanceDao taskInstanceDao; 
	protected TaskInstanceEntity thisInstanceEntity;
	
	@Autowired
	public void setTaskInstanceDao(TaskInstanceDao instanceDao) {
		this.taskInstanceDao = instanceDao;
	}

	protected TaskInstanceEntity getWaitingTaskInstanceEntity(){
		return taskInstanceDao.getWaitingTaskInstanceEntity(taskKey.getName(), taskKey.getGroup());
	}

	public void setThisInstanceEntity(TaskInstanceEntity thisInstanceEntity) {
		this.thisInstanceEntity = thisInstanceEntity;
	}

	@Override
	public void setParameter(String parameterName, Object value) {
		
		Preconditions.checkNotNull(parameterName, "parameterName cannot be null");
        Preconditions.checkNotNull(thisInstanceEntity, "thisInstanceEntity cannot be null");
		
		thisInstanceEntity.addParameter(parameterName, value);
		taskInstanceDao.save(thisInstanceEntity);
	}
	
	protected boolean isValidToCreateNewInstance(TaskStatus status){
		if (status != TaskStatus.PENDING){
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		setTaskFacade(context);
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		Integer taskInstanceId = (Integer) jobDataMap.get(TASK_INSTANCE_ID);
		TaskInstanceEntity entity = taskFacade.getTaskInstance(taskInstanceId);
		Runnable runnable = getRunnable(context);
		// please fix all places to initilized new Instances with PENDING status
		if (isValidToCreateNewInstance(entity.getTaskStatus())){
			entity = taskFacade.getCopyOfTaskInstanceEntity(taskInstanceId);
			jobDataMap.put(TASK_INSTANCE_ID, entity.getTaskInstanceId());
		}
	//	setTaskContext(context, runnable, taskFacade, entity.getTaskInstanceId());
		super.execute(context);
	}

	@Override
	protected void setTaskStatus(JobDataMap jobDataMap, TaskStatus taskStatus) {
		Integer taskInstanceId = (Integer) jobDataMap.get(TASK_INSTANCE_ID);
		taskFacade.setTaskStatus(taskInstanceId, taskStatus);
	}

}
