package com.m4g.job;

import com.m4g.exceptions.JobInterruptedException;

public interface InteruptableJob {
	
	/**
	 * 
	 * @throws JobInterruptedException
	 * 
	 * This method is used for "getting out" of job execution <br>
	 * No catch clause for JobInterruptedException should be surrounding this method call. <br>
	 * If general catch (i.e. catch(Exception e) ) surrounds this method call <br> 
	 * at the end of it a throw new JobInterruptedException() should be.
	 */
	public void iteruptExecution() throws JobInterruptedException;
	
	public void setCancel(boolean cancel);
	
	public boolean isCanceled();
}
