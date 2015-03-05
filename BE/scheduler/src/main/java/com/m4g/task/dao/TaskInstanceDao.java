package com.m4g.task.dao;


import com.m4g.task.TaskStatus;
import com.m4g.task.entities.TaskInstanceEntity;

import java.util.List;

public interface TaskInstanceDao extends Dao<TaskInstanceEntity, Integer> {

	public void deleteTaskInstance(Integer taskInstanceId);
	
	public TaskInstanceEntity getTaskInstance(Integer taskInstanceId);
	
	public List<TaskInstanceEntity> getTaskInstanceByStatus(TaskStatus status);
	
	public List<TaskInstanceEntity> getTaskInstanceByStatusAndGroup(TaskStatus status, String groupName);
	
	public void setTaskStatus(Integer taskInstanceId, TaskStatus taskStatus);
	
	public TaskInstanceEntity getWaitingTaskInstanceEntity(String name, String group);
	
	public TaskInstanceEntity getWaitingTaskInstanceEntity(Integer taskId, List<Integer> idsList, String className);
	
	public void setProgress(Integer taskInstanceId, Integer progress);
	
	public void setAmount(Integer taskInstanceId, Integer amount);
	
	public void setAmount(Integer taskInstanceId, Integer amount, int total);
	
	public void  setMessage(Integer taskInstanceId, String message);
	
	public TaskStatus getTaskInstanceEntityStatus(Integer taskInstanceId);
	
	public TaskStatus getTaskInstanceEntityStatus(String phisycalName);
	
	public TaskInstanceEntity getCopyOfTaskInstanceEntity(Integer taskInstanceId);
	

	void setDescription(Integer taskInstanceId, String description);

	public void setProperties(Integer taskInstanceId, String createClob);

	public int getProgress(Integer taskInstanceId);
	
	public int getMaxTaskId();
}
