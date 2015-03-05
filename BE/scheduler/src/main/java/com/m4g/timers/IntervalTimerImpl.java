package com.m4g.timers;

import java.util.Date;

/**
 * 
 * @author aharon_br
 *
 * This interface usage is to describe
 * interval oriented  timer that used to
 * scheduled a task.   
 */
public class IntervalTimerImpl implements IntervalTimer{
	
	private String name;
	private String group;
	private long startTime;
	private long endTime;
	private int repeatingSecondsInterval;
	private int repeatingNumber;
	private MissedScheduledPolicy policy = MissedScheduledPolicy.EXECUTE_NOW_WITH_EXISITING_COUNT;
	
	private int numberOfExecution = 0;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumberOfExecution() {
		return numberOfExecution;
	}

	public void setNumberOfExecution(int numberOfExecution) {
		this.numberOfExecution = numberOfExecution;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setStartTime(long startDateTime) {
		this.startTime = startDateTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public void setEndTime(long endDateTime) {
		this.endTime = endDateTime;
	}
	
	public Date getStartTimeAsDate(){
		return new Date(startTime);
	}
	
	public void setStartTimeFromDate(Date date){
		startTime = date.getTime();		
	}
	
	/**
	 * 
	 * @return Date if the endTime is greater than 0 <br> else it return null 
	 */
	public Date getEndTimeAsDate(){
		if (endTime >0){
			return new Date(endTime);
		}
		return null;
	}
	
	public void setEndTimeFromDate(Date date){
		endTime = date.getTime();		
	}
	
	public int getRepeatingNumber() {
		return repeatingNumber;
	}
	
	public void setRepeatingNumber(int repeatingNumber) {
		this.repeatingNumber = repeatingNumber;
	}

	public int getRepeatingSecondsInterval() {
		return repeatingSecondsInterval;
	}

	public void setRepeatingSecondsInterval(int repeatingSecondsInterval) {
		this.repeatingSecondsInterval = repeatingSecondsInterval;
	}

	@Override
	public MissedScheduledPolicy getMissedJobsPolicy() {
		return policy;
	}

	@Override
	public void setMissedJobsPolicy(MissedScheduledPolicy policy) {
		this.policy = policy;
	}

}
