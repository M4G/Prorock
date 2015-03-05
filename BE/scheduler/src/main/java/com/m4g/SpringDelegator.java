package com.m4g; /**
 * 
 */

import com.google.common.base.Preconditions;
import com.m4g.job.LifeCycleAware;
import com.m4g.task.TaskContextAware;
import com.m4g.task.TaskContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;

/**
 * @author aharon_br
 * 
 */
public class SpringDelegator implements Runnable, Serializable,
		ApplicationContextAware, TaskContextAware, LifeCycleAware{

	private static final long serialVersionUID = -4218710339407881505L;

	private String beanName;

	private transient ApplicationContext ctx;
	protected TaskContext taskContext;
	protected Object bean;

	public SpringDelegator(String beanName) throws IllegalArgumentException {
		Preconditions.checkNotNull(beanName, "Bean name cannot be null.");
		this.beanName = beanName;
	}
	
	protected void init(){
		bean = ctx.getBean(getBeanName());
		if (bean == null) {
			throw new IllegalStateException("Unknown spring bean: " + getBeanName());
		}

		if (!(bean instanceof Runnable)) {
			throw new IllegalStateException("Bean '" + getBeanName()
					+ " is not of type java.lang.Runnable.");
		}
	}

	@Override
	public void run() {
		((Runnable) bean).run();
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.ctx = ctx;
		init();
	}

	@Override
	public void setTaskContext(TaskContext taskContext) {
		this.taskContext = taskContext;
		if (bean instanceof TaskContextAware){
			( (TaskContextAware) bean).setTaskContext(taskContext);
		}
	}

	@Override
	public boolean hasTaskContext() {
		if (bean != null && bean instanceof TaskContextAware){
			return ((TaskContextAware) bean).hasTaskContext();
		}
		return false;
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		if (bean == null){
			this.bean = bean;
		}
	}

	@Override
	public void beforeExecution() {
		if (bean instanceof LifeCycleAware){
			( (LifeCycleAware) bean).beforeExecution();
		}
	}

	@Override
	public void afterExecution() {
		if (bean instanceof LifeCycleAware){
			( (LifeCycleAware) bean).afterExecution();
		}
	}

	public String getBeanName() {
		return beanName;
	}

	public ApplicationContext getCtx() {
		return ctx;
	}

	
}
