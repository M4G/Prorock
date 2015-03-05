package com.m4g.task.dao;

import com.coral.project.dao.AbstractDao;
import com.coral.project.entities.TaskTypeEntity;
import com.coral.project.facilities.scheduler.quartz.RunnableJobAdapter;
import com.coral.project.facilities.scheduler.quartz.TaskKey;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coral.project.facilities.scheduler.SchedulerConstants.FROM_TASK_TYPE_ENTITY_QUERY;
import static com.coral.project.facilities.scheduler.SchedulerConstants.FROM_TASK_TYPE_ENTITY_WHERE_NAME_AND_GROUP_QUERY;

@Repository("taskTypeDao")
public class TaskTypeDaoImpl extends AbstractDao<TaskTypeEntity, Integer>  implements TaskTypeDao{

	@Override
	public TaskTypeEntity getTaskTypeEntity(String name, String group) {
		String queryStr = FROM_TASK_TYPE_ENTITY_WHERE_NAME_AND_GROUP_QUERY;
		TaskTypeEntity taskTypeEntity = executeSingleResultQuery(queryStr, name , group);
		return taskTypeEntity;
	}

	@Override
	public TaskTypeEntity persistTaskTypeEntity(RunnableJobAdapter JobAdapter) {
		TaskTypeEntity taskEntity = new TaskTypeEntity(JobAdapter);  
		saveAndFlush(taskEntity);
		return taskEntity;
	}
	
	@Override
	public TaskTypeEntity persistTaskTypeEntity(Runnable runnable, TaskKey taskKey) {
		TaskTypeEntity taskEntity = new TaskTypeEntity(runnable, taskKey);  
		saveAndFlush(taskEntity);
		return taskEntity;
	}

	

	@Override
	public List<TaskTypeEntity> getAllTaskTypeEntities() {
		String queryStr = FROM_TASK_TYPE_ENTITY_QUERY;
		List<TaskTypeEntity> taskTypeEntitiesList = executeQuery(queryStr);
		return taskTypeEntitiesList;
	}
	
	public TaskTypeEntity findByTaskExcClass(String excClassFullName){
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("excClass", excClassFullName);
		return  executeSingleResultNamedQuery("TaskTypeEntity.findByTaskExcClass",parameters);
	}

	@Override
	public void setProperties(Integer taskId, String createClob) {
		TaskTypeEntity task = find(taskId);
		task.setParams(createClob);
		saveAndFlush(task);
	}

}
