package com.m4g.timers;

/**
 * 
 * @author aharon_br
 *
 * This class usage is to describe
 * date oriented  timer using  chroneExpression
 */
public class DateTimerImpl implements DateTimer{

	private String name;
	private String group;
	private String croneExpression;
	private MissedScheduledPolicy policy = MissedScheduledPolicy.EXECUTE_NOW_ONCE;
	
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

	public String getCroneExpression() {
		return croneExpression;
	}

	public void setCroneExpression(String croneExpression) {
		this.croneExpression = croneExpression;
	}

	@Override
	public MissedScheduledPolicy getMissedJobsPolicy() {
		return policy;
	}

	@Override
	public void setMissedJobsPolicy(MissedScheduledPolicy policy) {
		this.policy = policy;
	}

}
