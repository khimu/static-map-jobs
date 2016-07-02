package com.api.cron.batch.task;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.api.cron.batch.metadata.GoogleInfo;
import com.api.cron.batch.metadata.ImageInfo;
import com.api.cron.batch.metadata.TaskInfo;

/**
 * Download static image from google
 * 
 * @author Ung
 *
 */
public class GoogleDownloadTask implements Task {
	private final static Logger logger = Logger.getLogger(GoogleDownloadTask.class);

	@Resource
	private RestTemplate restTemplate;
	
	@Resource
	private MongoTemplate mongoTemplate;
	
	private int order;

	/*
	 * Requires GoogleMetadata and ImageMetadata in that respective order
	 * 
	 * (non-Javadoc)
	 * @see com.dummy.batch.task.Task#execute(com.dummy.batch.metadata.TaskMetadata[])
	 */
	@Override
	public void execute(TaskInfo... metadata) throws TaskException {
		GoogleInfo googleMetadata = (GoogleInfo) metadata[0];
		ImageInfo imageMetadata = (ImageInfo) metadata[1];

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
	
			HttpEntity<String> request = new HttpEntity<String>(headers);
	
			HttpEntity<byte[]> response = restTemplate.exchange(googleMetadata.getServiceEndpoint(), HttpMethod.GET, request, byte[].class);
	
			byte[] resultString = response.getBody();
	
			imageMetadata.setImageByte(resultString);
			
			Update update = new Update();
			update.set("filename", imageMetadata.getImageName());
			update.set("bytes", resultString);
			
			Query query = new Query();
			query.addCriteria(Criteria.where("store_id").is(imageMetadata.getBusinessId()));
			
			mongoTemplate.upsert(query, update, "bname");
			
			logger.debug("Download file successful " + googleMetadata.getServiceEndpoint());
		}
		catch(Exception e) {
			logger.error("Unable to download " + e.getMessage());
			throw new TaskException("Unable to download file [" + googleMetadata.getServiceEndpoint() + "] due to " + e.getMessage());
		}
	}


}
