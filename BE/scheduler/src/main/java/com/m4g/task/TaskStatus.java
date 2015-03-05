package com.m4g.task;

/**
 * @author aharon_br
 *
 * This enum describes all possible statuses of job
 */
public enum TaskStatus {
	WAITING(1), // task not started nor in process of starting
	PENDING(2), // task is about to start
	STARTED(3), 
	RUNNING(4),
	AFTER_RUN(5),
	COMPLETED(6),
	ERROR(7),
	COMPLETED_WITH_WARNING(8),
	COMPLETED_WITH_PROBLEM(9),
	CANCELED(10),
	CANCELED_BY_USER(11);
	
	private int value;
	
	TaskStatus(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	static public TaskStatus getTaskStatus(int value){
		for (TaskStatus status: TaskStatus.values()){
			if (value == status.getValue()){
				return status;
			}
		}
		return null;
	}
	
	static public TaskStatus getTaskStatus(String status){
		return TaskStatus.valueOf(status);
	}
	
	
	
}
