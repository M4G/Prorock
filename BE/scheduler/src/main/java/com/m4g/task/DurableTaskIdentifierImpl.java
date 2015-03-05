package com.m4g.task;

import com.m4g.job.adapters.RunnableJobAdapter;

public class DurableTaskIdentifierImpl extends RunnableTaskIdentifier implements DurableTaskIdentifier {

	public DurableTaskIdentifierImpl(TaskKey taskKey, Runnable runnable) {
		super(taskKey, runnable);
	}
	
	public DurableTaskIdentifierImpl(RunnableJobAdapter jobAdapter){
		super(jobAdapter);
	}
	
	private Integer taskTypeId;
	private Integer taskInstanceId;

	@Override
	public Integer getTaskTypeId() {
		return taskTypeId;
	}

	@Override
	public void setTaskTypeId(Integer taskTypeId) {
		this.taskTypeId = taskTypeId;
	}

	@Override
	public Integer getTaskInstanceId() {
		return taskInstanceId;
	}

	@Override
	public void setTaskInstanceId(Integer taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}
	
	public void updateWaitingInstanceName(String instanceName){
		this.physicalName = instanceName;
	}
	
}

