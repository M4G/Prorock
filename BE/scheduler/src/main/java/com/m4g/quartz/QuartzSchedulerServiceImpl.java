package com.m4g.quartz;


import com.google.common.base.Preconditions;
import com.m4g.*;
import com.m4g.job.adapters.*;
import com.m4g.job.CriticalJob;
import com.m4g.job.NoCuncurrentJob;
import com.coral.project.facilities.utils.ClobUtils;
import com.coral.utils.spring.PostInitializationListener;
import com.m4g.task.*;
import com.m4g.task.controllers.DurableTaskController;
import com.m4g.task.controllers.TaskController;
import com.m4g.task.controllers.WaitingTaskController;
import com.m4g.task.dao.TaskErrorDao;
import com.m4g.task.dao.TaskInstanceDao;
import com.m4g.task.dao.TaskTypeDao;
import com.m4g.task.entities.TaskInstanceEntity;
import com.m4g.task.entities.TaskTypeEntity;
import com.m4g.task.entities.TaskErrorEntity;
import com.m4g.timers.DateTimer;
import com.m4g.timers.IntervalTimer;
import com.m4g.timers.MissedScheduledPolicy;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

import static com.m4g.SchedulerConstants.*;
import static com.m4g.task.TaskStatus.*;

@Service("schedulerService")
@Transactional
public class QuartzSchedulerServiceImpl implements SchedulerService,
		PostInitializationListener, ApplicationContextAware, SchedulerFacade,  Serializable {

	private static final long serialVersionUID = -5759309097754323789L;

	private static final Logger logger = Logger.getLogger(QuartzSchedulerServiceImpl.class);

	
	@Autowired
	private TaskTypeDao taskTypeDao;
	
	@Autowired
	private TaskInstanceDao taskInstanceDao;
	
	@Autowired
	private TaskErrorDao taskErrorDao;
	
	private SchedulerFactory schedulerFactory;
	private Scheduler scheduler;
	private boolean recoveryMode;
	
	private transient ApplicationContext applicationContext;

	// using ThreadSafe map.
	private Hashtable<String, TaskTypeEntity> taskTypesMap;
	
	public void init() {
		try {
			taskTypesMap = new Hashtable<String, TaskTypeEntity>();
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();

			// We place the Spring application context inside the scheduler's
			// context so jobs can access it as well.
			scheduler.getContext().put(APPLICATION_CONTEXT_KEY, applicationContext);
//			initDaoBeans(applicationContext);
			removeAllScheduledJobs();
			initMapFromDB();
			//taskResolverService.setSchedulerService(this);
			//taskResolverService.registerReloadTableJob();			
		} catch (SchedulerException e) {
			scheduler = null;
			logger.error("Unable to init scheduler", e);
		}
	}



	 
   // returns the class (without the package if any)
    private String getClassName(String className) {
        int firstChar = className.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            className = className.substring(firstChar);
        }

        return className.replaceFirst(String.valueOf(className.charAt(0)), String.valueOf(Character.toLowerCase(className.charAt(0))));
    }
	
	private void setTimerExecutionNumber(java.util.Timer timer, Trigger trigger){
		IntervalTimer simpleTimer = (IntervalTimer) timer;
		SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) trigger;
		if (simpleTrigger.getEndTime() == null || simpleTrigger.getEndTime() == null) {
			simpleTimer.setNumberOfExecution(simpleTrigger.getRepeatCount() + 1); // <AHARON> should check this with Rachel
		}else{
			Date startTime = simpleTimer.getStartTimeAsDate();
			Date endTime = simpleTimer.getEndTimeAsDate();
			simpleTimer.setNumberOfExecution(simpleTrigger.computeNumTimesFiredBetween(startTime, endTime));
		}
	}
	
	private void addParametersToInstance(TaskInstanceEntity instanceEntity, Map<String, Object> runningParams){
		for (String name: runningParams.keySet()){
			Object value = runningParams.get(name);
			instanceEntity.addParameter(name, value);
		}
	}
	
	private TaskInstanceEntity createWaitingTaskInstance(TaskIdentifier taskIdentifier, RunnableJobAdapter jobAdapter) {
		TaskInstanceEntity instanceEntity = new TaskInstanceEntity();
		instanceEntity.setPhisycalName(taskIdentifier.getTaskInstanceName());
		instanceEntity.setTaskStatus(WAITING);
		instanceEntity.setUser(taskIdentifier.getUser());
		instanceEntity.setDescription(jobAdapter.getDescription());
		for (Iterator<String> itr = jobAdapter.getParametersNames(); itr
				.hasNext();) {
			String key = itr.next();
			Object value = jobAdapter.getParameter(key);
			instanceEntity.addParameter(key, value);
		}
		return instanceEntity;
	}
	

	private TaskInstanceEntity createTaskInstance(TaskIdentifier taskIdentifier, RunnableJobAdapter jobAdapter) {
		TaskInstanceEntity instanceEntity = new TaskInstanceEntity();
		instanceEntity.setPhisycalName(taskIdentifier.getTaskInstanceName());
		instanceEntity.setStartDate(new Date());
		instanceEntity.setTaskStatus(PENDING);
		instanceEntity.setUser(taskIdentifier.getUser());
		instanceEntity.setDescription(jobAdapter.getDescription());
		for (Iterator<String> itr = jobAdapter.getParametersNames(); itr
				.hasNext();) {
			String key = itr.next();
			Object value = jobAdapter.getParameter(key);
			instanceEntity.addParameter(key, value);
		}
		return instanceEntity;
	}
	
	private String createTaskNameForStorage(RunnableJobAdapter jobAdapter) {
		//String className = jobAdapter.getRunnable().getClass().getName();
		String className = jobAdapter.getOriginalExecClassName();
		TaskKey taskKey = jobAdapter.getTaskKey();
		return createTaskNameForStorage(className, taskKey.getName(), taskKey.getGroup());
	}

	private String createTaskNameForStorage(String className, String name, String group) {
		String clazzName = TasksUtils.formatClassName(className);
		StringBuilder stringBuilder = new StringBuilder(clazzName);
		stringBuilder.append(":");
		stringBuilder.append(name);
		stringBuilder.append(":");
		stringBuilder.append(group);

		return stringBuilder.toString();
	}
	
	protected boolean checkIfExistTaskType(RunnableJobAdapter jobAdapter) {
		Preconditions.checkNotNull(jobAdapter, "jobAdapter cannot be null");
		String taskIdent = createTaskNameForStorage(jobAdapter);
		return taskTypesMap.containsKey(taskIdent);
	}
	
	protected void putTaskWithEntity(RunnableJobAdapter jobAdapter,
			TaskTypeEntity taskEntity) {
		Preconditions.checkNotNull(jobAdapter, "jobAdapter cannot be null");
		Preconditions.checkNotNull(taskEntity, "taskEntity cannot be null");

		String taskIdent = createTaskNameForStorage(jobAdapter);
		taskTypesMap.put(taskIdent, taskEntity);
	}

	protected void initMapFromDB() {
		List<TaskTypeEntity> taskTypeEntitiesList = null;
		try {
			taskTypeEntitiesList = taskTypeDao.getAllTaskTypeEntities();
			for (TaskTypeEntity entity : taskTypeEntitiesList) {
				String taskIdent = createTaskNameForStorage(
						entity.getExecutionClass(), entity.getName(),
						entity.getGroup());
				taskTypesMap.put(taskIdent, entity);
			}
		} catch (Exception e) {
			logger.error("Unable to init scheduler, Unable to fetch Tasks types from DB", e);
		}
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	protected WaitingTaskController createWaitingTaskController(RunnableJobAdapter jobAdapter) {
		Preconditions.checkNotNull(jobAdapter, "taskKey cannot be null");
		WaitingTaskController taskController = new WaitingTaskController(jobAdapter, this);
		return taskController;
	}
	
	protected TaskController createTaskController(RunnableJobAdapter jobAdapter) {
		Preconditions.checkNotNull(jobAdapter, "jobAdapter cannot be null");
		TaskController taskController = new DurableTaskController(jobAdapter, this);
		
		return taskController;
	}

	private void storeInstanceId(JobDetail jobDetail, Integer taskInstanceId) {
		jobDetail.getJobDataMap().put(TASK_INSTANCE_ID,	taskInstanceId);
	}
	
	private void storeRunnable(JobDetail jobDetail, Runnable runnable) {
		jobDetail.getJobDataMap().put(RUNNABLE_KEY,	runnable);
	}
	
	
	private void storeTaskInstancesList(JobDetail  jobDetail, List<TaskInstanceEntity> taskInstancesIdsList){
		List<Integer> idsList = new ArrayList<Integer>();  
		for (TaskInstanceEntity entity: taskInstancesIdsList){
			idsList.add(entity.getTaskInstanceId());
		}
		jobDetail.getJobDataMap().put(TASK_INSTANCES_IDS_LIST_KEY, idsList);
	}
	
	// This method return the associated TaskTypeEntity for the task
	// If TaskTypeEntityExist in taskMap it return it else
	// The method create TaskType and persist it and store it in map.
	private TaskTypeEntity getTaskTaskType(RunnableJobAdapter jobAdapter) {
		TaskTypeEntity taskEntity = null;
		if (!checkIfExistTaskType(jobAdapter)) {
			taskEntity = taskTypeDao.persistTaskTypeEntity(jobAdapter);
			putTaskWithEntity(jobAdapter, taskEntity);
		} else {
			String taskName = createTaskNameForStorage(jobAdapter);
			taskEntity = taskTypesMap.get(taskName);
		}
		return taskEntity;
	}

	protected void updateIdentifierwithIds(TaskIdentifier identifier,
			Integer taskTypeId, Integer taskInstanceId) {
		RunnableTaskIdentifier taskIdentifier = (RunnableTaskIdentifier) identifier;
		taskIdentifier.setTaskTypeId(taskTypeId);
		taskIdentifier.setTaskInstanceId(taskInstanceId);
	}

	protected void storeParameters(JobDetail jobDetail, Properties properties) {
		QuartzUtils.storePropertiesInJobDataMap(jobDetail, properties);
	}

	protected void storeTaskTypeParameters(JobDetail jobDetail, TaskTypeEntity taskEntity) {
		Preconditions.checkNotNull(jobDetail, "jobDetail can not be null");
		Preconditions.checkNotNull(taskEntity, "taskEntity can not be null");
		
		JobDataMap  jobDataMap = jobDetail.getJobDataMap();
		jobDataMap.put(TASK_TYPE_PARAMS_KEY, taskEntity.getParamaters());
	}
	
	protected void storeTaskInstanceParameters(JobDetail jobDetail, TaskInstanceEntity taskInstanceEntity) {
		Preconditions.checkNotNull(jobDetail, "jobDetail can not be null");
		Preconditions.checkNotNull(taskInstanceEntity, "taskEntity can not be null");
		
		JobDataMap  jobDataMap = jobDetail.getJobDataMap();
		jobDataMap.put(TASK_INSTANCE_PARAMS_KEY, taskInstanceEntity.getParamaters());
	}
	

	@Override
	public TaskController registerTransientTask(Runnable runnable, java.util.Timer timer, TaskKey taskKey,
			Map<String, Object> stateValues) throws IllegalArgumentException {

		Preconditions.checkNotNull(runnable, "runnable cannot be null");
		Preconditions.checkNotNull(timer, "timerList cannot be null");
		Preconditions.checkNotNull(taskKey, "name cannot be null nor empty");
		
		TransientJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new TransientNoConcurrentJobAdapter();
		}else{
			jobAdapter = new TransientJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnable.getClass().getName());
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, taskKey, RECOVERY_MODE_FALSE);
		if (stateValues != null){
			populateJobDetail(jobDetail, stateValues);
		}

		try {
			Trigger trigger = QuartzTriggersFactory.createTrigger(timer);
			scheduler.scheduleJob(jobDetail, trigger);
			return createTaskController(jobAdapter);
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	@Override
	public TaskController registerImmediateTask(Class<? extends Runnable> runnableClass,
			 Map<String, Object> runningParams, String name, String description) throws IllegalArgumentException, SchedulerException {
		
		Preconditions.checkNotNull(runnableClass, "runnable cannot be null");

		Runnable runnable = new SpringDelegator(getClassName(runnableClass.getName()));
		//TaskTypeEntity jobType = taskTypeDao.findByTaskExcClass(runnableClass.getName());
		TaskKey taskKey = new TaskKey(name,getClassName(runnableClass.getName()));
		
		DurableJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new DurableNoConcurrentJobAdapter();
		}else{
			jobAdapter = new DurableJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnableClass.getName());
		jobAdapter.setDescription(description);
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, recoveryMode);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);

		try {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());			
			if (runningParams != null){
				addParametersToInstance(instanceEntity,runningParams);
				storeTaskInstanceParameters(jobDetail, instanceEntity);
			}
			taskInstanceDao.saveAndFlush(instanceEntity);
			storeInstanceId(jobDetail, instanceEntity.getTaskInstanceId());
			updateIdentifierwithIds(taskController.getIdentifier(),	taskEntity.getTaskId(),
					instanceEntity.getTaskInstanceId());
			scheduler.addJob(jobDetail, true);
			scheduler.triggerJob(jobDetail.getKey());
			return taskController;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
			throw e;
		} catch (Throwable e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
	}
	
	@Override
	public TaskController registerDurableTask(Class<? extends Runnable> runnableClass, java.util.Timer timer,
			 Map<String, Object> runningParams,String name) throws IllegalArgumentException, SchedulerException {

		Preconditions.checkNotNull(runnableClass, "runnable cannot be null");
		Preconditions.checkNotNull(timer, "timer cannot be null");
		
		//Runnable runnable = new SpringDelegator(getClassName(runnableClass.getName()));
		//TaskTypeEntity jobType = taskTypeDao.findByTaskExcClass(runnableClass.getName());
		//TaskKey taskKey = new TaskKey(SF.isEmpty(jobType)?getClassName(runnableClass.getName()):jobType.getName(),Scheduler.DEFAULT_GROUP);
		//TaskKey taskKey = new TaskKey(name,SF.isEmpty(jobType)?getClassName(runnableClass.getName()):jobType.getName());
		Runnable runnable = new SpringDelegator(getClassName(runnableClass.getName()));
		//TaskTypeEntity jobType = taskTypeDao.findByTaskExcClass(runnableClass.getName());
		TaskKey taskKey = new TaskKey(name,getClassName(runnableClass.getName()));
		
		DurableJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new DurableNoConcurrentJobAdapter();
		}else{
			jobAdapter = new DurableJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnableClass.getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, recoveryMode);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);
//		storeTaskFacade(jobDetail);

		try {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());			
			if (runningParams != null){
				addParametersToInstance(instanceEntity,runningParams);
				storeTaskInstanceParameters(jobDetail, instanceEntity);
			}
			taskInstanceDao.saveAndFlush(instanceEntity);
			storeInstanceId(jobDetail, instanceEntity.getTaskInstanceId());
			Trigger trigger = QuartzTriggersFactory.createTrigger(timer);
			updateIdentifierwithIds(taskController.getIdentifier(),	taskEntity.getTaskId(),
					instanceEntity.getTaskInstanceId());
			scheduler.scheduleJob(jobDetail, trigger);
			return taskController;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
			throw e;
		} catch (Exception e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
	
	}
	
	@Override
	public TaskController registerRecoverableTask(Class<? extends Runnable> runnableClass, java.util.Timer timer,
			 Map<String, Object> runningParams,String name) throws IllegalArgumentException {
		
		Preconditions.checkNotNull(timer, "timer cannot be null");
		Runnable runnable = new SpringDelegator(getClassName(runnableClass.getName()));
		//TaskTypeEntity jobType = taskTypeDao.findByTaskExcClass(runnableClass.getName());
		TaskKey taskKey = new TaskKey(name,getClassName(runnableClass.getName()));
		
		if (timer instanceof DateTimer){
			registerRecoverableCronTask(runnable, timer, taskKey);
		}
		Preconditions.checkNotNull(runnable, "runnable cannot be null");
		Preconditions.checkNotNull(taskKey, "name cannot be null nor empty");
		
		RecoverablePersistentJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new RecoverablePersistentNoConcurrentJobAdapter();
		}else{
			jobAdapter = new RecoverablePersistentJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnable.getClass().getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, taskKey, RECOVERY_MODE_FALSE);
		jobDetail.getJobDataMap().put(RUNNABLE_KEY, runnable);		
		jobDetail.getJobDataMap().put(TASKKEY_KEY, taskKey);
		QuartzUtils.storeTaskId(jobDetail, taskEntity.getTaskId());
		Trigger trigger = (SimpleTrigger) QuartzTriggersFactory.createTrigger(timer);
		setTimerExecutionNumber(timer, trigger);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);
		
		try {
			WaitingTaskController taskController = createWaitingTaskController(jobAdapter);
			
			//create number of taskInstances and persist them in database
			int executioanNumber = ((IntervalTimer) timer).getNumberOfExecution();
			List<TaskInstanceEntity> taskInstancesList = new ArrayList<TaskInstanceEntity>();
			for (int counter=0; counter < executioanNumber; counter++){
				TaskInstanceEntity instanceEntity = createWaitingTaskInstance(taskController.getIdentifier(), jobAdapter);
				instanceEntity.setTaskTypeEntity(taskEntity);
				instanceEntity.setTaskId(taskEntity.getTaskId());
				if (runningParams != null){
					addParametersToInstance(instanceEntity,runningParams);
					storeTaskInstanceParameters(jobDetail, instanceEntity);
				}
				taskInstancesList.add(instanceEntity);
			}
			taskInstanceDao.save(taskInstancesList);
			storeTaskInstancesList(jobDetail, taskInstancesList);
			taskController.addWaitingTasks(taskInstancesList);
			addWaitingTaskControllerToSchedulerContext(taskController);
			scheduler.scheduleJob(jobDetail, trigger);
			return taskController;
		} catch (Exception e) {
				logger.error("Unable register runnable and timer to scheduler", e);
		}
		return null;
	}

	@Override
	public TaskController registerImmediateTask(Class<? extends Runnable> runnableClass,
			 Map<String, Object> runningParams) throws IllegalArgumentException, SchedulerException {
		
		Preconditions.checkNotNull(runnableClass, "runnable cannot be null");

		Runnable runnable = new SpringDelegator(getClassName(runnableClass.getName()));
		TaskTypeEntity jobType = taskTypeDao.findByTaskExcClass(runnableClass.getName());
		TaskKey taskKey = new TaskKey(jobType == null ? getClassName(runnableClass.getName()+ "_" +new Date().getTime()):jobType.getName(),Scheduler.DEFAULT_GROUP);
		
		DurableJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new DurableNoConcurrentJobAdapter();
		}else{
			jobAdapter = new DurableJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnableClass.getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, recoveryMode);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);

		try {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());			
			if (runningParams != null){
				addParametersToInstance(instanceEntity,runningParams);
				storeTaskInstanceParameters(jobDetail, instanceEntity);
			}
			taskInstanceDao.saveAndFlush(instanceEntity);
			storeInstanceId(jobDetail, instanceEntity.getTaskInstanceId());
			updateIdentifierwithIds(taskController.getIdentifier(),	taskEntity.getTaskId(),
					instanceEntity.getTaskInstanceId());
			scheduler.addJob(jobDetail, true);
			scheduler.triggerJob(jobDetail.getKey());
			return taskController;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
			throw e;
		} catch (Throwable e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
	}
	
	@Override
	public TaskController registerDurableTask(Class<? extends Runnable> runnableClass, java.util.Timer timer,
			 Map<String, Object> runningParams) throws IllegalArgumentException, SchedulerException {

		Preconditions.checkNotNull(runnableClass, "runnable cannot be null");
		Preconditions.checkNotNull(timer, "timer cannot be null");
		
		Runnable runnable = new SpringDelegator(getClassName(runnableClass.getName()));
		TaskTypeEntity jobType = taskTypeDao.findByTaskExcClass(runnableClass.getName());
		TaskKey taskKey = new TaskKey(jobType.getName(),Scheduler.DEFAULT_GROUP);
		
		DurableJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new DurableNoConcurrentJobAdapter();
		}else{
			jobAdapter = new DurableJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnableClass.getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, recoveryMode);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);
//		storeTaskFacade(jobDetail);

		try {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());			
			if (runningParams != null){
				addParametersToInstance(instanceEntity,runningParams);
				storeTaskInstanceParameters(jobDetail, instanceEntity);
			}
			taskInstanceDao.saveAndFlush(instanceEntity);
			storeInstanceId(jobDetail, instanceEntity.getTaskInstanceId());
			Trigger trigger = QuartzTriggersFactory.createTrigger(timer);
			updateIdentifierwithIds(taskController.getIdentifier(),	taskEntity.getTaskId(),
					instanceEntity.getTaskInstanceId());
			scheduler.scheduleJob(jobDetail, trigger);
			return taskController;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
			throw e;
		} catch (Exception e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
	
	}
	
	
	@Override
	public TaskController registerDurableTask(Runnable runnable, java.util.Timer timer,
			TaskKey taskKey, Map<String, Object> runningParams) throws IllegalArgumentException {

		Preconditions.checkNotNull(runnable, "runnable cannot be null");
		Preconditions.checkNotNull(timer, "timer cannot be null");
		Preconditions.checkNotNull(taskKey, "name cannot be null nor empty");

		DurableJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new DurableNoConcurrentJobAdapter();
		}else{
			jobAdapter = new DurableJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnable.getClass().getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, recoveryMode);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);
//		storeTaskFacade(jobDetail);

		try {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());			
			if (runningParams != null){
				addParametersToInstance(instanceEntity,runningParams);
				storeTaskInstanceParameters(jobDetail, instanceEntity);
			}
			taskInstanceDao.saveAndFlush(instanceEntity);
			storeInstanceId(jobDetail, instanceEntity.getTaskInstanceId());
			Trigger trigger = QuartzTriggersFactory.createTrigger(timer);
			updateIdentifierwithIds(taskController.getIdentifier(),	taskEntity.getTaskId(),
					instanceEntity.getTaskInstanceId());
			scheduler.scheduleJob(jobDetail, trigger);
			return taskController;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	/**
	 * This method register Timer with runnable with jobAdapter. 
	 * <br> Please note this method is called within registerRecoverableTask
	 */
	protected TaskController registerRecoverableCronTask(Runnable runnable, java.util.Timer timer, TaskKey taskKey)
		throws IllegalArgumentException {
		Preconditions.checkNotNull(runnable, "runnable cannot be null");
		Preconditions.checkNotNull(taskKey, "name cannot be null nor empty");
		
		RecoverableJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new RecoverableNoConcurrentJobAdapter();
		}else{
			jobAdapter = new RecoverableJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnable.getClass().getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, taskKey, RECOVERY_MODE_TRUE);
		storeRunnable(jobDetail, runnable);

		Trigger trigger;
		if (runnable instanceof CriticalJob) {
			trigger = QuartzTriggersFactory.createTrigger(timer, MissedScheduledPolicy.EXECUTE_NOW_WITH_EXISITING_COUNT);
		} else {
			trigger = QuartzTriggersFactory.createTrigger(timer, MissedScheduledPolicy.EXECUTE_NEXT_WITH_REMAINING_COUNT);
		}
		
		try {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(
					taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());
			taskInstanceDao.saveAndFlush(instanceEntity);
			storeInstanceId(jobDetail, instanceEntity.getTaskInstanceId());
			scheduler.scheduleJob(jobDetail, trigger);
			updateIdentifierwithIds(taskController.getIdentifier(),
					taskEntity.getTaskId(), instanceEntity.getTaskInstanceId());
			return taskController;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	private void addWaitingTaskControllerToSchedulerContext(WaitingTaskController taskController){
		Preconditions.checkNotNull(taskController, "taskController can not be null");
		try {
			List<WaitingTaskController> waitingTakControllerList = 
				(List<WaitingTaskController>) scheduler.getContext().get(WAITING_TASKCONTROLLER_LIST_KEY);
			if (waitingTakControllerList == null){
				waitingTakControllerList = new ArrayList<WaitingTaskController>();
			}
			waitingTakControllerList.add(taskController);
			scheduler.getContext().put(WAITING_TASKCONTROLLER_LIST_KEY, waitingTakControllerList);
		} catch (SchedulerException e) {
			logger.error("Unable to add waitingTaskController to context", e);
		}
	}

	@Override
	public TaskController registerRecoverableTask(Runnable runnable, java.util.Timer timer,
			TaskKey taskKey, Map<String, Object> runningParams) throws IllegalArgumentException {
		
		Preconditions.checkNotNull(timer, "timer cannot be null");
		if (timer instanceof DateTimer){
			registerRecoverableCronTask(runnable, timer, taskKey);
		}
		Preconditions.checkNotNull(runnable, "runnable cannot be null");
		Preconditions.checkNotNull(taskKey, "name cannot be null nor empty");
		
		RecoverablePersistentJobAdapter jobAdapter;
		if (runnable instanceof NoCuncurrentJob){
			jobAdapter = new RecoverablePersistentNoConcurrentJobAdapter();
		}else{
			jobAdapter = new RecoverablePersistentJobAdapter();
		}
		
		jobAdapter.setTaskKey(taskKey);
		jobAdapter.setRunnable(runnable);
		jobAdapter.setOriginalExecClassName(runnable.getClass().getName());
		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, taskKey, RECOVERY_MODE_FALSE);
		jobDetail.getJobDataMap().put(RUNNABLE_KEY, runnable);		
		jobDetail.getJobDataMap().put(TASKKEY_KEY, taskKey);
		QuartzUtils.storeTaskId(jobDetail, taskEntity.getTaskId());
		Trigger trigger = (SimpleTrigger) QuartzTriggersFactory.createTrigger(timer);
		setTimerExecutionNumber(timer, trigger);
		storeTaskTypeParameters(jobDetail, taskEntity);
		storeRunnable(jobDetail, runnable);
		
		try {
			WaitingTaskController taskController = createWaitingTaskController(jobAdapter);
			
			//create number of taskInstances and persist them in database
			int executioanNumber = ((IntervalTimer) timer).getNumberOfExecution();
			List<TaskInstanceEntity> taskInstancesList = new ArrayList<TaskInstanceEntity>();
			for (int counter=0; counter < executioanNumber; counter++){
				TaskInstanceEntity instanceEntity = createWaitingTaskInstance(taskController.getIdentifier(), jobAdapter);
				instanceEntity.setTaskTypeEntity(taskEntity);
				instanceEntity.setTaskId(taskEntity.getTaskId());
				if (runningParams != null){
					addParametersToInstance(instanceEntity,runningParams);
					storeTaskInstanceParameters(jobDetail, instanceEntity);
				}
				taskInstancesList.add(instanceEntity);
			}
			taskInstanceDao.save(taskInstancesList);
			storeTaskInstancesList(jobDetail, taskInstancesList);
			taskController.addWaitingTasks(taskInstancesList);
			addWaitingTaskControllerToSchedulerContext(taskController);
			scheduler.scheduleJob(jobDetail, trigger);
			return taskController;
		} catch (Exception e) {
				logger.error("Unable register runnable and timer to scheduler", e);
		}
		return null;
	}

	// This method populate JobDetail with values we want it
	// to have, each time task is executed
	private void populateJobDetail(JobDetail jobDetail, Map<String, Object> stateValues) {
		JobDataMap map = jobDetail.getJobDataMap();
		for (String key : stateValues.keySet()) {
			Object value = stateValues.get(key);
			map.put(key, value);
		}
	}

	@Override
	public void removeScheduledJob(TaskIdentifier taskIdentifier)
			throws IllegalArgumentException, NoSuchElementException {
		Preconditions.checkNotNull(taskIdentifier, "taskIdentifier cannot be null");
		String name = taskIdentifier.getTaskTypeName();
		String group = taskIdentifier.getGroup();

		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");

		JobKey jobKey = new JobKey(name, group);
		try {
			scheduler.deleteJob(jobKey);
		} catch (SchedulerException e) {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void removeScheduledJob(Integer id)
			throws IllegalArgumentException, NoSuchElementException {
		TaskTypeEntity taskTypeEntitie = taskTypeDao.find(id);
		JobKey jobKey = new JobKey(taskTypeEntitie.getName(), taskTypeEntitie.getGroup());
		try 
		{					
			scheduler.deleteJob(jobKey);
		} 
		catch (SchedulerException e) 
		{
			throw new NoSuchElementException();
		}
	}
	
	@Override
	public void removeAllScheduledJobs()
			throws IllegalArgumentException, NoSuchElementException {
		List<TaskTypeEntity> taskTypeEntitiesList = null;
		try {
			taskTypeEntitiesList = taskTypeDao.getAllTaskTypeEntities();
			for (TaskTypeEntity entity : taskTypeEntitiesList) 
			{				
				JobKey jobKey = new JobKey(entity.getName(), entity.getGroup());
				try 
				{					
					scheduler.deleteJob(jobKey);
				} 
				catch (SchedulerException e) 
				{
					throw new NoSuchElementException();
				}
			}
		} 
		catch (Exception e) 
		{
			logger.error("Unable to init scheduler, Unable to fetch Tasks types from DB", e);
		}
	}

	@Override
	public void addDateTimerWithRecovery(RecoverableJobAdapter jobAdapter,
			java.util.Timer newTimer) throws IllegalArgumentException {
		Preconditions.checkNotNull(jobAdapter, "jobAdapter cannot be null");
        Preconditions.checkNotNull(newTimer, "newTimer cannot be null");

		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, RECOVERY_MODE_TRUE);
		Trigger newTrigger;
		if (jobAdapter.getRunnable() instanceof CriticalJob) {
			newTrigger = QuartzTriggersFactory.createTrigger(newTimer,
					MissedScheduledPolicy.EXECUTE_NOW_WITH_EXISITING_COUNT);
		} else {
			newTrigger = QuartzTriggersFactory.createTrigger
				(newTimer, MissedScheduledPolicy.EXECUTE_NEXT_WITH_REMAINING_COUNT);
		}
		try {
			scheduler.scheduleJob(jobDetail, newTrigger);
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
		}
	}

	/**
	 * 
	 * @param jobAdapter
	 *            . The jobAdapter of the job that should be registered.
	 * @param timer
	 *            . The timer that should be registered.
	 * @return first WAITING TaskInstanceId.
	 * 
	 *         This method create for IntervalTimer all its (repeating number of
	 *         timer) <br>
	 *         taskInstances that should run, and persist them with WAITING
	 *         STATUS
	 */
	protected Integer persistJobWaitingInstancess(
			RecoverableJobAdapter jobAdapter, java.util.Timer timer) {
		if (timer instanceof DateTimer) {
			return null;
		}

		int executingNumber = ((IntervalTimer) timer).getNumberOfExecution();
		Integer firstTaskInstanceId = null;

		TaskTypeEntity taskEntity = getTaskTaskType(jobAdapter);
		for (int counter = 0; counter < executingNumber; counter++) {
			TaskController taskController = createTaskController(jobAdapter);
			TaskInstanceEntity instanceEntity = createTaskInstance(
					taskController.getIdentifier(), jobAdapter);
			instanceEntity.setTaskTypeEntity(taskEntity);
			instanceEntity.setTaskId(taskEntity.getTaskId());
			instanceEntity.setTaskStatus(WAITING);
			instanceEntity.setPhisycalName(EMPTY_WAITING_PREFIX);
			taskInstanceDao.saveAndFlush(instanceEntity);
			if (firstTaskInstanceId == null) {
				firstTaskInstanceId = instanceEntity.getTaskInstanceId();
			}
		}
		return firstTaskInstanceId;
	}

    /*	
	@Override
	public Integer addTimerWithRecovery(RecoverableJobAdapter jobAdapter,
			Timer newTimer) throws IllegalArgumentException {
		Preconditions.checkNotNull(jobAdapter, "jobAdapter cannot be null");
		Preconditions.checkNotNull(newTimer, "newTimer cannot be null");

		if (newTimer instanceof DateTimer) {
			addDateTimerWithRecovery(jobAdapter, newTimer);
		}

		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, false);
		Trigger newTrigger = QuartzTriggersFactory.createTrigger(newTimer);
		try {
			Integer taskInstnceId = persistJobWaitingInstancess(jobAdapter,	newTimer);
			scheduler.scheduleJob(jobDetail, newTrigger);
			return taskInstnceId;
		} catch (SchedulerException e) {
			logger.error("Unable register runnable and timer to scheduler", e);
		}
		return null;
	}

	@Override
	public void addTimer(RunnableJobAdapter jobAdapter, Timer newTimer)
			throws IllegalArgumentException {
		Preconditions.checkNotNull(jobAdapter, "jobAdapter cannot be null");
		Preconditions.checkNotNull(newTimer, "newTimer cannot be null");

		JobDetail jobDetail = QuartzUtils.createJobDetail(jobAdapter, false);
		Trigger newTrigger = QuartzTriggersFactory.createTrigger(newTimer);
		try {
			scheduler.scheduleJob(jobDetail, newTrigger);
		} catch (SchedulerException e) {
			logger.error("Unable register JobAdapter and timer to scheduler", e);
		}

	}
	*/

	public void removeTimerFromScheduler(java.util.Timer timer)
			throws IllegalArgumentException, NoSuchElementException {
		Assert.hasText(timer.getName(), "name cannot be null nor empty");
		Assert.hasText(timer.getGroup(), "group cannot be null nor empty");

		TriggerKey triggerKey = new TriggerKey(timer.getName(),
				timer.getGroup());
		try {
			scheduler.unscheduleJob(triggerKey);
		} catch (SchedulerException e) {
			throw new NoSuchElementException();
		}
	}

	public void pauseTimer(java.util.Timer timer) throws IllegalArgumentException,
			NoSuchElementException {
		if (timer == null) {
			throw new NoSuchElementException();
		}

		String name = timer.getName();
		String group = timer.getGroup();

		// The combination of name and group is used to identify Timer,
		// If either is null no identification is not possible
		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");

		TriggerKey triggerKey = new TriggerKey(name, group);

		try {
			scheduler.pauseTrigger(triggerKey);
		} catch (SchedulerException e) {
			logger.error("Unable to pause timer", e);
		}
	}

	@Override
	public void pauseJob(TaskIdentifier taskIdentifier)
			throws IllegalArgumentException {
		Preconditions.checkNotNull(taskIdentifier, "taskIdentifier cannot be null");

		String name = taskIdentifier.getTaskTypeName();
		String group = taskIdentifier.getGroup();

		// The combination of name and group is used to identify job,
		// If either is null no identification is not possible
		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");

		JobKey jobKey = new JobKey(name, group);
		try {
			scheduler.pauseJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("Unable to pause job", e);
		}
	}
	
	private boolean isTaskInstanceExecuting(TaskInstanceEntity taskInstance){
		String status = taskInstance.getStatus();
		if (status.equals(PENDING.toString()) || status.equals(STARTED.toString()) || 
				status.equals(RUNNING.toString()) || status.equals(AFTER_RUN.toString()) ){
			return true;
		}
		return false;
	}
	
	
	// This method usage is by UI
	@Override
	public void pauseJob(TaskInstanceEntity taskInstance){
		Preconditions.checkNotNull(taskInstance, "taskInstance cannot be null");
		
		String name = taskInstance.getTaskTypeEntity().getName();
		String group = taskInstance.getTaskTypeEntity().getGroup();
		
		JobKey jobKey = new JobKey(name, group);
		try {
			scheduler.pauseJob(jobKey);
			if (isTaskInstanceExecuting(taskInstance)){
				taskInstance.setStatus(CANCELED_BY_USER.toString());
			}else{
				taskInstance.setStatus(CANCELED.toString());
			}
			taskInstanceDao.save(taskInstance);
			
		} catch (SchedulerException e) {
			logger.error(UNABLE_TO_PAUSE_JOB, e);
		} catch (Exception e) {
			logger.error(EXCEPTION_WHILE_EXECUTING, e);
		}
	}

	@Override
	public void resumeTimer(java.util.Timer timer) throws IllegalArgumentException,
			NoSuchElementException {
		Preconditions.checkNotNull(timer, "timer cannot be null");
		String name = timer.getName();
		String group = timer.getGroup();

		// The combination of name and group is used to identify Timer,
		// If either is null no identification is not possible

		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");

		TriggerKey triggerKey = new TriggerKey(name, group);
		try {
			scheduler.resumeTrigger(triggerKey);
		} catch (SchedulerException e) {
			logger.error("Unable to resume timer", e);
		}
	}

	public SchedulerFactory getSchedulerFactory() {
		return schedulerFactory;
	}

	@Autowired
	public void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		this.schedulerFactory = schedulerFactory;
	}

	@Override
	public boolean containsJob(TaskIdentifier taskIdentifier)
			throws IllegalArgumentException {

		Preconditions.checkNotNull(taskIdentifier, "taskIdentifier cannot be null");
		String name = taskIdentifier.getTaskTypeName();
		String group = taskIdentifier.getGroup();

		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");

		JobKey jobKey = new JobKey(name, group);
		try {
			return scheduler.checkExists(jobKey);
		} catch (SchedulerException e) {
			return false;
		}
	}

	@Override
	public boolean containsTimer(java.util.Timer timer) throws IllegalArgumentException {

		Preconditions.checkNotNull(timer, "timer cannot be null");
		Assert.hasText(timer.getName(), "name cannot be null nor empty");
		Assert.hasText(timer.getGroup(), "group cannot be null nor empty");

		TriggerKey triggerKey = new TriggerKey(timer.getName(),
				timer.getGroup());
		try {
			return scheduler.checkExists(triggerKey);
		} catch (SchedulerException e) {
			return false;
		}
	}

	@Override
	public void pauseJobGroup(String group) throws IllegalArgumentException {
		Assert.hasText(group);

		GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(group);
		try {
			scheduler.pauseJobs(groupMatcher);
		} catch (SchedulerException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void resumeJobGroup(String group)
			throws IllegalArgumentException {
		Assert.hasText(group);
		GroupMatcher<JobKey> groupMatcher = GroupMatcher.groupEquals(group);
		try {
			scheduler.resumeJobs(groupMatcher);
		} catch (SchedulerException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void pauseTimersGroup(String group) throws IllegalArgumentException {
		Assert.hasText(group);
		GroupMatcher<TriggerKey> groupMatcher = GroupMatcher.groupEquals(group);
		try {
			scheduler.pauseTriggers(groupMatcher);
		} catch (SchedulerException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void resumeTimersGroup(String group) throws IllegalArgumentException {
		Assert.hasText(group);
		GroupMatcher<TriggerKey> groupMatcher = GroupMatcher.groupEquals(group);
		try {
			scheduler.resumeTriggers(groupMatcher);
		} catch (SchedulerException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public List<String> getJobsGroupNames() {
		try {
			return scheduler.getJobGroupNames();
		} catch (SchedulerException e) {
			return null;
		}
	}

	@Override
	public List<String> getTimerGroupNames() {
		try {
			return scheduler.getTriggerGroupNames();
		} catch (SchedulerException e) {
			return null;
		}
	}

	@Override
	public void setRecoveryMode(boolean recoveryMode) {
		this.recoveryMode = recoveryMode;
	}

	@Override
	public boolean isRecoveryMode() {
		return recoveryMode;
	}

	public TaskTypeDao getTaskTypeDao() {
		return taskTypeDao;
	}

	@Autowired
	public void setTaskTypeDao(TaskTypeDao taskTypeDao) {
		this.taskTypeDao = taskTypeDao;
	}

	public TaskInstanceDao getTaskInstanceDao() {
		return taskInstanceDao;
	}

	@Autowired
	public void setTaskInstanceDao(TaskInstanceDao taskInstanceDao) {
		this.taskInstanceDao = taskInstanceDao;
	}

	
	public TaskErrorDao getTaskErrorDao() {
		return taskErrorDao;
	}

	@Autowired
	public void setTaskErrorDao(TaskErrorDao taskErrorDao) {
		this.taskErrorDao = taskErrorDao;
	}

	@Override
	public void cleanTaskInstance(Integer taskInstanceId) {
		taskInstanceDao.deleteTaskInstance(taskInstanceId);
	}

	@Override
	public void resumeJob(TaskIdentifier taskIdentifier)
			throws IllegalArgumentException {
		Preconditions.checkNotNull(taskIdentifier, "taskIdentifier cannot be null");

		String name = taskIdentifier.getTaskTypeName();
		String group = taskIdentifier.getGroup();

		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");

		JobKey jobKey = new JobKey(name, group);
		try {
			scheduler.resumeJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("Unable to resume RunnableJobAdapter", e);
		}
	}
	
	@Override
	public void resumeJob(TaskInstanceEntity taskInstance){
		Preconditions.checkNotNull(taskInstance, "taskInstance cannot be null");
		
		String name = taskInstance.getTaskTypeEntity().getName();
		String group = taskInstance.getTaskTypeEntity().getGroup();
		
		Assert.hasText(name, "name cannot be null nor empty");
		Assert.hasText(group, "group cannot be null nor empty");
		
		JobKey jobKey = new JobKey(name, group);
		try {
			scheduler.resumeJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("Unable to pause job", e);
		}
	}
	
	public  void setTaskStatus(Integer taskInstanceId, TaskStatus taskStatus) {
		taskInstanceDao.setTaskStatus(taskInstanceId, taskStatus);
	}
	
	public  TaskInstanceEntity getWaitingTaskInstanceEntity(Integer taskId, List<Integer> idsList, String className) {
		return taskInstanceDao.getWaitingTaskInstanceEntity(taskId, idsList, className);
	}

	@Override
	public TaskInstanceEntity getWaitingTaskInstanceEntity(String name, String  group){
		return taskInstanceDao.getWaitingTaskInstanceEntity(name, group);
	}
	
	@Override
	public  TaskInstanceEntity getWaitingTaskInstanceEntity(TaskKey taskKey) {
		return taskInstanceDao.getWaitingTaskInstanceEntity(taskKey.getName(), taskKey.getGroup());
	}

	@Override
	public  void setProgress(Integer taskInstanceId, Integer progress) {
		taskInstanceDao.setProgress(taskInstanceId, progress);
	}
	
	
	@Override
	public  void setInstanceProperties(Integer taskInstanceId, Properties prop) {
		taskInstanceDao.setProperties(taskInstanceId, ClobUtils.createClob(prop));
	}

	@Override
	public  void setAmount(Integer taskInstanceId, Integer amount) {
		taskInstanceDao.setAmount(taskInstanceId, amount);
	}
	
	@Override
	public TaskStatus getTaskInstanceEntityStatus(Integer taskInstanceEntityId){
		return taskInstanceDao.getTaskInstanceEntityStatus(taskInstanceEntityId);
		
	}

	@Override
	public  void setError(Integer taskInstanceId, String errorMessage) {
		TaskInstanceEntity taskInstance  = taskInstanceDao.getTaskInstance(taskInstanceId);
		taskErrorDao.addError(taskInstance, errorMessage);
	}

	@Override
	public  void setError(Integer taskInstaceId, String errorMessage,
			String stackTrace, ErrorSeverity sevirity) {
		TaskInstanceEntity taskInstance  = taskInstanceDao.getTaskInstance(taskInstaceId);
		taskErrorDao.addError(taskInstance, errorMessage, stackTrace, sevirity);
	}

	@Override
	public  void setAmount(Integer taskInstanceId, Integer amount, int total) {
		taskInstanceDao.setAmount(taskInstanceId, amount, total);
	}

	@Override
	public  void setMessage(Integer taskInstanceId, String message) {
		taskInstanceDao.setMessage(taskInstanceId, message);
	}
	
	private boolean listContainsError(List<TaskErrorEntity> taskErrorList){
		for (TaskErrorEntity taskError: taskErrorList){
			ErrorSeverity errorSevirity = ErrorSeverity.valueOf(taskError.getSeverity());
			if (errorSevirity ==  ErrorSeverity.FATAL_ERROR || errorSevirity == ErrorSeverity.ERROR){
				return true;
			}
		}
		return false;
	}

	@Override
	public void setTaskCompleteStatus(Integer taskInstanceId) {
		TaskInstanceEntity taskInstance = taskInstanceDao.getTaskInstance(taskInstanceId);  
		taskInstance.setEndDate(new Date());
		taskInstance.setProgress(100);
		List<TaskErrorEntity> taskErrorList = taskErrorDao.getTaskErrors(taskInstanceId);
		if (taskErrorList.isEmpty()){
			taskInstance.setTaskStatus(COMPLETED);
			return;
		}
		if (listContainsError(taskErrorList)){
			taskInstance.setTaskStatus(COMPLETED_WITH_PROBLEM);
		}else{
			taskInstance.setTaskStatus(COMPLETED_WITH_WARNING);
		}

		
	}

	@Override
	public  TaskInstanceEntity getTaskInstance(Integer taskInstanceId) {
		return taskInstanceDao.getTaskInstance(taskInstanceId);
	}

	@Override
	public  List<TaskErrorEntity> getTaskInstanceError(Integer taskInstanceId) {
		return taskErrorDao.getTaskErrors(taskInstanceId);
	}

	/**
	 * Inject application context.
	 * 
	 * @param applicationContext
	 *            Spring's application context.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}


	@Override
	public TaskInstanceEntity getCopyOfTaskInstanceEntity(Integer taskInstanceId) {
		return taskInstanceDao.getCopyOfTaskInstanceEntity(taskInstanceId);
	}


	@Override
	public TaskStatus getJobStatus(TaskIdentifier taskIdentifier) {
		String isnatnceName = taskIdentifier.getTaskInstanceName();
		return taskInstanceDao.getTaskInstanceEntityStatus(isnatnceName);
	}

	@Override
	public TaskStatus getJobStatusByTaskInstanceId(Integer taskInstanceId) {
		return taskInstanceDao.getTaskInstanceEntityStatus(taskInstanceId);
	}

	@Override
	public Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}




	@Override
	public void setDescription(Integer taskInstanceId, String description) {
		TaskInstanceEntity taskInstance  = taskInstanceDao.getTaskInstance(taskInstanceId);
		taskInstance.setDescription(description);
		taskInstanceDao.saveAndFlush(taskInstance);
	}




	@Override
	public int getProgress(Integer taskInstanceId) {
		return taskInstanceDao.getProgress(taskInstanceId);	
	}


	@Override
	public void setTypeProperties(Integer taskInstanceId,Properties taskTypeProperties) {
		TaskInstanceEntity instance = taskInstanceDao.find(taskInstanceId);
		taskTypeDao.setProperties(instance.getTaskId(),ClobUtils.createClob(taskTypeProperties));		
	}




	@Override
	public int getRetries(Integer taskInstanceId) {
		TaskInstanceEntity instance = taskInstanceDao.find(taskInstanceId);
		TaskTypeEntity type  = taskTypeDao.find(instance.getTaskId());
		return type!= null && type.getRetries() !=null ?type.getRetries(): 0;
	}




	@Override
	public void setRetries(Integer taskInstanceId, int retries) {
		TaskInstanceEntity instance = taskInstanceDao.find(taskInstanceId);
		TaskTypeEntity type  = taskTypeDao.find(instance.getTaskId());
		type.setRetries(retries);
		taskTypeDao.saveAndFlush(type);
	}

}
