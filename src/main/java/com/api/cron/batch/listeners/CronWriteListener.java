package com.api.cron.batch.listeners;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.api.cron.batch.CronJobException;
import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.model.BaseModel;
import com.api.cron.batch.task.TaskException;

/**
 * 
 * 
 * @author Ung
 *
 */
@Component("cronWriteListener")
@Scope("step")
public class CronWriteListener implements ItemWriteListener<BaseModel> {
	private final static Logger logger = Logger.getLogger(CronWriteListener.class);
	
	@Resource
	private JobState jobState;

	
	@Override
	public void beforeWrite(List<? extends BaseModel> items) {
		logger.debug("ItemWriteListener - beforeWrite item count " + items.size());
	}

	
	
	@Override
	public void afterWrite(List<? extends BaseModel> items) {
		logger.debug("ItemWriteListener - afterWrite item count " + items.size());
	}

	
	
	@Override
	public void onWriteError(Exception exception, List<? extends BaseModel> items) {
		logger.debug("ItemWriteListener - onWriteError " + exception.getMessage());
		
		if((exception instanceof TaskException) == false) {
			/*
			 * only skip tasklet exceptions
			 */
			exception.printStackTrace();
			
			throw new CronJobException(exception.getMessage());
		}

	}

}
