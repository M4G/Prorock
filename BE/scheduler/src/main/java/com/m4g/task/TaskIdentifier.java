package com.m4g.task;

/**
 * 
 * @author aharon_br
 *
 * This identifier expose API for retrieving: <br>
 * logical name group and  "physical name" <br>
 * Please note that TaskIdentifier instance exist only within TaskController  
 */
public interface TaskIdentifier {
	
	/**
	 * 
	 * @return The logical name of the task. <br>
	 * The logical name is the name the user choosed for the task
	 */
	public String getTaskTypeName();
	
	/**
	 * 
	 * @return the group of the task
	 */
	public String getGroup();
	
	/**
	 * 
	 * @return the physical name of the task
	 */
	public String getTaskInstanceName();
	
	/**
	 * 
	 * @return The User associated with the task
	 */
	public String getUser();
	
	/**
	 * 
	 * @return the task instance id.
	 */
	public Integer getTaskInstanceId();
	
	/**
	 * 
	 * @return the task type id.
	 */
	public Integer getTaskTypeId();
	
}
