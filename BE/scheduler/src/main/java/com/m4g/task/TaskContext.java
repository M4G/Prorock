package com.m4g.task;

import com.m4g.ErrorSeverity;
import com.m4g.quartz.TaskFacade;
import com.m4g.task.TaskStatus;

public interface TaskContext {
	
	public void setTaskInstanceId(Integer taskInstanceId);
	
	public Integer getTaskInstanceId();
	
	public void setTaskFacade(TaskFacade taskFacade);
	
	public Object getParameter(String key);
	
	public void setProgress(int precentage);
	
	public int getProgress();
	
	public void setAmount(int amount);
	
	public void setAmount(int amount, int total);
	
	public void setMessage(String message);
	
	public void setTaskStatus(TaskStatus taskStatus);
	
	public void setError(String errorMsg);
	
	public void setError(String errorMsg, String stackTrace, ErrorSeverity sevirity);
	
	public Object getBean(String beanName);
	
	public void setError(String messageError, Throwable t, ErrorSeverity sevirity);

	public void setDescription(String description);

	void setTypeParameter(String key, Object value);

	void setInstanceParameter(String key, Object value);

	public int getRetries();
	public void setRetries(int retries);
}
