package com.m4g.task.entities;


import com.coral.project.facilities.utils.ClobUtils;
import com.coral.project.facilities.utils.TasksUtils;
import com.coral.utils.SF;
import com.coral.utils.functions.DateFunctions;
import com.m4g.task.TaskStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 
 * @author aharon_br
 * This class is responsible to document task execution instance
 */

@Entity
@Table(name = "TASK_INSTANCE")
@SequenceGenerator(name = "TASK_INSTANCE_SEQ_GEN", sequenceName = "TASK_INSTANCE_SEQ", allocationSize = 1)
@NamedQueries({
		@NamedQuery(name = "TaskInstanceEntity.getMaxTaskId", query = "select max(tie.taskInstanceId) from TaskInstanceEntity tie ")})
public class TaskInstanceEntity extends TrackableEntity implements Serializable {

	private static final long serialVersionUID = -4231029464155254252L;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="TASK_ID", nullable = false, updatable = false)
	private TaskTypeEntity taskTypeEntity;
	
	@Column(name = "TASK_ID", insertable = false, updatable = false, nullable = false)
	private Integer taskId;

	@Column(name = "PHISYCAL_NAME", length = 200, nullable = false)
	private String phisycalName;

	@Column(name = "START_DATE", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;
	
	@Column(name = "END_DATE", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;
	
	@Lob
	@Column(name = "PARMS" )	
	private String params;
	
	@Transient
	public Properties paramaters = new Properties();	
	
	@Transient
	private String status;
	
	@Column(name = "STATUS_CD" , nullable=false ,precision = 4)
	private Integer statusCD;
	
	@Column(name = "TASK_USER", nullable = false, length = 100)
	private String user;
	
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TASK_INSTANCE_SEQ_GEN")
	@Column(name = "TASK_INSTANCE_ID", nullable = false)	
	private Integer taskInstanceId;
	
	@Column(name = "PROGRESS", updatable = true, nullable = true)
	private Integer progress;
	
	@Column(name = "AMOUNT", updatable = true, nullable = true)
	private Integer amount;
	
	@Column(name = "MESSAGE", nullable = true, length = 2000)
	private String message;
	
	@Column(name = "DESCRIPTION", nullable = true, length = 2000)
	private String description;
	
	@Transient
	private boolean canceled = false;
	
	public Integer getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(Integer taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isCanceled() {
		return canceled;
	}

	//TaskInstanceEntity cannot be resumed by UI
	public void setCanceled(boolean canceled) {
		if (this.canceled){
			return;
		}
		this.canceled = canceled;
	}

	public TaskInstanceEntity(){
		super();
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public String getStartDateStr() {
		return DateFunctions.getFormatedDate(DateFunctions.DDMMYY_HHmm_FORMAT, startDate);
	}
	
	public void setStartDateStr(String startDate) throws ParseException {
		if(!SF.isEmpty(startDate)){
			DateFormat dateFormat = new SimpleDateFormat(DateFunctions.DEFAULT_FORMAT);
			this.startDate = dateFormat.parse(startDate);
		}else{
			this.startDate = null;
		}
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getEndDateStr() {
		return DateFunctions.getFormatedDate(DateFunctions.DDMMYY_HHmm_FORMAT, endDate);
	}
	
	public void setEndDateStr(String endDate) throws ParseException {
		if(!SF.isEmpty(endDate)){
			DateFormat dateFormat = new SimpleDateFormat(DateFunctions.DEFAULT_FORMAT);
			this.endDate = dateFormat.parse(endDate);
		}else{
			this.endDate = null;
		}
	}
	
	public TaskStatus getTaskStatus(){
		return TaskStatus.getTaskStatus(statusCD);
	}
	
	public void setTaskStatus(TaskStatus taskStatus){
		this.status = taskStatus.toString();
		this.statusCD = taskStatus.getValue();
	}
	
	public String getPhisycalName() {
		return phisycalName;
	}

	public void setPhisycalName(String phisycalName) {
		this.phisycalName = phisycalName;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public TaskTypeEntity getTaskTypeEntity() {
		return taskTypeEntity;
	}

	public void setTaskTypeEntity(TaskTypeEntity taskTypeEntity) {
		this.taskTypeEntity = taskTypeEntity;
	}

	public String getParams() {
		if (params == null && paramaters != null){
			params = ClobUtils.createClob(paramaters);
		}
		return params;
	}

	public void setParams(String params) {
		this.params = params;
		if (params != null){
			this.paramaters = ClobUtils.parseClob(params);
		}
	}

	public Properties getParamaters() {
		if (paramaters.isEmpty() && params != null){
			paramaters = ClobUtils.parseClob(params);
		}
		return paramaters;
	}

	public void setParamaters(Properties paramaters) {
		this.paramaters = paramaters;
		params = ClobUtils.createClob(paramaters);
	}
	
	public String getStatus() {
		if (status == null && statusCD != null){
			status = TaskStatus.getTaskStatus(statusCD).toString();
		}
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	
	
	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getStatusCD() {
		return statusCD;
	}

	public void setStatusCD(Integer statusCD) {
		this.statusCD = statusCD;
		this.status = TaskStatus.getTaskStatus(statusCD).toString();
	}

	// use this method to add parameter instead of getParameters.put(key, value) etc ...
	public void addParameter(String key, Object value){
		paramaters.put(key, value);
		params = ClobUtils.createClob(paramaters);
	}
	
	
	public TaskInstanceEntity cloneTaskInstanceEntity(){
		TaskInstanceEntity newEntity = new TaskInstanceEntity();
		newEntity.setAmount(this.getAmount());
		newEntity.setCanceled(this.isCanceled());
		newEntity.setEndDate(this.getEndDate());
		newEntity.setInsertDate(this.getInsertDate());
		newEntity.setInsertUser(this.getInsertUser());
		newEntity.setMessage(this.getMessage());
		newEntity.setPageUrl(this.getPageUrl());
		newEntity.setParamaters(this.getParamaters());
		newEntity.setParams(this.getParams());
		newEntity.setPhisycalName(TasksUtils.cloneTimeOrientedUniquName(this.getPhisycalName()));
		newEntity.setProgress(this.getProgress());
		newEntity.setStartDate(this.getStartDate());
		newEntity.setTaskId(this.getTaskId());
		TaskStatus thisStatus = this.getTaskStatus();
		if (thisStatus == TaskStatus.CANCELED || thisStatus == TaskStatus.CANCELED_BY_USER){
			newEntity.setTaskStatus(thisStatus);
		}else{
			newEntity.setTaskStatus(TaskStatus.PENDING);
		}
				
		newEntity.setTaskTypeEntity(getTaskTypeEntity());
		newEntity.setUpdateDate(this.getUpdateDate());
		newEntity.setUpdateUser(this.getUpdateUser());
		newEntity.setUser(this.getUser());
		return newEntity;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
