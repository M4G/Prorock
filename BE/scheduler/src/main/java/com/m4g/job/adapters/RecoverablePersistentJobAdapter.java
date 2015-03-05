package com.m4g.job.adapters;

import com.m4g.task.TaskStatus;
import com.m4g.exceptions.JobInterruptedException;
import com.m4g.job.InteruptableJob;
import com.m4g.job.LifeCycleAware;
import com.m4g.task.controllers.WaitingTaskController;
import com.m4g.task.TaskKey;
import com.m4g.task.entities.TaskInstanceEntity;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.m4g.SchedulerConstants.*;

/**
 * 
 * @author aharon_br
 * This JobAdapter is use for handling recoverable JobAdapter registered <br>
 * with IntervalTimer. The RecoverablePersistentJobAdapter will fetch <br>
 * the suitable WAITING TaskInstance and will change it status <br>
 * and start using that TaskInstance  
 */
public class RecoverablePersistentJobAdapter extends RecoverableJobAdapter {
	
	protected WaitingTaskController taskWaitingcontroller;
	
	protected void setWaitingTaskControllerActive(JobExecutionContext context , TaskInstanceEntity entity, TaskKey taskKey,
			Runnable runnable){
		try {
			List<WaitingTaskController> waitingControllerslist =  
				(List<WaitingTaskController>) context.getScheduler().getContext().get(WAITING_TASKCONTROLLER_LIST_KEY);
			for (WaitingTaskController controller: waitingControllerslist){
				if (controller.hasWaitingTaskInstace(entity.getTaskInstanceId())){
					taskWaitingcontroller = controller;
					controller.setActiveEntity(entity,taskKey, runnable);
					break;
				}
			}
		} catch (SchedulerException e) {
			logger.error("Unable set waiting taskController list", e); 
		}
	}

	protected void deActiveTaskInstance(JobDataMap jobDataMap){
		if (taskWaitingcontroller != null){
			taskWaitingcontroller.setActiveInstanceEntity(null);
			taskWaitingcontroller = null;
			jobDataMap.put(TASK_INSTANCE_ID, null);
		}
	}
	
	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		setTaskFacade(context); 
		//1. retrieve from db some taskInstanceEntity with status waiting
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		TaskKey taskKey = (TaskKey) jobDataMap.get(TASKKEY_KEY);
		Integer taskId = (Integer) jobDataMap.get(TASK_ID_KEY);
		List<Integer> IdsList =  (List<Integer>) jobDataMap.get(TASK_INSTANCES_IDS_LIST_KEY);
		Runnable newRunnable = (Runnable) jobDataMap.get(RUNNABLE_KEY);
		TaskInstanceEntity taskInstanceEntity = 
			taskFacade.getWaitingTaskInstanceEntity(taskId, IdsList, newRunnable.getClass().getName());
		Integer taskInstanceId = taskInstanceEntity.getTaskInstanceId();
		jobDataMap.put(TASK_INSTANCE_ID, taskInstanceId);
		
		//2. associate with existing waitingTaskController, so that taskConreoller
		// that was return in SchedulerService will be active
		setWaitingTaskControllerActive(context, taskInstanceEntity, taskKey, newRunnable);
		setRunnable(context);
		setTaskContext(context, runnable, taskFacade, taskInstanceId);
		setCancelByTaskInstanceEntityId(taskInstanceId);
		try {
			if (newRunnable instanceof LifeCycleAware){
				if (cancel){
					setTaskStatus(jobDataMap, TaskStatus.CANCELED);
					if (runnable instanceof InteruptableJob){
						( (InteruptableJob) runnable).setCancel(true);
					}
				}else{
					setTaskStatus(jobDataMap, TaskStatus.STARTED);
					beforeExecution();
					setTaskStatus(jobDataMap, TaskStatus.RUNNING);
					execute();
					afterExecution();
					setTaskStatus(jobDataMap, TaskStatus.COMPLETED);
					jobDataMap.remove(TASK_INSTANCE_ID);
				}
			}else{
				if (cancel){
					setTaskStatus(jobDataMap, TaskStatus.CANCELED);
					if (runnable instanceof InteruptableJob){
						( (InteruptableJob) runnable).setCancel(true);
					}
				}else{
					setTaskStatus(jobDataMap, TaskStatus.RUNNING);
					execute();
					setTaskStatus(jobDataMap, TaskStatus.COMPLETED);
					jobDataMap.remove(TASK_INSTANCE_ID);
				}
			}			
		} catch (JobInterruptedException e){
			if (runnable != null){
				logger.warn(JOB_CANCELD_BY_USER_RUNNABLE + runnable.getClass().getName(), e);
			}else{
				logger.warn(JOB_CANCELD_BY_USER, e);
			}
			setTaskStatus(jobDataMap, TaskStatus.CANCELED_BY_USER);
		}
		catch (Exception e) {
			if (runnable != null){
				logger.error(EXCEPTION_HAS_OCCURS_WHILE_JOB_EXECUTING + runnable.getClass().getName(), e);
			}else{
				logger.error(EXCEPTION_WHILE_EXECUTING, e);
			}
			setTaskStatus(jobDataMap, TaskStatus.ERROR);
		}finally{
			deActiveTaskInstance(jobDataMap);
		}
	}
	
}
