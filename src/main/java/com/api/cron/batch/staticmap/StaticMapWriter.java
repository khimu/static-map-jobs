package com.api.cron.batch.staticmap;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.api.cron.batch.CronJobException;
import com.api.cron.batch.jobitems.BaseItem;
import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.jobitems.StaticMapItem;
import com.api.cron.batch.metadata.GoogleInfo;
import com.api.cron.batch.metadata.ImageInfo;
import com.api.cron.batch.metadata.S3Info;
import com.api.cron.batch.task.GoogleDownloadTask;
import com.api.cron.batch.task.GoogleServiceFactory;
import com.api.cron.batch.task.S3ResourceExistTask;
import com.api.cron.batch.task.S3UploadTask;

/**
 * Writer class for google static map.  Handles uploading a static map to 
 * S3 if it does not exist.
 * 
 * @author Ung
 *
 */
@Component("staticMapWriter")
@Scope("step")
public class StaticMapWriter implements ItemWriter<BaseItem> {
	
	private final static Logger logger = Logger.getLogger(StaticMapWriter.class);
	
	@Resource
	private JobState jobState;
	
	@Resource
	private S3Info s3Info;
	
	@Resource
	private GoogleServiceFactory googleServiceFactory;

	@Override
	public void write(List<? extends BaseItem> items) throws Exception {
		logger.debug("In writer with " + items.size());
		
		for(BaseItem item : items) {
			try {
				StaticMapItem staticMapItem = (StaticMapItem) item;
	
				ImageInfo imageMetadata = (ImageInfo) staticMapItem.getTaskInfo(ImageInfo.class.getSimpleName());
				GoogleInfo googleMetadata = (GoogleInfo) staticMapItem.getTaskInfo(GoogleInfo.class.getSimpleName());
	
	
				/*
				 * check if resource exist
				 */
				// TODO disable this until s3 credential is created and can be set in the xml configuration
				// googleServiceFactory.getTask(S3ResourceExistTask.class.getSimpleName()).execute(s3Info, imageMetadata);
				
	
				/*
				 * image does not exist in s3
				 */
				// TODO disable this until s3 credential is created and can be set in the xml configuration
				// if(imageMetadata.isExist() == false) {
				if(true) {
					logger.debug("Image does not exist");
					/*
					 * Download image from google service
					 */
					googleServiceFactory.getTask(GoogleDownloadTask.class.getSimpleName()).execute(googleMetadata, imageMetadata);
					/*
					 * upload the image
					 */
					// TODO disable this until s3 credential is created and can be set in the xml configuration
					//googleServiceFactory.getTask(S3UploadTask.class.getSimpleName()).execute(s3Info, imageMetadata);
					logger.info("Image Name: " + imageMetadata.getImageName());
					jobState.incrementSuccess();
					logger.debug("Total success " + jobState.getSuccess());					
				}
				else {
					logger.debug("Do nothing Image already exist in s3 server for " + staticMapItem.getStoreId());
					jobState.incrementAlreadyExist();
				}
				
				jobState.setLastProcessedKey(staticMapItem.getStoreId());
			}catch(Exception e) {
				jobState.incrementFailed();
				logger.error("Write Error: Failed " + jobState.getFailed() + " on " + jobState.getLastProcessedKey() + " due to " + e.getMessage());
			}
		}
		logger.info("Success: " + jobState.getSuccess() + " Failed: " + jobState.getFailed() + " Exist " + jobState.getAlreadyExist() + " for key " + jobState.getLastProcessedKey());
	}

}
