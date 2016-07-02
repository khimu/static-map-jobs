package com.api.cron.batch;

public interface BatchJob {

	public void execute() throws CronJobException;
	
}
