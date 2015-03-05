package com.m4g.task.entities;

import com.coral.project.facilities.scheduler.quartz.RunnableJobAdapter;
import com.coral.project.facilities.utils.ClobUtils;
import com.m4g.task.TaskKey;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Properties;

@Entity
@Table(name = "TASK_TYPE")
@SequenceGenerator(name = "TASK_TYPE_SEQ", sequenceName = "TASK_TYPE_SEQ", allocationSize = 1)
@NamedQueries({
		@NamedQuery(name = "TaskTypeEntity.findByTaskExcClass", 
				query = "select tt from TaskTypeEntity tt where tt.executionClass=:excClass")})


public class TaskTypeEntity extends TrackableEntity implements Serializable{
	
	/**
	 * serial Version unique ID
	 */
	private static final long serialVersionUID = 1955908878943101864L;
	
	@Id
	@GeneratedValue(generator = "TASK_TYPE_SEQ")
	@Column(name = "TASK_ID", nullable = false)
	private Integer taskId;
	
	@Column(name = "NAME", nullable = false, length=100)
	private String name;
	
	@Column(name = "GROUP_C", nullable = false, length=100)
	private String group;
	
	@Column(name = "DESCRIPTION", nullable = true)
	private String description;
	
	@Column(name = "CLASS_EXC", nullable = false)
	private String executionClass;

	@Column(name = "NOTIFICATION_CODE", nullable = true)
	private Integer notificationCode;
	
	@Lob
	@Column(name = "PARAMS", nullable = true)	
	private String params;
	
	@Column(name = "BASE_FOLDER", nullable = true)
	private String baseFolder;
	
	@Column(name = "RETRIES")
	private Integer retries;
	
	@Transient
	public Properties paramaters = new Properties();
	
	public TaskTypeEntity(){
		super();
	}
	
	public TaskTypeEntity(Runnable runnable, TaskKey taskKey){
		super();
		this.name  = taskKey.getName();
		this.group = taskKey.getGroup();
		this.executionClass = runnable.getClass().getName();
	}
	
	public TaskTypeEntity(RunnableJobAdapter jobAdapter){
		super();
		TaskKey taskKey = jobAdapter.getTaskKey();
		this.name  = taskKey.getName();
		this.group = taskKey.getGroup();
		//this.executionClass = jobAdapter.getRunnable().getClass().getName();
		this.executionClass = jobAdapter.getOriginalExecClassName();
		this.description = jobAdapter.getDescription();
	}
	
	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExecutionClass() {
		return executionClass;
	}

	public void setExecutionClass(String executionClass) {
		this.executionClass = executionClass;
	}

	public Integer getNotificationCode() {
		return notificationCode;
	}

	public void setNotificationCode(Integer notificationCode) {
		this.notificationCode = notificationCode;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
		if (params != null){
			this.paramaters = ClobUtils.parseClob(params);
		}
	}

	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public Properties getParamaters() {
		if (paramaters == null && params != null){
			paramaters = ClobUtils.parseClob(params);
		}
		return paramaters;
	}

	public void setParamaters(Properties paramaters) {
		this.paramaters = paramaters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((executionClass == null) ? 0 : executionClass.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskTypeEntity other = (TaskTypeEntity) obj;
		if (executionClass == null) {
			if (other.executionClass != null)
				return false;
		} else if (!executionClass.equals(other.executionClass))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		return true;
	}

	public Integer getRetries() {
		return retries;
	}

	public void setRetries(Integer retries) {
		this.retries = retries;
	}
}
