package com.m4g.quartz;

import com.m4g.ErrorSeverity;
import com.m4g.task.TaskKey;
import com.m4g.task.TaskStatus;
import com.m4g.task.entities.TaskErrorEntity;
import com.m4g.task.entities.TaskInstanceEntity;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

public class TaskFacadeImpl implements TaskFacade, Serializable{
	
	private static final long serialVersionUID = 1921541092843747556L;
//	private SchedulerService schedulerService;
	private SchedulerFacade schedulerFacade;
	
	public TaskFacadeImpl(){
		
	}

	public void setTaskStatus(Integer taskInstanceId, TaskStatus taskStatus) {
		schedulerFacade.setTaskStatus(taskInstanceId, taskStatus);
	}
	
	@Override
	public TaskInstanceEntity getWaitingTaskInstanceEntity(Integer taskId, List<Integer> idsList, String className) {
		return schedulerFacade.getWaitingTaskInstanceEntity(taskId, idsList, className);
	}
	
	@Override
	public TaskInstanceEntity getWaitingTaskInstanceEntity(String name, String  group){
		return schedulerFacade.getWaitingTaskInstanceEntity(name, group);
	}
	
	@Override
	public TaskInstanceEntity getWaitingTaskInstanceEntity(TaskKey taskKey) {
		return schedulerFacade.getWaitingTaskInstanceEntity(taskKey);
	}

	@Override
	public void setProgress(Integer taskInstanceId, Integer progress) {
		schedulerFacade.setProgress(taskInstanceId, progress);
	}

	@Override
	public int getProgress(Integer taskInstanceId){
		return schedulerFacade.getProgress(taskInstanceId);
	}

	@Override
	public void setAmount(Integer taskInstanceId, Integer amount) {
		schedulerFacade.setAmount(taskInstanceId, amount);
	}

	@Override
	public void setError(Integer taskInstanceId, String errorMessage) {
		schedulerFacade.setError(taskInstanceId, errorMessage);
	}

	@Override
	public void setError(Integer taskInstanceId, String errorMessage,
			String stackTrace, ErrorSeverity sevirity) {
		schedulerFacade.setError(taskInstanceId, errorMessage, stackTrace, sevirity);
	}

	@Override
	public void setAmount(Integer taskInstanceId, Integer amount, int total) {
		schedulerFacade.setAmount(taskInstanceId, amount, total);
	}

	@Override
	public void setMessage(Integer taskInstanceId, String message) {
		schedulerFacade.setMessage(taskInstanceId, message);
		
	}
	
	@Override
	public void setInstanceProperties(Integer taskInstanceId, Properties properties) {
		schedulerFacade.setInstanceProperties(taskInstanceId, properties);
		
	}

	@Override
	public void setTypeProperties(Integer taskInstanceId,
			Properties taskTypeProperties) {
		schedulerFacade.setTypeProperties(taskInstanceId, taskTypeProperties);
	}
	
	@Override
	public void setTaskCompleteStatus(Integer taskInstanceId) {
		schedulerFacade.setTaskCompleteStatus(taskInstanceId);
	}
	
	@Override
	public TaskInstanceEntity getTaskInstance(Integer taskInstanceId) {
		return schedulerFacade.getTaskInstance(taskInstanceId);
	}
	
	@Override
	public List<TaskErrorEntity> getTaskInstanceError(Integer taskInstanceId) {
		return schedulerFacade.getTaskInstanceError(taskInstanceId);
	}

	public SchedulerFacade getSchedulerFacade() {
		return schedulerFacade;
	}

	public void setSchedulerFacade(SchedulerFacade schedulerFacade) {
		this.schedulerFacade = schedulerFacade;
	}
	
	public TaskStatus getTaskInstanceEntityStatus(Integer id){
		return schedulerFacade.getTaskInstanceEntityStatus(id);
	}

	@Override
	public void pauseJob(TaskInstanceEntity taskInstanceEntity) {
		schedulerFacade.pauseJob(taskInstanceEntity);
	}

	@Override
	public void resumeJob(TaskInstanceEntity taskInstanceEntity) {
		schedulerFacade.resumeJob(taskInstanceEntity);
	}

	@Override
	public TaskInstanceEntity getCopyOfTaskInstanceEntity(Integer taskInstanceId) {
		return schedulerFacade.getCopyOfTaskInstanceEntity(taskInstanceId);
	}
	
	public Object getBean(String beanName){
		return schedulerFacade.getBean(beanName);
	}

	@Override
	public void setError(Integer taskInstanceId, String messageError,
			Throwable t, ErrorSeverity sevirity) {
		Writer stackTrace = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(stackTrace);
	    t.printStackTrace(printWriter);
	    setError(taskInstanceId, messageError, stackTrace.toString(), sevirity);
		
	}

	@Override
	public void setDescription(Integer taskInstanceId, String description) {		
		schedulerFacade.setDescription(taskInstanceId, description);
	}

	@Override
	public int getRetries(Integer taskInstanceId) {
		return schedulerFacade.getRetries(taskInstanceId);
	}

	@Override
	public void setRetries(Integer taskInstanceId, int retries) {
		schedulerFacade.setRetries(taskInstanceId,retries);
	}
	
}
