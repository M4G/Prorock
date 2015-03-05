package com.m4g.exceptions;


/**
 * 
 * @author aharon_br
 * This exception is ( used ) thrown when an InteruptableJob <br>
 * need to cancel Job in an executing stage
 */
public class JobInterruptedException extends Exception {
	
	public JobInterruptedException(){
		super();
	}
	
	public JobInterruptedException (String message){
		super(message);
	}
}
