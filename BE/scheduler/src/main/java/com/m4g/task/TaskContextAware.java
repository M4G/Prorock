package com.m4g.task;

public interface TaskContextAware {

	public void setTaskContext(TaskContext taskContext);
	
	public boolean hasTaskContext();
}
