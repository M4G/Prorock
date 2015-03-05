package com.m4g.timers;

public class GeneralTablesTimerImpl implements GeneralTabelsTimer{
	
	private String name;
	private String group;
	private String croneExpression;
	private MissedScheduledPolicy policy = MissedScheduledPolicy.EXECUTE_NOW_ONCE;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public void setGroup(String Group) {
		this.group = Group;
	}

	@Override
	public MissedScheduledPolicy getMissedJobsPolicy() {
		return policy;
	}

	@Override
	public void setMissedJobsPolicy(MissedScheduledPolicy policy) {
		this.policy = policy;
	}

	@Override
	public String getCroneExpression() {
		return croneExpression;
	}

	@Override
	public void setCroneExpression(String croneExpression) {
		this.croneExpression = croneExpression;
	}

}
