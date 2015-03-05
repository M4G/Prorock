package com.m4g.quartz;

import com.m4g.ErrorSeverity;
import com.m4g.SchedulerService;
import com.m4g.task.TaskKey;
import com.m4g.task.TaskStatus;
import com.m4g.task.entities.TaskErrorEntity;
import com.m4g.task.entities.TaskInstanceEntity;

import java.util.List;
import java.util.Properties;

interface SchedulerFacade extends SchedulerService{

	public void setTaskStatus(Integer taskInstanceId, TaskStatus taskStatus);
	
	public void setTaskCompleteStatus(Integer taskInstanceId);
	
	public TaskInstanceEntity getWaitingTaskInstanceEntity(String name, String group);
	
	public TaskInstanceEntity getWaitingTaskInstanceEntity(TaskKey taskKey);

	public TaskInstanceEntity getWaitingTaskInstanceEntity(Integer taskId, List<Integer> idsList, String className);
	
	public void setProgress(Integer taskInstaceId, Integer progress);
	
	public void setMessage(Integer taskInstaceId, String message);
	
	public void setAmount(Integer taskInstaceId, Integer amount);
	
	public void setAmount(Integer taskInstaceId, Integer amount, int total);
	
	public void setError(Integer taskInstaceId, String messageError);
	
	public void setError(Integer taskInstaceId, String messageError, String stackTrace, ErrorSeverity Sevirity);
	
	public TaskInstanceEntity getTaskInstance(Integer taskInstanceId);
	
	public List<TaskErrorEntity> getTaskInstanceError(Integer taskInstanceId);
	
	public TaskStatus getTaskInstanceEntityStatus(Integer taskInstanceEntityId);
	
	public TaskInstanceEntity getCopyOfTaskInstanceEntity(Integer taskInstanceId);
	
	public Object getBean(String beanName);

	public void setDescription(Integer taskInstanceId, String description);

	public void setInstanceProperties(Integer taskInstanceId, Properties prop);

	public int getProgress(Integer taskInstanceId);

	public void setTypeProperties(Integer taskInstanceId,
                                  Properties taskTypeProperties);

	public int getRetries(Integer taskInstanceId);

	public void setRetries(Integer taskInstanceId, int retries);


}
