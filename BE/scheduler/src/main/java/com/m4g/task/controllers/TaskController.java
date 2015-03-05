package com.m4g.task.controllers;

import com.m4g.task.TaskIdentifier;
import com.m4g.task.TaskStatus;

// This interface give user some control of task execution
public interface TaskController {
	
	/**
	 * This method pause the task.
	 */
	public void pauseTask();
	
	/**
	 * This method resume the task.
	 */
	
	public void resumeTask();
	
	/**
	 * 
	 * @return TaskStatus. The status of the task.
	 */
	public TaskStatus getTaskStatus();
	
	/**
	 * 
	 * @return the task Identifier of the task
	 */
	public TaskIdentifier getIdentifier();
}

