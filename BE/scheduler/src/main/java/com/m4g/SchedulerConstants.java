package com.m4g;

public class SchedulerConstants {

	public static final String APPLICATION_CONTEXT_KEY = "SPRING_APPLICATION_CONTEXT";
	
	public static final String TASK_INSTANCE_ID = "TaskInstanceID";
	
	public static final String RUNNABLE_KEY = "runnableKey";
	
	public static final String SERVICE_KEY = "serviceKey";
	
	public static final String TASKKEY_KEY = "taskKey_key";
	
	public static final String DEFAULT_USER = "defaultUser";
	
	public static final String DEFAULT_GROUP = "defaultGroup";
	
	public static final boolean RECOVERY_MODE_FALSE = false;
	
	public static final boolean RECOVERY_MODE_TRUE = true;
	
	public static final String INSTANCE_MAP_VALUE_KEY = "instanceMapValueKey";
	
	public static final String BEAN_NAME_KEY = "beanNameKey";
	
	public static final String DURABLE_JOB_ADAPTER = "durableJobAdapter";
	
	public static final String RECOVERABLE_JOB_ADAPTER = "recoverableJobAdapter";
	
	public static final String RECOVERABLE_PERSISTENT_JOB_ADAPTER = "recoverablePersistentJobAdapter ";
	
	public static final String WAITING_TASKCONTROLLER_LIST_KEY = "waiting_taskcontroller_list_key";
	
	public static final String TASK_FACADE_KEY = "taskFacadeKey";
	
	public static final String TASK_FACADE_BEAN = "taskFacade";
	
	public static final String TASK_ID_KEY = "taskIdKey";
	
	public static final String TASK_TYPE_PARAMS_KEY = "taskTypeKey";
	
	public static final String TASK_INSTANCE_PARAMS_KEY = "taskInstanceParamsKey";
	
	public static final String TASK_PARAM_PREFIX = "task_param_";
	
	public static final String TASK_INSTANCE_ID_KEY = "taskInstanceId";
	
	public static final String TASK_TYPE_DAO_BEAN = "taskTypeDao";
	
	public static final String TASK_INSTANCE_DAO_BEAN = "taskInstanceDao";
	
	public static final String TASK_ERROR_DAO_BEAN = "taskErrorDao";
	
	public static final String EMPTY_WAITING_PREFIX = "waiting_";
	
	public static final String TASK_INSTANCES_IDS_LIST_KEY  = "taskInstancesListIds_key";
	
	
	// ***********************  QUERIES  *******************
	
	public static final String FROM_TASK_TYPE_ENTITY_QUERY = "FROM TaskTypeEntity";
	
	public static final String FROM_TASK_TYPE_ENTITY_WHERE_NAME_AND_GROUP_QUERY = "FROM TaskTypeEntity where name = ? and group = ?";
	
	public static final String FROM_TASK_INSTANCE_ENTITY_WHERE_TASK_ID_AND_STATUS_QUERY = "FROM TaskInstanceEntity where taskId = ? and status = ?";
	
	public static final String FROM_TASK_INSTANCE_ENTITY_WHERE_NAME_AND_GROUP_AND_STATUS_QUERY = "FROM TaskInstanceEntity where name = ? and group = ? and status = ?";
	
	public static final String FROM_TASK_INSTANCE_ENTITY_WHERE_STATUS_AND_GROUP_QUERY = "FROM TaskInstanceEntity where status = ? and group = ?";
	
	public static final String FROM_TASK_INSTANCE_ENTITY_WHERE_STATUS_QUERY = "FROM TaskInstanceEntity where status = ?";
	
	public static final String FROM_TASK_INSTANCE_ENTITY_WHERE_TASK_INSTANCE_ID_QUERY = "FROM TaskInstanceEntity where taskInstanceId = ?";
	
	public static final String SQL_DELETE_QUERY = "DELETE FROM TASK_INSTANCE WHERE TASK_INSTANCE_ID = ";
	
	public static final String FROM_TASK_INSTANCE_ENTITY_WHERE_PHISYCAL_NAME = "FROM TaskInstanceEntity where phisycalName = ?";

	// ***********************  Legger messages  *******************
	
	public static final String JOB_CANCELD_BY_USER = "Job canceld by user";
	
	public static final String UNABLE_TO_PAUSE_JOB = "Unable to pause job";
	
	public static final String EXCEPTION_OCCURS_WHILE_PAUSE_JOB = "Exception occurs while attempting to pause job";

	public static final String JOB_CANCELD_BY_USER_RUNNABLE = "Job canceld by user :";

	public static final String EXCEPTION_WHILE_EXECUTING = "Exception while executing";

	public static final String EXCEPTION_HAS_OCCURS_WHILE_JOB_EXECUTING = "Exception has occurs while Job executing :";	
	
}
