package com.api.cron.batch.jobs.yellowpages;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.api.cron.batch.jobitems.BaseItem;
import com.api.cron.batch.jobitems.BusinessItem;
import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.model.Store;
import com.api.cron.batch.task.TaskException;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;

/**
 * ALERT: This is not used due to only need to insert directly to DB
 * 
 * 
 * retrieve store by name
 * retrieve store category by category store id
 * 
 * if duplicate log and skip
 * 
 * retrieve business location with geocode
 * retrieve business data with google places API
 * 
 * @author Ung
 *
 */
@Component("businessDataItemWriter")
@Scope("step")
@Transactional(value="transactionManager", timeout=60000, isolation=Isolation.READ_COMMITTED)
public class BusinessDataItemWriter implements ItemWriter<BaseItem>{
	private final static Logger logger = Logger.getLogger(BusinessDataItemWriter.class);
	
	@Resource(name = "insertStoreItemWriter")
	private ItemWriter<Store> insertStoreItemWriter;
	
	@Resource
	private JobState jobState;
	

	@Override
	public void write(List<? extends BaseItem> items) throws Exception {
		logger.info("items to write " + items.size());

		List<Store> stores = new ArrayList<Store>();
		
		for(int i = 0; i < items.size(); i ++) {
			BusinessItem item = ((BusinessItem)items.get(i));
			stores.addAll(item.getStores());
		}
		
		try {
			logger.info("Inserting stores to db " + stores.size());
			insertStoreItemWriter.write(stores);
			
			synchronized(jobState) {
				for(int i = 0; i < items.size(); i ++) {
					jobState.incrementSuccess();
				}
			}
		}catch(Exception e) {
			synchronized(jobState) {
				for(int i = 0; i < items.size(); i ++) {
					jobState.incrementFailed();
				}
			}
			throw new TaskException("Unexpected error skipping " + e.getMessage());
		}
		
		logger.info("Done");
	}
}
