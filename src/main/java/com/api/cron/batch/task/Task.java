package com.api.cron.batch.task;

import com.api.cron.batch.metadata.TaskInfo;

/**
 * Each service has a list of task to execute and each task has a specific priority order
 * 
 * @author Ung
 *
 */
public interface Task {
	
	/*
	 * Each task has an operation to execute
	 */
	public void execute(TaskInfo... metadata) throws TaskException;
	
}
