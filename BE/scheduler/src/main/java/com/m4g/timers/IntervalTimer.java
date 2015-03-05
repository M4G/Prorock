package com.m4g.timers;

import java.util.Date;

public interface IntervalTimer extends Timer {
	public long getStartTime();
	
	public void setStartTime(long startDateTime);
	
	public long getEndTime();
	
	public void setEndTime(long endDateTime);
	
	public Date getStartTimeAsDate();
	
	public void setStartTimeFromDate(Date date);
	
	public Date getEndTimeAsDate();
	
	public void setEndTimeFromDate(Date date);
	
	public int getRepeatingNumber();
	
	public void setRepeatingNumber(int repeatingNumber);

	public int getRepeatingSecondsInterval();

	public void setRepeatingSecondsInterval(int repeatingSecondsInterval);
	
	public int getNumberOfExecution();
	
	public void setNumberOfExecution(int numberOfExecution);
}
