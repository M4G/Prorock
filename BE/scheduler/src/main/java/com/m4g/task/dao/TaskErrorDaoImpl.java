package com.m4g.task.dao;

import com.coral.project.dao.AbstractDao;
import com.coral.project.entities.TaskErrorEntity;
import com.coral.project.entities.TaskInstanceEntity;
import com.coral.project.facilities.scheduler.ErrorSeverity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("taskErrorDao")
public class TaskErrorDaoImpl extends AbstractDao<TaskErrorEntity, Integer>  implements TaskErrorDao {

	@Override
	public void addError(TaskInstanceEntity taskInstance, String message,
			String stackTrace, ErrorSeverity severity) {
		
		Integer taskInstanceId = taskInstance.getTaskInstanceId();
		TaskErrorEntity  taskErrorEntity = new TaskErrorEntity();
		taskErrorEntity.setTaskInstanceEntity(taskInstance);
		taskErrorEntity.setTaskInstanceId(taskInstanceId);
		taskErrorEntity.setMessage(message);
		taskErrorEntity.setStackTrace(stackTrace);
		taskErrorEntity.setSeverity(severity);
		try {
			saveAndFlush(taskErrorEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addError(TaskInstanceEntity taskInstance, String message) {
		Integer taskInstanceId = taskInstance.getTaskInstanceId();
		TaskErrorEntity  taskErrorEntity = new TaskErrorEntity();
		taskErrorEntity.setTaskInstanceEntity(taskInstance);
		taskErrorEntity.setTaskInstanceId(taskInstanceId);
		taskErrorEntity.setMessage(message);
		save(taskErrorEntity);
	}

	@Override
	public List<TaskErrorEntity> getTaskErrors(Integer taskInstanceId) {
		String queryString = "FROM TaskErrorEntity where taskInstanceId = ?";
		List<TaskErrorEntity> taskErrorList = executeQuery(queryString, taskInstanceId);
		return taskErrorList;
	}

}
