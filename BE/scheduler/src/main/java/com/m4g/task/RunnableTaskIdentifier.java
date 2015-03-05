package com.m4g.task;

import com.m4g.job.adapters.RunnableJobAdapter;

public class RunnableTaskIdentifier implements TaskIdentifier {

	protected String physicalName;
	private String logicalName;
	private String group;
	private String user; 
	@SuppressWarnings("unused")// unused locally but used outside
	private Integer taskInstanceId;
	private Integer taskTypeId;


	private RunnableTaskIdentifier(){
	}

	public RunnableTaskIdentifier(RunnableJobAdapter jobAdapter){
		TaskKey taskKey = jobAdapter.getTaskKey();
		this.logicalName = taskKey.getName();
		this.group =  taskKey.getGroup();
		this.user = taskKey.getUser();
		generatePhysicalName(jobAdapter);
	}

	public RunnableTaskIdentifier(TaskKey taskKey, Runnable runnable){
		if (physicalName == null){
			this.logicalName = taskKey.getName();
			this.group =  taskKey.getGroup();
			this.user = taskKey.getUser();
			generatePhysicalName(runnable);
		}
	}

	private void generatePhysicalName(Runnable runnable) {
		if (physicalName == null){
			physicalName = TasksUtils.createTimeOrientedClassName(runnable);
		}
	}

	private void generatePhysicalName(RunnableJobAdapter jobAdapter) {
		if (physicalName == null){
			physicalName = TasksUtils.createTimeOrientedClassName(jobAdapter.getRunnable());			
		}
	}				

	@Override
	public String getTaskTypeName() {
		return logicalName;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public String getTaskInstanceName() {
		return physicalName;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public Integer getTaskInstanceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getTaskTypeId() {
		return this.taskTypeId;
	}

	public void setTaskTypeId(Integer taskTypeId) {
		this.taskTypeId = taskTypeId;
	}

	public void setTaskInstanceId(Integer taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}			
}
