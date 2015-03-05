package com.m4g.task;


import com.m4g.SpringDelegator;

import java.util.Date;


public class TasksUtils {
	
	/**
	 * This method omit all package related prefix  
	 * @param className 
	 * @return formatted className
	 */
	public static String formatClassName(String className){
		int dotPosition = className.lastIndexOf('.');
		if (dotPosition > -1){
			return className.substring(dotPosition+1);
		}
		return className;
	}
	
	public static String createTimeOrientedUniquName(String name){
		Date current = new Date();
		long miliSeconds = current.getTime();
		String uniqueName = name + ":" + miliSeconds;
		return uniqueName;
	}
	
	public static String createTimeOrientedClassName(Runnable runnable){
		String className = ((SpringDelegator)runnable).getBeanName();
		//String className = runnable.getClass().getName();
	//	className = formatClassName(className);
		return createTimeOrientedUniquName(className);
	}
	
	public static String createFormattedClassName(Object runnable){
		String className = runnable.getClass().getName();
		className = formatClassName(className);
		return className;
	}
	
	public static String cloneTimeOrientedUniquName(String name){
		int idx = name.indexOf(':');
		String className = name.substring(0, idx);
		Date current = new Date();
		long miliSeconds = current.getTime();
		String uniqueName = className + ":" + miliSeconds;
		return uniqueName;
	}
	
	
	
}
