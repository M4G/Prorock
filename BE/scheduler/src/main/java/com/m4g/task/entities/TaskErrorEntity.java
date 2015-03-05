package com.m4g.task.entities;

import com.coral.project.facilities.scheduler.ErrorSeverity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "TASK_ERROR")
@SequenceGenerator(name = "TASK_ERROR_SEQ_GEN", sequenceName = "TASK_ERROR_SEQ", allocationSize = 1)
public class TaskErrorEntity extends TrackableEntity implements Serializable{

	private static final long serialVersionUID = -5515062158568149135L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TASK_ERROR_SEQ_GEN")
	@Column(name = "TASK_ERROR_ID", nullable = false, updatable = false)
	private Integer taskErrorId;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="TASK_INSTANCE_ID", nullable = false, updatable = false)	
	private TaskInstanceEntity taskInstanceEntity;
	
	@Column(name = "TASK_INSTANCE_ID", insertable = false, updatable = false, nullable = false)
	private Integer taskInstanceId;
	
	@Column(name = "MESSAGE" )
	private String message;
	
	@Transient
	private String severity;
	
	@Column(name = "SEVERITY_CD" , nullable=false, precision = 4)
	private Integer severityCD;
	
	@Lob
	@Column(name = "STACK_TRACE" )	
	private String stackTrace;

	public Integer getTaskErrorId() {
		return taskErrorId;
	}

	public void setTaskErrorId(Integer taskErrorId) {
		this.taskErrorId = taskErrorId;
	}

	public TaskInstanceEntity getTaskInstanceEntity() {
		return taskInstanceEntity;
	}

	public void setTaskInstanceEntity(TaskInstanceEntity taskInstanceEntity) {
		this.taskInstanceEntity = taskInstanceEntity;
	}

	public Integer getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(Integer taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSeverity() {
		if (severity == null && severityCD != null){
			severity = ErrorSeverity.getErrorSeverity(severityCD).toString();
		}
		return severity;
	}

	public void setSeverity(ErrorSeverity severity) {
		this.severity = severity.toString();
		severityCD = severity.getValue();
	}

	public Integer getSeverityCD() {
		return severityCD;
	}

	public void setSeverityCD(Integer severityCD) {
		this.severityCD = severityCD;
		this.severity = ErrorSeverity.getErrorSeverity(severityCD).toString();
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
