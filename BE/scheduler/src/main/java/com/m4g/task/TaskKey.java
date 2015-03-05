package com.m4g.task;

import com.google.common.base.Strings;

import java.io.Serializable;

import static com.m4g.SchedulerConstants.*;

/**
 * 
 * @author aharon_br
 * This class encapsulate the most common task key components <br>
 * name - mandatory <br>
 * group - optional but highly recommended to set. <br>
 * user - optional but highly recommended to set. <br>
 */
public class TaskKey implements Serializable{

	private static final long serialVersionUID = -4041212352419305605L;
	
	private String name;
	private String group;
	private String user;
	
	/** Default constructor */
	public TaskKey(){
	}
	
	public TaskKey(String name, String group, String user){
		this.name = name;
		this.group = group;
		this.user = user;
	}
	
	public TaskKey(String name, String group){
		this.name = name;
		this.group = group;
	
	}
	/**
	 * 
	 * @return name of the task;
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return group of the task or <br>
	 * default group if group is empty
	 */
	public String getGroup() {
		if (Strings.isNullOrEmpty(group) ){
			return DEFAULT_GROUP;
		}
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	
	/**
	 * @return user associated with the task or <br>
	 * default user if user is empty
	 */
	public String getUser() {
		if (Strings.isNullOrEmpty(user)){
			return DEFAULT_USER;
		}
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
}
