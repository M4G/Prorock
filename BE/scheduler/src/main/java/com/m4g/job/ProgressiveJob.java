package com.m4g.job;

public interface ProgressiveJob {
	
	/**
	 * 
	 * @return percent of progress
	 */
	public int getProgress();
	
	/**
	 * This method set the progress in percent <br>
	 * progress can be advanced up to 100%
	 */
	public void setProgress(int percent);
	
	/**
	 * 
	 * @return The amount value of task element complete 
	 */
	public int getAmountComplete();
	
	/**
	 * @param amount the number of completed task elements. 
	 * 
	 * sets the number of completed task  elements 
	 */
	public void setAmountComplete(int amount);
	
	/**
	 * 
	 * @return The total number of task element complete
	 */
	public int getAmountTotal();
	
	/**
	 * 
	 * @param total the sum number of all task elements
	 */
	public void setAmountTotal(int total);
	
	/**
	 * 
	 * @return Text base message describing the amount of progress;
	 */
	public String getProgressMessage();
	
	/**
	 * 
	 * @return Current step number;
	 */
	public int getStepNumber();
	
	/**
	 * 
	 * @param stepNumber the current step number.
	 */
	public void setStepNumber(int stepNumber);
	
}
