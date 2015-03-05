package com.m4g.task.controllers;

import com.m4g.*;
import com.m4g.job.adapters.RunnableJobAdapter;
import com.m4g.task.RunnableTaskIdentifier;
import com.m4g.task.TaskIdentifier;
import com.m4g.task.TaskStatus;
import com.m4g.task.TaskKey;

public class RunnableTaskController implements TaskController {

	private RunnableJobAdapter jobAdapter;
	private SchedulerService service;
	private TaskKey taskKey;
	
	protected TaskIdentifier identifier;
	
	private RunnableTaskController(){
	}
	
	protected void createIdentifier(TaskKey taskKey, Runnable runnable){
		this.identifier = new RunnableTaskIdentifier(taskKey, runnable);
	}
	
	RunnableTaskController(RunnableJobAdapter jobAdapter, SchedulerService service){
		this.jobAdapter = jobAdapter;
		this.service = service; 
		this.identifier = new RunnableTaskIdentifier(jobAdapter);
	}
	
	@Override
	public void pauseTask() {
		service.pauseJob(identifier);
	}

	@Override
	public void resumeTask() {
		service.resumeJob(identifier);

	}

	@Override
	public TaskIdentifier getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(TaskIdentifier identifier) {
		this.identifier = identifier;
	}


	@Override
	public TaskStatus getTaskStatus() {
		// TODO Auto-generated method stub
		return null;
	}
}
