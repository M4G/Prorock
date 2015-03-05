package com.m4g;

import com.m4g.job.adapters.RecoverableJobAdapter;
import com.m4g.task.TaskIdentifier;
import com.m4g.task.TaskKey;
import com.m4g.task.TaskStatus;
import com.m4g.task.controllers.TaskController;
import com.m4g.task.entities.TaskInstanceEntity;
import com.m4g.timers.Timer;
import org.quartz.SchedulerException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 
 * @author aharon_br
 * 
 *         Scheduler service enable to register executable and time triggers in
 *         order to create processes running at different time segments defined
 *         in database.
 * 
 */
@Transactional
public interface SchedulerService{


	/**
	 * @param recoveryMode enable / disable recovery mode
	 * This method set the recovery mode of the scheduler <br>
	 * (i.e. execute tasks that was not executed)  
	 *  default is true. <br>
	 *  Please note it is not recommended to disable recovery mode. 
	 */
	public void setRecoveryMode(boolean recoveryMode);
	
	/**
	 * @return true if the recovery mode is enabled
	 */
	public boolean isRecoveryMode();

	/**
	 * 
	 * @param runnable the runnable we want to schedule
	 * @param timer describing the scheduling of the runnable
	 * @param taskKey Object describing name group and user of the job to scheduled
	 * @return TaskController of the job 
	 * @throws IllegalArgumentException
	 * 
	 * Please not registerTransientTask does not accessing the database
	 */
	public TaskController registerTransientTask(Runnable runnable, Timer timer,
                                                TaskKey taskKey, Map<String, Object> runningParams)
		throws IllegalArgumentException;
	
	/**
	 * 
	 * @param runnable the runnable we want to schedule
	 * @param timer describing the scheduling of the runnable
	 * @param taskKey Object describing name group and user of the job to scheduled
	 * @return TaskController of the job 
	 * @throws IllegalArgumentException
	 * 
	 * Please not registerDurableTask persist the task instance and its state in the database
	 */
	public TaskController registerDurableTask(Runnable runnable, Timer timer,
                                              TaskKey taskKey, Map<String, Object> runningParams)
		throws IllegalArgumentException;
	

	/**
	 * 
	 * @param runnable the runnable we want to schedule
	 * @param timer timer describing the scheduling of the runnable.
	 * @param taskKey Object describing name group and user of the job to scheduled
	 * @return TaskController of the job 
	 * @throws IllegalArgumentException
	 * 
	 * Please not registerRecoverableTask persist the task instance and its state <br>
	 *  in the database and use recovery strategies <br>
	 *  Please note the usage of CriticalJob interface to mark runnable as critical
	 */
	public TaskController registerRecoverableTask(Runnable runnable, Timer timer,
                                                  TaskKey taskKey, Map<String, Object> runningParams)
		throws IllegalArgumentException;


	/**
	 * 
	 * @param integer the taskIdentifier to omitted from scheduler
	 * @throws IllegalArgumentException when there is no name nor group
	 * @throws NoSuchElementException when this element is not exist in the scheduler
	 */
	public void removeScheduledJob(Integer integer)
			throws IllegalArgumentException, NoSuchElementException;

	/**
	 * @param jobAdapter the jobAdapter that should be added new timer
	 * @param newTimer the new date time to be added
	 * @throws IllegalArgumentException
	 * 
	 * Please note if the job is CriticalJob (interface)<br>
	 * than the trigger will created with  EXECUTE_NOW_WITH_EXISITING_COUNT misfire policy <br>
	 * else the trigger will created with  EXECUTE_NEXT_WITH_REMAINING_COUNT misfire policy
	 */
	public void addDateTimerWithRecovery(RecoverableJobAdapter jobAdapter, Timer newTimer) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param jobAdapter the job to add new trigger
	 * @param newTimer new trigger to add to existing job
	 * 
	 * This method use recovery persistence mechanism
	 */
//	public Integer addTimerWithRecovery(RecoverableJobAdapter jobAdapter, Timer newTimer) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param jobAdapter the job to add new trigger
	 * @param newTimer new trigger to add to existing job
	 */	
//	public void addTimer(RunnableJobAdapter jobAdapter, Timer newTimer) throws IllegalArgumentException;


	/**
	 * 
	 * @param timer The timer to be removed from Scheduler
	 */
	public void removeTimerFromScheduler(Timer timer)
			throws IllegalArgumentException, NoSuchElementException;

	/**
	 * 
	 * @param timer The timer we want to pause its activity
	 * 
	 *            This method pause timer. <br>
	 *            Please note this timer can resume its activity <br>
	 *            Using ResumeTimer method
	 * 
	 */
	public void pauseTimer(Timer timer) throws IllegalArgumentException,
			NoSuchElementException;

	/**
	 * 
	 * @param taskIdentifier The taskIdentifier of the the job we want to pause it activity.
	 * 
	 * This method pause job activity <br>
	 * Please note this job can resume its activity <br>
	 * Using ResumeJob method
	 */
	public void pauseJob(TaskIdentifier taskIdentifier) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param taskInstance The running taskInstance of the the job we want to pause it activity.
	 * 
	 * This method pause job activity <br>
	 * Please note this job can resume its activity <br>
	 * Using ResumeJob method
	 */
	public void pauseJob(TaskInstanceEntity taskInstance);
	
	/**
	 * 
	 * @param timer The paused timer to become active.
	 * @throws IllegalArgumentException
	 * 
	 * This method un-pause timer so it will be active
	 */
	public void resumeTimer(Timer timer) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param taskIdentifier The taskIdentifier describing  the job we want to check if  <br>
	 * is register within the scheduler contains it.
	 * 
	 * @return true if the scheduler contain this jobAdapter. 
	 */
	public boolean containsJob(TaskIdentifier taskIdentifier) throws IllegalArgumentException;
	
	
	/**
	 * 
	 * @param timer The timer we want to check if  <br>
	 * the scheduler contains it.
	 * 
	 * @return true if the scheduler contain this timer. 
	 */	
	public boolean containsTimer(Timer timer) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param group The job Group we want to pause all it tasks.
	 * 
	 * Pause all job of given group. 
	 */
	public void pauseJobGroup(String group) throws IllegalArgumentException;
	

	/**
	 * 
	 * @param group The job Group we want to resume all it tasks.
	 * 
	 * Resume all job of given group. 
	 */
	
	public void resumeJobGroup(String group) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param group The Timer Group we want to pause all it timer.
	 * 
	 * Pause all timers of given group. 
	 */
	public void pauseTimersGroup(String group) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param group The Timer Group we want to resume all it tasks.
	 * 
	 * Resume all timers of given group. 
	 */
	
	public void resumeTimersGroup(String group) throws IllegalArgumentException;
	
	/**
	 * 
	 * @return List of all SchedueldTask groups Names that exist within the scheduler
	 */
	public List<String> getJobsGroupNames();
	
	/**
	 * 
	 * @return List of all timer groups Names that exist within the scheduler
	 */
	public List<String> getTimerGroupNames();
	
	/**
	 * This method clean task that completed to run
	 */
	public void cleanTaskInstance(Integer taskInstanceId);

	/**
	 * @param taskIdentifier The paused taskIdentifier to become active. <br>
	 * @throws IllegalArgumentException
	 * 
	 * This method un-pause job so it will be active
	 */
	public void resumeJob(TaskIdentifier taskIdentifier) throws IllegalArgumentException;
	
	/**
	 * @param taskInstance The paused taskInstance to become active. <br>
	 * @throws IllegalArgumentException
	 * 
	 * This method un-pause job so it will be active
	 */
	
	public void resumeJob(TaskInstanceEntity taskInstance);
	
	public TaskStatus getJobStatus(TaskIdentifier taskIdentifier);

	TaskController registerImmediateTask(Class<? extends Runnable> runnableClass, Map<String, Object> runningParams)
		throws IllegalArgumentException, SchedulerException;
	
	TaskController registerDurableTask(Class<? extends Runnable> runnableClass, Timer timer, Map<String, Object> runningParams) 
		throws IllegalArgumentException, SchedulerException;

	TaskController registerDurableTask(Class<? extends Runnable> runnableClass,
                                       Timer timer, Map<String, Object> runningParams, String name)
			throws IllegalArgumentException, SchedulerException;

	void removeAllScheduledJobs() throws IllegalArgumentException,
			NoSuchElementException;

	public TaskStatus getJobStatusByTaskInstanceId(Integer taskInstanceId);

	TaskController registerImmediateTask(
            Class<? extends Runnable> runnableClass,
            Map<String, Object> runningParams, String name, String description)
			throws IllegalArgumentException, SchedulerException;

	TaskController registerRecoverableTask(
            Class<? extends Runnable> runnableClass, Timer timer,
            Map<String, Object> runningParams, String name)
			throws IllegalArgumentException;

	void removeScheduledJob(TaskIdentifier taskIdentifier)
			throws IllegalArgumentException, NoSuchElementException;
}
