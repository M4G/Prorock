package com.m4g.task.dao;


import com.m4g.job.adapters.RunnableJobAdapter;
import com.m4g.task.TaskKey;
import com.m4g.task.entities.TaskTypeEntity;

import java.util.List;


public interface TaskTypeDao extends Dao<TaskTypeEntity, Integer> {
	
	public TaskTypeEntity getTaskTypeEntity(String name, String group);
	
	public TaskTypeEntity persistTaskTypeEntity(RunnableJobAdapter JobAdapter);
	
	public List<TaskTypeEntity> getAllTaskTypeEntities();

	public TaskTypeEntity persistTaskTypeEntity(Runnable runnable, TaskKey taskKey);

	public TaskTypeEntity findByTaskExcClass(String excClassFullName);

	public void setProperties(Integer taskId, String createClob);
}
