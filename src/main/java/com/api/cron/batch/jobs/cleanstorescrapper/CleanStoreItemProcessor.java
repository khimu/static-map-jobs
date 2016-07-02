package com.api.cron.batch.jobs.cleanstorescrapper;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.metadata.GoogleInfo;
import com.api.cron.batch.metadata.GoogleInfo.GoogleMetadataBuilder;
import com.api.cron.batch.model.Store;
import com.api.cron.batch.task.GoogleGeocodeTask;
import com.api.cron.batch.task.GoogleServiceFactory;

/**
 * Given an address, find the longitude and latitude of the business
 * 
 * storeDataJob
 * 
 * @author khimung
 *
 */
@Component("cleanStoreItemProcessor")
@Scope("step")
public class CleanStoreItemProcessor implements ItemProcessor<Store, Store> { 
	private final static Logger logger = Logger.getLogger(CleanStoreItemProcessor.class);
	
	@Value("${google.cron.access.key}")
	private String accessKey;
	
	@Value("${cron.job.google.places.url}")
	private String placesUrl;

	@Value("${cron.job.google.geocode.url}")
	private String geocodeUrl;

	@Value("#{jobParameters['city']}")
	private String city;
	
	@Value("#{jobParameters['state']}")
	private String state;
	
	@Resource
	private JobState jobState;
	
	@Resource
	private GoogleServiceFactory googleServiceFactory;

	@Override
	public Store process(Store item) throws Exception {
		logger.info("process item " + item.getName() + " " + item.getStoreId());
		jobState.setLastProcessedKey(item.getStoreId());
	
		if(StringUtils.trimToNull(item.getLongitude()) == null && StringUtils.trimToNull(item.getLatitude()) == null) {
			Map<String, String> geocodePlaceholder = new HashMap<String, String>();
			geocodePlaceholder.put("\\{address\\}", item.getAddressLine1() + " " + item.getAddressLine2() + ", " + item.getCity() + ", " + item.getState() + ", " + item.getZipcode() + ", " + item.getCountryCode());
	
			GoogleInfo geocode = new GoogleMetadataBuilder().setAccessKey(accessKey).setGoogleServiceEndpoint(geocodeUrl).setPlaceholder(geocodePlaceholder).execute();
			
			googleServiceFactory.getTask(GoogleGeocodeTask.class.getSimpleName()).execute(geocode);
			
			item.setLongitude(geocode.getResults().get("longitude"));
			item.setLatitude(geocode.getResults().get("latitude"));
	
			logger.info("ID " + item.getStoreId() + " longitude[" + item.getLongitude() + "] latitude[" + item.getLatitude() + "]");
			
			/*
			Map<String, String> placesPlaceholder = new HashMap<String, String>();
			placesPlaceholder.put("\\{longitude\\}", item.getLongitude());
			placesPlaceholder.put("\\{latitude\\}", item.getLatitude());
			placesPlaceholder.put("\\{radius\\}", "200");
			
			
			GoogleInfo places = new GoogleMetadataBuilder().setAccessKey(accessKey).setGoogleServiceEndpoint(placesUrl).setPlaceholder(placesPlaceholder).execute();
	
			
			googleServiceFactory.getTask(GooglePlacesTask.class.getSimpleName()).execute(places);
			*/
	
			// results here : places
		}
		else {
			jobState.incrementAlreadyExist();
		}
		
		logger.info("Done ");
		
		return item;
	}

}
