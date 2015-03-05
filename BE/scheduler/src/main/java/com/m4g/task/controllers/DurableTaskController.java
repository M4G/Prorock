package com.m4g.task.controllers;

import com.m4g.*;
import com.m4g.job.adapters.RunnableJobAdapter;
import com.m4g.task.DurableTaskIdentifierImpl;
import com.m4g.task.TaskIdentifier;
import com.m4g.task.TaskStatus;
import com.m4g.task.TaskKey;

public class DurableTaskController implements TaskController {

	private RunnableJobAdapter jobAdapter;
	private TaskIdentifier identifier;
	private SchedulerService service;
	private TaskKey taskKey;
	
	private DurableTaskController(){
	}
	
	public DurableTaskController(RunnableJobAdapter jobAdapter, SchedulerService service){
		this.jobAdapter = jobAdapter;
		this.service = service; 
		this.identifier = new DurableTaskIdentifierImpl(jobAdapter);
	}
	
	DurableTaskController(TaskKey taskKey, SchedulerService service ,Runnable runnable){
		this.taskKey = taskKey;
		this.service = service;
		this.identifier = new DurableTaskIdentifierImpl(taskKey, runnable);
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
		if (jobAdapter != null){
			return jobAdapter.getTaskStatus();
		}		
		return null;
				
	}
}
