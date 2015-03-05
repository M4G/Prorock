package com.m4g.timers;

/**
 * 
 * @author aharon_br
 * This is the base interface for all timers like: <br> interval timer , date timer etc...
 */
public interface Timer {
	
	public String getName();
	
	public void setName(String name);
	
	public String getGroup();
	
	public void setGroup(String Group);
	
	public MissedScheduledPolicy getMissedJobsPolicy();
	
	public void setMissedJobsPolicy(MissedScheduledPolicy policy);
}
