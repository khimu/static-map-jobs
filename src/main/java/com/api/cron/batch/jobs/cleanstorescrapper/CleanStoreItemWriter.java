package com.api.cron.batch.jobs.cleanstorescrapper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.model.BaseModel;
import com.api.cron.batch.model.Store;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


/**
 * storeDataJob
 * 
 * retrieve store by name
 * retrieve store category by category store id
 * 
 * if duplicate log and skip
 * 
 * retrieve business location with geocode
 * retrieve business data with google places API
 * 
 * @author khimung
 *
 */
@Component("cleanStoreItemWriter")
@Scope("step")
public class CleanStoreItemWriter implements ItemWriter<BaseModel>{
	private final static Logger logger = Logger.getLogger(CleanStoreItemWriter.class);
	
	@Resource
	private MongoTemplate mongoTemplate;
	
	@Resource(name = "storeUpdateItemWriter")
	private ItemWriter<Store> storeUpdateItemWriter;
	
	private final static int retry = 3;
	
	@Resource
	private JobState jobState;

	@Override
	public void write(List<? extends BaseModel> items) throws Exception {
		logger.info("items to write " + items.size());
		DBCollection collection = mongoTemplate.getCollection("bname");
		BulkWriteOperation bulk = collection.initializeOrderedBulkOperation();
		
		/**
		 * TODO 
		 * 
		 * Chagne this from update to insert
		 * 
		 */
		List<Store> stores = new ArrayList<Store>();
		for(int i = 0; i < items.size(); i ++) {
			Store store = ((Store)items.get(i));


			try {
				BasicDBObject searchObject = new BasicDBObject();
				searchObject.put("store_id", store.getStoreId());

				
				DBObject modifiedObject =new BasicDBObject();
				modifiedObject.put("$set", new BasicDBObject()
					.append("name", store.getName())
					.append("address_line_1", store.getAddressLine1())
					.append("address_line_2", store.getAddressLine2())
					.append("zipcode", store.getZipcode())
					.append("city", store.getCity())
					.append("state", store.getState())
					.append("phone", store.getPhoneNumber())
					.append("website", store.getWebsite())
					.append("email", store.getEmailAddress())
					.append("longitude", store.getLongitude())
					.append("latitude", store.getLatitude())
					.append("keywords", store.getKeyWords())
					.append("public_store_key", store.getPublicStoreKey())
				);
						
				bulk.find(searchObject).
				upsert().update(modifiedObject);
			}
			catch(Exception e) {
				logger.error("Mongo query failed " + e.getMessage());
			}
			
			stores.add(store);
		}
		
		
		
		
		/*
		 * Mongo
		 */
		boolean success = false;
		try {
			if(items.size() > 0) {
				BulkWriteResult writeResult = bulk.execute();
				success = true;
				logger.info("DONE with MONGODB UPSERT " + items.size());
			}
		}
		catch(Exception e) {
			logger.error("Failed to update to mongo " + e.getMessage());
		}
		
		
		
		
		/*
		 * DB
		 */
		success = false;
		try {
			storeUpdateItemWriter.write(stores);
			success = true;
		}catch(Exception e) {
			logger.error("Failed to update to db " + e.getMessage());
		}
		
		/*
		 * fail/success count on mongo only
		 */
		if(success == false) {
			for(int j = 0; j < items.size();j ++) {
				jobState.incrementFailed();
			}
		}
		else {
			for(int i = 0; i < items.size(); i ++) {
				jobState.incrementSuccess();
			}
		}
		logger.info("Done ");
	}
}
