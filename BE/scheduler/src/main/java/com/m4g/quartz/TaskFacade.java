package com.m4g.quartz;

import com.m4g.ErrorSeverity;
import com.m4g.task.TaskKey;
import com.m4g.task.TaskStatus;
import com.m4g.task.entities.TaskErrorEntity;
import com.m4g.task.entities.TaskInstanceEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;

@Transactional
public interface TaskFacade{
	
	/**
	 * @param taskInstanceId the id of the taskInstance it sate should be change
	 * @param taskStatus the new status of the taskInstance
	 * 
	 * This method set the taskInstanceId status
	 */
	public void setTaskStatus(Integer taskInstanceId, TaskStatus taskStatus);
	
	/**
	 * @param taskInstanceId the id of the taskInstance it sate should be change
	 * This method set the taskInstanceId status COMPLETED or <br>
	 * COMPLETED_WITH_WARNING if taskInstance has warning(s) or <br>
	 * COMPLETED_WITH_PROBLEM if taskInstance has errors
	 */
	public void setTaskCompleteStatus(Integer taskInstanceId);
	
	/**
	 * @param name The name of the task (TaskType)
	 * @param group The group of the task (TaskType)
	 * @return TaskInstance with WAITING status
	 * 
	 * This method return WAITING taskInstance.  
	 */
	public TaskInstanceEntity getWaitingTaskInstanceEntity(String name, String group);
	
	/**
	 * @param taskKey the taskKey of the task (TaskType)
	 * @return TaskInstance with WAITING status
	 * 
	 * This method return WAITING taskInstance.
	 */
	public TaskInstanceEntity getWaitingTaskInstanceEntity(TaskKey taskKey);

	/**
	 * @param taskId the id (pk) of the task (TaskType) which <br>
	 * @param idsList list of WAITNING id's that one of them should be return 
	 * its WAITING taskInstance we want to return; 
	 * @return
	 */
	public TaskInstanceEntity getWaitingTaskInstanceEntity(Integer taskId, List<Integer> idsList, String className);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to set its progress.
	 * @param progress percentage of progress (0-100)
	 * 
	 * This method set the progress of taskInstance
	 */
	public void setProgress(Integer taskInstanceId, Integer progress);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to set its message.
	 * @param message message of taskInstanceId.
	 * 
	 *  This method set the progress of taskInstance
	 */
	public void setMessage(Integer taskInstanceId, String message);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to set its amount 
	 * @param amount the amount some quantity of task (units) completion
	 * 
	 * This method set the amount of taskInstance
	 */
	public void setAmount(Integer taskInstanceId, Integer amount);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to set its amount
	 * @param amount the amount some quantity of task (units) completion
	 * @param total the total number of task completion (units)
	 */
	public void setAmount(Integer taskInstanceId, Integer amount, int total);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to create an error entity
	 * @param messageError message describing the error of task
	 * 
	 * This method creates TaskErrorEntity associated with taskInstance
	 */
	public void setError(Integer taskInstanceId, String messageError);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to create an error entity
	 * @param messageError message describing the error of task
	 * @param stackTrace Error (Exception) stackTrace
	 * @param Sevirity the severity of the error.
	 */
	public void setError(Integer taskInstanceId, String messageError, String stackTrace, ErrorSeverity Sevirity);
	
	/**
	 * @param taskInstanceId the id of the taskInstance we want to create an error entity
	 * @param messageError message describing the error of task
	 * @param t   Throwable.
	 * @param Sevirity the severity of the error.
	 */
	public void setError(Integer taskInstanceId, String messageError, Throwable t, ErrorSeverity Sevirity);

	
	/**
	 * @param taskInstanceId the id (pk) of the TaskInstanceEntity we want to resolve
	 * @return TaskInstanceEntity
	 * 
	 * This method retrieve TaskInstanceEntity using taskInstanceId.
	 */
	public TaskInstanceEntity getTaskInstance(Integer taskInstanceId);
	
	/**
	 * @param taskInstanceId id (pk) of the TaskInstanceEntity its taskError we want to retrieve
	 * @return list of all TaskError associated with specific TaskInstanceEntity using its taskInstanceId     
	 * 
	 * The method return list of all TaskError associated with specific TaskInstanceEntity using its taskInstanceId
	 */
	public List<TaskErrorEntity> getTaskInstanceError(Integer taskInstanceId);

	/**
	 * 
	 * @param id The id (pk) of the TaskInstanceEntity
	 * @return the status of the TaskInstanceEntity with the id
	 */
	public TaskStatus getTaskInstanceEntityStatus(Integer id);
	
	/**
	 * 
	 * @param taskInstanceEntity the taskInstanceEntity we want to  pause 
	 */
	public void pauseJob(TaskInstanceEntity taskInstanceEntity);
	
	/**
	 * 
	 * @param taskInstanceEntity the taskInstanceEntity we want to resume
	 */
	public void resumeJob(TaskInstanceEntity taskInstanceEntity);
	
	/**
	 * 
	 * @param taskInstanceId the id of the original TaskInstanceEntity.
	 * @return new copy of TaskInstanceEntity same as the original taskInstanceEntity <br>
	 * but different (PK) id
	 * 
	 * This method create new copy of taskInstanceEntity same as the original taskInstanceEntity <br>
	 */
	public TaskInstanceEntity getCopyOfTaskInstanceEntity(Integer taskInstanceId);
	

	/**
	 * 
	 * @param beanName
	 * @return Bean from applicationContext connected with schedulerService
	 */
	public Object getBean(String beanName);

	public void setDescription(Integer taskInstanceId, String description);

	void setInstanceProperties(Integer taskInstanceId, Properties properties);

	public int getProgress(Integer taskInstanceId);

	public void setTypeProperties(Integer taskInstanceId,
                                  Properties taskInstanceProperties);

	public int getRetries(Integer taskInstanceId);

	public void setRetries(Integer taskInstanceId, int retries);
}
