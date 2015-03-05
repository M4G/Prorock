package com.m4g;

public enum ErrorSeverity {
	WARNING(1),
	ERROR(2),
	FATAL_ERROR(3);
	
	private int value;
	
	ErrorSeverity(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	static public ErrorSeverity getErrorSeverity(int value){
		for (ErrorSeverity severity: ErrorSeverity.values()){
			if (value == severity.getValue()){
				return severity;
			}
		}
		return null;
	}
	
	static public ErrorSeverity getErrorSeverity(String severity){
		return ErrorSeverity.valueOf(severity);
	}
}
