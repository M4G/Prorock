package com.m4g.task.dao;

import com.m4g.ErrorSeverity;
import com.m4g.task.entities.TaskErrorEntity;
import com.m4g.task.entities.TaskInstanceEntity;

import java.util.List;

public interface TaskErrorDao extends Dao<TaskErrorEntity, Integer> {

	/**
	 * @param taskInstance the id of the taskInstance we want to create an error entity
	 * @param message message describing the error of task
	 * 
	 * This method creates TaskErrorEntity associated with taskInstance
	 */
	public void addError(TaskInstanceEntity taskInstance, String message);

	
	/**
	 * @param taskInstance the id of the taskInstance we want to create an error entity
	 * @param message message describing the error of task
	 * @param stackTrace Error (Exception) stackTrace
	 * @param severity the severity of the error.
	 */
	public void addError(TaskInstanceEntity taskInstance, String message, String stackTrace, ErrorSeverity severity);
	
	/**
	 * 
	 * @param taskInstanceId the id of the taskInstance we want to retrieve all its errors 
	 * @return List of all error associated with a specific taskInsatnce 
	 */
	public List<TaskErrorEntity> getTaskErrors(Integer taskInstanceId); 
}
