package com.m4g.job;

/**
 * 
 * @author aharon_br
 * This interface is used to mark (tag) runnable as a <br>
 * Critical job which indicating that if a job misfire situation occurs <br>
 * The SchedulerService must activate the job at highest priority   
 */
public interface CriticalJob {
}
