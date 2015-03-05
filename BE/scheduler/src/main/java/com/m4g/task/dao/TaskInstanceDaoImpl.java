package com.m4g.task.dao;

import com.coral.project.dao.AbstractDao;
import com.coral.project.entities.TaskInstanceEntity;
import com.coral.project.facilities.scheduler.TaskStatus;
import com.coral.project.facilities.scheduler.quartz.RunnableJobAdapter;
import com.coral.project.facilities.utils.TasksUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coral.project.facilities.scheduler.SchedulerConstants.*;

@Repository("taskInstanceDao")

public class  TaskInstanceDaoImpl extends AbstractDao<TaskInstanceEntity, Integer>  implements TaskInstanceDao{
	
	@Override
	public void deleteTaskInstance(Integer taskInstanceId) {
		String sqlString = SQL_DELETE_QUERY + taskInstanceId;
		Query query =  getSession().createSQLQuery(sqlString);
		query.executeUpdate();
	}

	@Override
	public TaskInstanceEntity getTaskInstance(Integer taskInstanceId) {
			String queryString = FROM_TASK_INSTANCE_ENTITY_WHERE_TASK_INSTANCE_ID_QUERY;
			TaskInstanceEntity taskInstanceEntity = executeSingleResultQuery(queryString, taskInstanceId);
			return taskInstanceEntity;
	}

	@Override
	public List<TaskInstanceEntity> getTaskInstanceByStatus(TaskStatus status) {
		String queryString = FROM_TASK_INSTANCE_ENTITY_WHERE_STATUS_QUERY;
		List<TaskInstanceEntity> taskInstancesList = executeQuery(queryString, status);
		return taskInstancesList;
	}

	@Override
	public List<TaskInstanceEntity> getTaskInstanceByStatusAndGroup(
			TaskStatus status, String groupName) {
		String queryString = FROM_TASK_INSTANCE_ENTITY_WHERE_STATUS_AND_GROUP_QUERY;
		List<TaskInstanceEntity> taskInstancesList = executeQuery(queryString, status, groupName);
		return taskInstancesList;
	}

	@Override
	public void setTaskStatus(Integer taskInstanceId, TaskStatus newTaskStatus) {
		TaskInstanceEntity entity = getTaskInstance(taskInstanceId);
		if (entity.getStartDate() == null){
			TaskStatus currentStatus = entity.getTaskStatus();
			if (currentStatus == TaskStatus.STARTED || currentStatus == TaskStatus.EXECUTING){
				entity.setStartDate(new Date());
			}
		}
		TaskStatus entityTaskStatus = TaskStatus.getTaskStatus(entity.getStatusCD()); 
		if (RunnableJobAdapter.isChangeStatusValid(entityTaskStatus, newTaskStatus));
		entity.setTaskStatus(newTaskStatus);
		if (newTaskStatus == TaskStatus.COMPLETE || newTaskStatus == TaskStatus.COMPLETE_WITH_PROBLEM || 
				newTaskStatus == TaskStatus.COMPLETE_WITH_WARNING ){
			entity.setEndDate(new Date());
		}
		saveAndFlush(entity);
	}
	
	@Override
	public TaskInstanceEntity getWaitingTaskInstanceEntity(String name, String  group){
		String queryString = FROM_TASK_INSTANCE_ENTITY_WHERE_NAME_AND_GROUP_AND_STATUS_QUERY;
		List<TaskInstanceEntity> taskInstancesList = executeQuery(queryString, name, group, "WAITING");
		return taskInstancesList.get(0);
	}
	
	@Override
	public TaskInstanceEntity getWaitingTaskInstanceEntity(Integer taskId, List<Integer> idsList, String className){
		String queryString = FROM_TASK_INSTANCE_ENTITY_WHERE_TASK_ID_AND_STATUS_QUERY;
		List<TaskInstanceEntity> taskInstancesList = executeQuery(queryString, taskId, "WAITING");
		for (TaskInstanceEntity entity: taskInstancesList){
			if (idsList.contains(entity.getTaskInstanceId())){
				entity.setTaskStatus(TaskStatus.PENDING);
				// now when we have got pending taskInstanceEntity 
				// we should change it's time oriented name
				String phisycalName = TasksUtils.formatClassName(className);
				phisycalName = TasksUtils.createTimeOrientedUniquName(phisycalName);
				entity.setPhisycalName(phisycalName);
				saveAndFlush(entity);
				return entity;
			}
		}
		return null;
	}

	@Override
	public void setProgress(Integer taskInstanceId, Integer progress) {
		TaskInstanceEntity taskInstanceEntity = getTaskInstance(taskInstanceId);
		taskInstanceEntity.setProgress(progress);
		saveAndFlush(taskInstanceEntity);
	}
	
	@Override
	public void setDescription(Integer taskInstanceId, String description) {
		TaskInstanceEntity taskInstanceEntity = getTaskInstance(taskInstanceId);
		taskInstanceEntity.setDescription(description);
		saveAndFlush(taskInstanceEntity);
	}

	@Override
	public void setAmount(Integer taskInstanceId, Integer amount) {
		TaskInstanceEntity taskInstanceEntity = getTaskInstance(taskInstanceId);
		taskInstanceEntity.setAmount(amount);
		saveAndFlush(taskInstanceEntity);
	}

	@Override
	public void setAmount(Integer taskInstanceId, Integer amount, int total) {
		setAmount(taskInstanceId, amount);
		int progress = Math.round(( amount / total ) * 100); 
		setProgress(taskInstanceId, progress);
	}

	@Override
	public void setMessage(Integer taskInstanceId, String message) {
		TaskInstanceEntity taskInstanceEntity = getTaskInstance(taskInstanceId);
		taskInstanceEntity.setMessage(message);
		saveAndFlush(taskInstanceEntity);
	}
	
	@Override
	public TaskStatus getTaskInstanceEntityStatus(Integer taskInstanceId){
		TaskInstanceEntity entity = getTaskInstance(taskInstanceId);
		TaskStatus taskStatus = TaskStatus.getTaskStatus(entity.getStatusCD());  
		return taskStatus;
	}

	@Override
	public TaskInstanceEntity getCopyOfTaskInstanceEntity(Integer taskInstanceId) {
		TaskInstanceEntity originalEntity = getTaskInstance(taskInstanceId);		
		TaskInstanceEntity newEntity = originalEntity.cloneTaskInstanceEntity();
		newEntity.setStatusCD(TaskStatus.PENDING.getValue());
		save(newEntity);
		return newEntity;
	}

	@Override
	public TaskStatus getTaskInstanceEntityStatus(String phisycalName) {
		try {
			String queryString = FROM_TASK_INSTANCE_ENTITY_WHERE_PHISYCAL_NAME;
			TaskInstanceEntity taskInstanceEntity = executeSingleResultQuery(queryString, phisycalName);
			if (taskInstanceEntity != null){
				return taskInstanceEntity.getTaskStatus();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void setProperties(Integer taskInstanceId, String createClob) {
		TaskInstanceEntity taskInstanceEntity = getTaskInstance(taskInstanceId);
		taskInstanceEntity.setParams(createClob);
		saveAndFlush(taskInstanceEntity);
	}

	@Override
	public int getProgress(Integer taskInstanceId) {
		TaskInstanceEntity taskInstanceEntity = getTaskInstance(taskInstanceId);
		return taskInstanceEntity.getProgress();
	}

	@Override
	public int getMaxTaskId() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		return executeMaxQuery("TaskInstanceEntity.getMaxTaskId", parameters);
	}
}
