package com.m4g.quartz;

import com.m4g.timers.DateTimer;
import com.m4g.timers.IntervalTimer;
import com.m4g.timers.MissedScheduledPolicy;
import com.m4g.timers.Timer;
import org.quartz.*;

import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

/**
 * 
 * @author aharon_br
 * This class is used for creating Quartz triggers 
 * from interfaces module timers  
 */
public class QuartzTriggersFactory {

	private static void setMissedJobPolicy(SimpleScheduleBuilder builder,
			MissedScheduledPolicy jobsPolicy){
		switch(jobsPolicy){
		case IGNORE :
			builder.withMisfireHandlingInstructionIgnoreMisfires();
			break;
		case EXECUTE_NOW_ONCE:
			break;
		case EXECUTE_NOW_WITH_EXISITING_COUNT:
			builder.withMisfireHandlingInstructionNowWithExistingCount();
			break;
		case EXECUTE_NEXT_WITH_EXISITING_COUNT:
			builder.withMisfireHandlingInstructionNextWithExistingCount();
			break;
		case EXECUTE_NOW_WITH_REMAINING_COUNT:
			builder.withMisfireHandlingInstructionNowWithRemainingCount();
			break;
		case EXECUTE_NEXT_WITH_REMAINING_COUNT:
			builder.withMisfireHandlingInstructionNextWithRemainingCount();
			break;
		}		
	}
	
	private static void setMissedJobPolicy(CronScheduleBuilder builder,
			MissedScheduledPolicy jobsPolicy){
		switch(jobsPolicy){
		case IGNORE :
			builder.withMisfireHandlingInstructionDoNothing();
			break;
		case EXECUTE_NOW_ONCE:
			builder.withMisfireHandlingInstructionFireAndProceed();
			break;
		case EXECUTE_NOW_WITH_EXISITING_COUNT:
			break;
		case EXECUTE_NEXT_WITH_EXISITING_COUNT:
			break;
		case EXECUTE_NOW_WITH_REMAINING_COUNT:
			break;
		case EXECUTE_NEXT_WITH_REMAINING_COUNT:
			break;
		}		
	}	
	
	/**
	 * 
	 * @param triggerName 
	 * @param group
	 * @param startTime
	 * @param endTime
	 * @param repeatingSecondsInterval
	 * @param repeatingNumber
	 * @return new Quartz SimpleTrigger
	 * 
	 * This method create Quartz SimpleTrigger
	 */
	public static SimpleTrigger createSimpleTrigger(String triggerName, String group,
			Date startTime, Date endTime, int repeatingSecondsInterval,
			int repeatingNumber, MissedScheduledPolicy policy){
		
		SimpleScheduleBuilder builder = simpleSchedule();
		setMissedJobPolicy(builder, policy);
		
		Trigger trigger = newTrigger() 
		.withIdentity(triggerKey(triggerName, group))
		.withSchedule(builder
	    .withIntervalInSeconds(repeatingSecondsInterval)
	    .withRepeatCount(repeatingNumber))
		.startAt(startTime)
		.endAt(endTime)
		.build();

		return (SimpleTrigger) trigger;
	}
	
	
	/**
	 * 
	 * @param name 
	 * @param group
	 * @param startTime
	 * @param endTime
	 * @param repeatingSecondsInterval
	 * @param repeatingNumber
	 * @return new Quartz SimpleTrigger
	 * 
	 * This method create Quartz SimpleTrigger <br>
	 * Please note this method register MisfireHandlingInstructionNextWithExistingCount to the trigger
	 */
	public static SimpleTrigger createSimpleTrigger(String name, String group,
			Date startTime, Date endTime, int repeatingSecondsInterval, int repeatingNumber){
		Trigger trigger = newTrigger() 
		.withIdentity(triggerKey(name, group))
		.withSchedule(simpleSchedule()
	    .withIntervalInSeconds(repeatingSecondsInterval)
	    .withRepeatCount(repeatingNumber)
	    .withMisfireHandlingInstructionNextWithExistingCount())
		.startAt(startTime)
		.endAt(endTime)
		.build();
		
		return (SimpleTrigger) trigger;
	}
	
	/**
	 * 
	 * @param name
	 * @param group
	 * @param startTime
	 * @param repeatingSecondsInterval
	 * @param repeatingNumber
	 * @return new Quartz SimpleTrigger
	 * 
	 * This method create Quartz SimpleTriggerSimpleTrigger <br>
	 * and sets its MissedJobsPolicy
	 */
	public static SimpleTrigger createSimpleTrigger(String name, String group,
			Date startTime, int repeatingSecondsInterval, int repeatingNumber,
			MissedScheduledPolicy policy){
		
		SimpleScheduleBuilder builder = simpleSchedule();
		setMissedJobPolicy(builder, policy);
		
		Trigger trigger = newTrigger() 
		.withIdentity(triggerKey(name, group))
		.withSchedule(builder
	    .withIntervalInSeconds(repeatingSecondsInterval)
	    .withRepeatCount(repeatingNumber))
		.startAt(startTime)
		.build();
		
		return (SimpleTrigger) trigger;
	}
	
	/**
	 * 
	 * @param name
	 * @param group
	 * @param startTime
	 * @param repeatingSecondsInterval
	 * @param repeatingNumber
	 * @return new Quartz SimpleTrigger
	 * 
	 * This method create Quartz SimpleTriggerSimpleTrigger <br>
	 * Please note this method register MisfireHandlingInstructionNextWithExistingCount to the trigger
	 */
	public static SimpleTrigger createSimpleTrigger(String name, String group,
			Date startTime, int repeatingSecondsInterval, int repeatingNumber){
		
		Trigger trigger = newTrigger() 
		.withIdentity(triggerKey(name, group))
		.withSchedule(simpleSchedule()
	    .withIntervalInSeconds(repeatingSecondsInterval)
	    .withRepeatCount(repeatingNumber)
	    .withMisfireHandlingInstructionNextWithExistingCount())
		.startAt(startTime)
		.build();
		
		return (SimpleTrigger) trigger;
	}
	
	/**
	 * 
	 * @param name
	 * @param group
	 * @param intervalTimer
	 * @param endTime
	 * @return SimpleTrigger
	 * 
	 * This method create simple trigger with intervalTimer <br>
	 * 
	 *  and sets its MissedJobsPolicy
	 */
	public static SimpleTrigger createSimpleTrigger(String name, String group,
			IntervalTimer intervalTimer, boolean endTime,
		MissedScheduledPolicy policy){

		SimpleScheduleBuilder builder = simpleSchedule();
		setMissedJobPolicy(builder, policy);	
	
		if (endTime){
			Trigger trigger = newTrigger() 
			.withIdentity(triggerKey(name, group))
			.withSchedule(builder
		    .withIntervalInSeconds(intervalTimer.getRepeatingSecondsInterval())
		    .withRepeatCount(intervalTimer.getRepeatingNumber()))
			.startAt(intervalTimer.getStartTimeAsDate())
			.endAt(intervalTimer.getEndTimeAsDate())
			.build();
			
			return (SimpleTrigger) trigger;
		}else{
			Trigger trigger = newTrigger() 
			.withIdentity(triggerKey(name, group))
			.withSchedule(builder
		    .withIntervalInSeconds(intervalTimer.getRepeatingSecondsInterval())
		    .withRepeatCount(intervalTimer.getRepeatingNumber()))
			.startAt(intervalTimer.getStartTimeAsDate())			
			.build();
			
			return (SimpleTrigger) trigger;
		}
	}
	
	/**
	 * 
	 * @param name
	 * @param group
	 * @param intervalTimer
	 * @param endTime
	 * @return SimpleTrigger
	 * 
	 * This method create simple trigger with intervalTimer 
	 */
	public static SimpleTrigger createSimpleTrigger(String name, String group,
			IntervalTimer intervalTimer, boolean endTime){
		
		MissedScheduledPolicy policy = intervalTimer.getMissedJobsPolicy();
		if (policy != null){
			createSimpleTrigger(name, group, intervalTimer, endTime, policy);
		}
		
		if (endTime){
			Trigger trigger = newTrigger() 
			.withIdentity(triggerKey(name, group))
			.withSchedule(simpleSchedule()
			.withMisfireHandlingInstructionNextWithExistingCount()
		    .withIntervalInSeconds(intervalTimer.getRepeatingSecondsInterval())
		    .withRepeatCount(intervalTimer.getRepeatingNumber()))
			.startAt(intervalTimer.getStartTimeAsDate())
			.endAt(intervalTimer.getEndTimeAsDate())
			.build();
			
			return (SimpleTrigger) trigger;
		}else{
			Trigger trigger = newTrigger() 
			.withIdentity(triggerKey(name, group))
			.withSchedule(simpleSchedule()
			.withMisfireHandlingInstructionNextWithExistingCount()
		    .withIntervalInSeconds(intervalTimer.getRepeatingSecondsInterval())
		    .withRepeatCount(intervalTimer.getRepeatingNumber()))
			.startAt(intervalTimer.getStartTimeAsDate())			
			.build();
			
			return (SimpleTrigger) trigger;
		}
	}
	
	/**
	 * 
	 * @param triggerName
	 * @param triggerGroup
	 * @param cronExpression
	 * @return Quartz CronTrigger
	 * 
	 * This method creates CronTrigger. but not relate it to a certain task. <br>
	 * The method also sets the MissedJobsPolicy to the cronTrigger
	 */
	public static CronTrigger createCroneTrigger(String triggerName, String triggerGroup,
			String cronExpression, MissedScheduledPolicy policy){
        CronScheduleBuilder builder = cronSchedule(cronExpression);
        setMissedJobPolicy(builder, policy);
        Trigger trigger = newTrigger()
        .withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(builder)
        .build();

        return (CronTrigger) trigger;
	}
	
	/**
	 * 
	 * @param triggerName
	 * @param triggerGroup
	 * @param cronExpression
	 * @return Quartz CronTrigger
	 * 
	 * This method creates CronTrigger. but not relate it to a certain task.
	 * 
	 * Please note that when task is misfire, the scheduler will fire the task first time it can 
	 */
	public static CronTrigger createCroneTrigger(String triggerName, String triggerGroup, String cronExpression){

        Trigger trigger = newTrigger()
        .withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(
                cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionFireAndProceed()
        )
        .build();

        return (CronTrigger) trigger;
	}
	
	/**
	 * 
	 * @param triggerName
	 * @param triggerGroup
	 * @param cronExpression
	 * @param JobName
	 * @param JobGroup
	 * @return CronTrigger
	 * 
	 * This method creates Quartz Cron Trigger  and relate it with a scheduled task using its name and group. <br>
	 * 
	 * This method sets the trigger with MissedJobsPolicy
	 */
	public static CronTrigger createCroneTrigger(String triggerName, String triggerGroup,
			String cronExpression, String JobName, String JobGroup, MissedScheduledPolicy policy){

        CronScheduleBuilder builder = cronSchedule(cronExpression);
        setMissedJobPolicy(builder, policy);

        Trigger trigger = newTrigger()
        .withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(builder)
        .forJob(JobName, JobGroup)
        .build();

        return (CronTrigger) trigger;
	}
	
	
	/**
	 * 
	 * @param triggerName
	 * @param triggerGroup
	 * @param cronExpression
	 * @param JobName
	 * @param JobGroup
	 * @return CronTrigger
	 * 
	 * This method creates Quartz Cron Trigger  and relate it with a scheduled task using its name and group.
	 * 
	 * Please note that when task is misfire, the scheduler will fire the task first time it can
	 */
	public static CronTrigger createCroneTrigger(String triggerName, String triggerGroup, String cronExpression, String JobName, String JobGroup){

        Trigger trigger = newTrigger()
        .withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(
                cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionFireAndProceed()
        )
        .forJob(JobName, JobGroup)
        .build();

        return (CronTrigger) trigger;
	}
	
	/**
	 * 
	 * @param timer
	 * @param policy
	 * 
	 * @return Trigger
	 * 
	 * This method create trigger from timer <br>
	 * and sets its MissedJobsPolicy
	 */
	public static Trigger createTrigger(Timer timer, MissedScheduledPolicy policy){
		if (policy == null){
			return createTrigger(timer);
		}
		String name = timer.getName();
		String group = timer.getGroup();
		
		if (timer instanceof IntervalTimer){
			IntervalTimer intervalTimer = (IntervalTimer) timer;
			Date endTime = intervalTimer.getEndTimeAsDate();
			boolean withEndTime = endTime != null; 
			return createSimpleTrigger(name, group, intervalTimer, withEndTime, policy);
		}else if(timer instanceof DateTimer){
			DateTimer dateTimer = (DateTimer) timer;
			return createCroneTrigger(name, group, dateTimer.getCroneExpression(), policy);
		}
		return null;
	}	
	
	/**
	 * 
	 * @param timer
	 * @return Trigger
	 * 
	 * This method create trigger from timer <br>
	 */
	public static Trigger createTrigger(Timer timer){
		MissedScheduledPolicy policy = timer.getMissedJobsPolicy();
		if (policy != null){
			createTrigger(timer, policy);
		}
		
		String name = timer.getName();
		String group = timer.getGroup();
		
		if (timer instanceof IntervalTimer){
			IntervalTimer intervalTimer = (IntervalTimer) timer;
			Date endTime = intervalTimer.getEndTimeAsDate();
			boolean withEndTime = endTime != null; 
			return createSimpleTrigger(name, group, intervalTimer, withEndTime);
		}else if(timer instanceof DateTimer){
			DateTimer dateTimer = (DateTimer) timer;
			return createCroneTrigger(name, group, dateTimer.getCroneExpression());
		}
		return null;
	}
	
}
