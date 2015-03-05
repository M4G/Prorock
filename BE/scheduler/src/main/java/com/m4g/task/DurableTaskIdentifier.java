package com.m4g.task;

/**
 * 
 * @author aharon_br
 *
 * This TaskIdentifier is used with DurableJobAdapter 
 */
public interface DurableTaskIdentifier extends TaskIdentifier {

	public Integer getTaskTypeId();
	
	public void setTaskTypeId(Integer taskTypeId);
	
	public Integer getTaskInstanceId();
	
	public void setTaskInstanceId(Integer taskInstanceId);
	
	public void updateWaitingInstanceName(String instanceName);
}
