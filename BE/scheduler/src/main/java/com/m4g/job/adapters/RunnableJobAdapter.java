package com.m4g.job.adapters;

import com.m4g.ErrorSeverity;
import com.m4g.task.SimpleTaskContextImpl;
import com.m4g.task.TaskContext;
import com.m4g.quartz.TaskFacade;
import com.m4g.task.TaskStatus;
import com.m4g.exceptions.JobInterruptedException;
import com.m4g.job.InteruptableJob;
import com.m4g.job.LifeCycleAware;
import com.m4g.task.TaskContextAware;
import com.m4g.task.TaskKey;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.Properties;

import static com.m4g.SchedulerConstants.*;

/**
 * 
 * @author aharon_br
 * 
 *         Please note that the JobAdapter delegate a runnable <br>
 *         (any kind of runnable)
 * 
 */
public abstract class RunnableJobAdapter extends JobAdapter {

	protected String originalExecClassName;
	protected String description;
	protected Runnable runnable;
	protected TaskKey taskKey;
	protected TaskStatus taskStatus;
	protected TaskFacade taskFacade;
	Properties parameters;
	protected boolean cancel = false;
	protected ApplicationContext applicationContext;

	protected static final Logger logger = Logger
			.getLogger(RunnableJobAdapter.class);

	public RunnableJobAdapter() {
		parameters = new Properties();
	}
	
	protected void setTaskContext(JobExecutionContext context,
			Runnable runnable, TaskFacade taskFacade, Integer taskInstaceId) {
		if (runnable instanceof TaskContextAware) {
			/*
			if (((TaskContextAware) runnable).hasTaskContext()) {
				return;
			}
			*/
			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

			Properties taskTypeParams = (Properties) jobDataMap
					.get(TASK_TYPE_PARAMS_KEY);
			Properties taskInstaceParams = (Properties) jobDataMap
					.get(TASK_INSTANCE_PARAMS_KEY);

			TaskContext taskContext = new SimpleTaskContextImpl(taskFacade,
					taskTypeParams, taskInstaceParams, taskInstaceId);
			taskContext.setTaskFacade(taskFacade);
			((TaskContextAware) runnable).setTaskContext(taskContext);
		}
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	public void execute() throws JobInterruptedException {
		runnable.run();
	}

	protected void beforeExecution() {
		((LifeCycleAware) runnable).beforeExecution();
	}

	protected void afterExecution() {
		((LifeCycleAware) runnable).afterExecution();
	}

	protected abstract void setTaskStatus(JobDataMap jobDataMap,
			TaskStatus taskStatus);

	protected void setCompleteStatus(Integer taskInstanceId) {
		taskFacade.setTaskCompleteStatus(taskInstanceId);
	}

	protected void setTaskFatalError(Integer taskInstanceId, String message,
			Exception e) {
		taskFacade.setError(taskInstanceId, message, e.getMessage(), ErrorSeverity.FATAL_ERROR);
	}

	protected void setCancelByTaskInstanceEntityId(Integer id) {
		TaskStatus status = taskFacade.getTaskInstanceEntityStatus(id);
		if (status == TaskStatus.CANCELED
				|| status == TaskStatus.CANCELED_BY_USER) {
			setCancel(true);
		}
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		setRunnable(context);
		try {
			applicationContext = (ApplicationContext) context.getScheduler()
					.getContext().get(APPLICATION_CONTEXT_KEY);
		
			if (runnable instanceof ApplicationContextAware) {
				((ApplicationContextAware)runnable).setApplicationContext(applicationContext);
			}
		} catch (SchedulerException ex) {
			throw new JobExecutionException("Failed to extract Spring application context.", ex);
		}
		
		Integer taskInstanceId = getTaskInsanceId(jobDataMap);
		setTaskContext(context, runnable, taskFacade, taskInstanceId);
		setCancelByTaskInstanceEntityId(taskInstanceId);
		// create new TasskEntity with the same params of the taskInstanceId
		// taskInstanceEntity
		try {
			if (runnable instanceof LifeCycleAware) {
				if (cancel) {
					setTaskStatus(jobDataMap, TaskStatus.CANCELED);
					if (runnable instanceof InteruptableJob) {
						((InteruptableJob) runnable).setCancel(true);
					}
				} else {
					setTaskStatus(jobDataMap, TaskStatus.STARTED);
					beforeExecution();
					setTaskStatus(jobDataMap, TaskStatus.RUNNING);
					execute();
					setTaskStatus(jobDataMap, TaskStatus.AFTER_RUN);
					afterExecution();
					setCompleteStatus(taskInstanceId);
				}
			} else {
				if (cancel) {
					setTaskStatus(jobDataMap, TaskStatus.CANCELED);
					if (runnable instanceof InteruptableJob) {
						((InteruptableJob) runnable).setCancel(true);
					}
				} else {
					setTaskStatus(jobDataMap, TaskStatus.RUNNING);
					execute();
					setCompleteStatus(taskInstanceId);
				}
			}
		} catch (JobInterruptedException e) {
			cancel = true;
			if (runnable != null) {
				logger.warn(JOB_CANCELD_BY_USER_RUNNABLE
						+ runnable.getClass().getName(), e);
			} else {
				logger.warn(JOB_CANCELD_BY_USER, e);
			}
			setTaskStatus(jobDataMap, TaskStatus.CANCELED_BY_USER);
		} catch (Exception e) {
			if (runnable != null) {
				logger.error(EXCEPTION_HAS_OCCURS_WHILE_JOB_EXECUTING
						+ runnable.getClass().getName(), e);

			} else {
				logger.error(EXCEPTION_WHILE_EXECUTING, e);
			}
			setTaskStatus(jobDataMap, TaskStatus.ERROR);
			setTaskFatalError(taskInstanceId, "EXCEPTION_HAS_OCCURS_WHILE_JOB_EXECUTING" + runnable.getClass().getName(), e);
		}
	}

	// This method retrieve the runnable object from jobDataMap and set it
	// to the new created task instance (created by Quartz engine)
	protected void setRunnable(JobExecutionContext context) {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Runnable runnable = (Runnable) dataMap
				.get(RUNNABLE_KEY);
		setRunnable(runnable);
	}

	protected void setTaskFacade(JobExecutionContext context) {
		ApplicationContext applicationContext;
		try {
			applicationContext = (ApplicationContext) context.getScheduler()
					.getContext().get(APPLICATION_CONTEXT_KEY);
			taskFacade = (TaskFacade) applicationContext
					.getBean(TASK_FACADE_BEAN);
		} catch (SchedulerException e) {
			logger.error("Unable get TaskFacade", e);
		}
	}

	public String getName() {
		return taskKey.getName();
	}

	public String getGroup() {
		return taskKey.getGroup();
	}

	public void setTaskKey(TaskKey taskKey) {
		this.taskKey = taskKey;
	}

	public TaskKey getTaskKey() {
		return taskKey;
	}

	public String getUser() {
		return taskKey.getUser();
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public Object getParameter(String parameterName) {
		Assert.notNull(parameterName, "parameterName cannot be null");
		return parameters.get(parameterName);
	}

	public void setParameter(String parameterName, Object value) {
		Assert.notNull(parameterName, "parameterName cannot be null");
		parameters.put(parameterName, value);
	}

	public Iterator<String> getParametersNames() {
		if (parameters != null) {
			return parameters.stringPropertyNames().iterator();
		}
		return null;
	}

	/**
	 * This method check if TaskInstanceId is authorize <br>
	 * to change it state. For example TaskInstanceId that during execution
	 * change its status to COMPLETED_WITH_PROBLEM cannot change it status.
	 */
	public static boolean isChangeStatusValid(TaskStatus existingTaskStatus,
			TaskStatus newTaskStatus) {
		if (existingTaskStatus == TaskStatus.COMPLETED_WITH_PROBLEM) {
			return false;
		} else if (existingTaskStatus == TaskStatus.COMPLETED_WITH_WARNING) {
			if (newTaskStatus == TaskStatus.COMPLETED_WITH_PROBLEM) {
				return true;
			}
			return false;
		}
		return true;
	}

	protected Runnable getRunnable(JobExecutionContext context) {
		JobDataMap map = context.getJobDetail().getJobDataMap();
		Runnable runnable = (Runnable) map.get(RUNNABLE_KEY);
		return runnable;
	}

	protected Integer getTaskInsanceId(JobDataMap jobDataMap) {
		return (Integer) jobDataMap.get(TASK_INSTANCE_ID);
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		if (!this.cancel) {
			this.cancel = cancel;
		}
	}

	public String getOriginalExecClassName() {
		return originalExecClassName;
	}

	public void setOriginalExecClassName(String originalExecClassName) {
		this.originalExecClassName = originalExecClassName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}	
}
