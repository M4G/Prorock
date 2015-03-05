package com.m4g;

import com.m4g.task.TaskContext;
import com.m4g.task.TaskContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Transactional
@Component("runnableFileToDB")
public class TestableTask implements Runnable, Serializable, TaskContextAware{

	private TaskContext taskContext;

	@Override
	public void setTaskContext(TaskContext taskContext) {
		this.taskContext = taskContext;
	}

	@Override
	public boolean hasTaskContext() {
		return taskContext != null;
	}

	@Override
	public void run() {
		try{
			taskContext.setProgress(100);
			taskContext.setMessage("Complete proccessing file");
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			taskContext.setMessage("problem while proccesing file ");
			taskContext.setError("problem while proccesing file ",e.getMessage(), ErrorSeverity.ERROR);
		}
	}
}
