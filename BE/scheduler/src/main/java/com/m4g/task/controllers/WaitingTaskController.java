package com.m4g.task.controllers;

import com.google.common.base.Preconditions;
import com.m4g.SchedulerService;
import com.m4g.job.adapters.RunnableJobAdapter;
import com.m4g.task.DurableTaskIdentifierImpl;
import com.m4g.task.TaskIdentifier;
import com.m4g.task.TaskKey;
import com.m4g.task.TaskStatus;
import com.m4g.task.entities.TaskInstanceEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author aharon_br
 * This taskController can encapsulate taskController <br>
 * with 1 or more TaskInstances with WAITING status. <br>
 * Please note that all its override methods will do nothing <br>
 * if there is no activeTask.     
 */
public class WaitingTaskController extends RunnableTaskController implements Serializable{

	private static final long serialVersionUID = -6808165092974199536L;
	
	private TaskInstanceEntity activeInstanceEntity;
	private TaskController activeTaskController;
	
	private SchedulerService service;
	private Runnable runnable;
	private TaskKey taskKey;
	
	private Map<Integer, TaskInstanceEntity> waitingTaskMap;
	
	public WaitingTaskController(RunnableJobAdapter jobAdapter, SchedulerService service) {
		super(jobAdapter, service);
		waitingTaskMap = new HashMap<Integer, TaskInstanceEntity>();
	}
	
	@Override
	protected void createIdentifier(TaskKey taskKey, Runnable runnable){
		identifier = new DurableTaskIdentifierImpl(taskKey, runnable);
	}
	
	public void setActiveEntity(TaskInstanceEntity instanceEntity, TaskKey taskKey, Runnable activeRunnable){
		Preconditions.checkNotNull(instanceEntity, "instanceEntity can not be null");
		activeInstanceEntity = instanceEntity;
		if (this.taskKey == null){
			this.taskKey = taskKey;
		}
		activeTaskController = new DurableTaskController(this.taskKey, this.service, activeRunnable);
		DurableTaskIdentifierImpl identifier = (DurableTaskIdentifierImpl) activeTaskController.getIdentifier();
		identifier.setTaskInstanceId(activeInstanceEntity.getTaskInstanceId());
		identifier.setTaskTypeId(activeInstanceEntity.getTaskTypeEntity().getTaskId());
		identifier.updateWaitingInstanceName(instanceEntity.getPhisycalName());
	}
	
	public void removeActiveTaskIncetanceEntity(){
		activeInstanceEntity = null;
		activeTaskController = null;
	}
	
	protected boolean isValidActiveInstanceEntity(){
		if (activeInstanceEntity == null || activeInstanceEntity.getTaskStatus() == TaskStatus.WAITING){
			return false;
		}
		return true;
	}
	
	/**
	 * pause current active task
	 * if no active TaskInstance exist this method will do nothing
	 */
	@Override
	public void pauseTask() {
		if (!isValidActiveInstanceEntity()){
			return;
		}
		service.pauseJob(activeTaskController.getIdentifier());
	}

	/**
	 * if no active TaskInstance exist this method will do nothing
	 */
	@Override
	public void resumeTask() {
		if (!isValidActiveInstanceEntity()){
			return;
		}
		service.resumeJob(activeTaskController.getIdentifier());
	}
	
	/**
	 * if no active TaskInstance exist this method will do nothing
	 */
	@Override
	public TaskStatus getTaskStatus() {
		if (!isValidActiveInstanceEntity()){
			return TaskStatus.WAITING;
		}
		return activeInstanceEntity.getTaskStatus();
	}

	public TaskInstanceEntity getActiveInstanceEntity() {
		return activeInstanceEntity;
	}

	public void setActiveInstanceEntity(TaskInstanceEntity activeInstanceEntity) {
		this.activeInstanceEntity = activeInstanceEntity;
	}

	/**
	 * if no active TaskInstance exist this method will do nothing
	 */
	@Override
	public TaskIdentifier getIdentifier() {
		if (activeTaskController == null){
			return identifier; // Identifier without ids
		}
		return activeTaskController.getIdentifier();
	}
	
	public void addWaitingTask(TaskInstanceEntity instnceEntity){
		Preconditions.checkNotNull(instnceEntity, "instnceEntity can not be null");
		Integer id = instnceEntity.getTaskInstanceId();
		Preconditions.checkNotNull(id, "taskInstanceId can not be null");
		
		waitingTaskMap.put(id, instnceEntity);
	}
	
	public void addWaitingTasks(List<TaskInstanceEntity> instnceEntityList){
		Preconditions.checkNotNull(instnceEntityList, "instnceEntity can not be null");
		
		for (TaskInstanceEntity instnceEntity: instnceEntityList){
			addWaitingTask(instnceEntity);
		}
	}
	
	public void removeWaitingTaskInstance(Integer taskInstanceId){
		Preconditions.checkNotNull(taskInstanceId, "taskInstanceId can not be null");
		waitingTaskMap.remove(taskInstanceId);
	}
	
	public boolean hasWaitingTaskInstace(Integer taskInstanceId){
		Preconditions.checkNotNull(taskInstanceId, "taskInstanceId can not be null");
		return waitingTaskMap.containsKey(taskInstanceId);
	}
	
}
