package com.m4g.task;

import com.m4g.ErrorSeverity;
import com.m4g.quartz.TaskFacade;

import java.util.Properties;

public class SimpleTaskContextImpl implements TaskContext {

	private Properties taskTypeProperties;
	private Properties taskInstanceProperties;
	private TaskFacade taskFacade;
	private Integer taskInstanceId;
	
	public SimpleTaskContextImpl(TaskFacade facade, Properties typeProperties, Properties instanceProperties, Integer instanceId){
		taskTypeProperties = typeProperties;
		taskInstanceProperties = instanceProperties;
		taskFacade = facade;
		taskInstanceId = instanceId;
	}
	
	@Override
	public Object getParameter(String key) {
		Object value = taskTypeProperties.get(key);
		if (value == null){
			return taskInstanceProperties.get(key);
		}else{
			return value;
		}
	}
	
	@Override
	public void setTypeParameter(String key, Object value) {
		taskTypeProperties.put(key, value);
		taskFacade.setTypeProperties(taskInstanceId, taskInstanceProperties);
	}
	
	@Override
	public void setInstanceParameter(String key, Object value) {
		taskInstanceProperties.put(key, value);
		taskFacade.setInstanceProperties(taskInstanceId, taskInstanceProperties);
	}
	
	@Override
	public void setProgress(int precentage) {
		taskFacade.setProgress(taskInstanceId, precentage);
	}

	@Override
	public int getProgress(){
		return taskFacade.getProgress(taskInstanceId);
	}
	
	@Override
	public void setAmount(int amount) {
		taskFacade.setAmount(taskInstanceId, amount);
	}

	@Override
	public void setTaskStatus(TaskStatus taskStatus) {
		taskFacade.setTaskStatus(taskInstanceId, taskStatus);
	}
	
	@Override
	public Integer getTaskInstanceId() {
		return taskInstanceId;
	}

	@Override
	public void setTaskInstanceId(Integer taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

	@Override
	public void setTaskFacade(TaskFacade taskFacade) {
		this.taskFacade = taskFacade;
	}
	
	@Override
	public void setError(String errorMsg) {
		taskFacade.setError(taskInstanceId, errorMsg);
	}

	@Override
	public void setError(String errorMsg, String stackTrace,
			ErrorSeverity sevirity) {
		taskFacade.setError(taskInstanceId, errorMsg, stackTrace, sevirity);
	}

	@Override
	public void setError(String errorMsg, Throwable t,
			ErrorSeverity sevirity) {
		taskFacade.setError(taskInstanceId, errorMsg, t, sevirity);
	}

	@Override
	public void setAmount(int amount, int total) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMessage(String message) {
		taskFacade.setMessage(taskInstanceId, message);
	}
	
	public Object getBean(String beanName){
		return taskFacade.getBean(beanName);
	}

	@Override
	public void setDescription(String description) {
		taskFacade.setDescription(taskInstanceId, description);
		
	}

	@Override
	public int getRetries() {
		return taskFacade.getRetries(taskInstanceId);
	}

	@Override
	public void setRetries(int retries) {
		taskFacade.setRetries(taskInstanceId,retries);
	}
}
