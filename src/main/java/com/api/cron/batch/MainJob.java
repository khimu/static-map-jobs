package com.api.cron.batch;

import org.apache.log4j.Logger;

public class MainJob {
	private final static Logger logger = Logger.getLogger(MainJob.class);
	
	/*
	 * Cache maps pulled from google
	 */
	private final static String STATIC_MAPS = "staticMapJob";
	
	/*
	 * update the keyword and public_store_key field of store table
	 */
	private final static String STORE_DATA_UPDATE = "storeDataUpdateJob";
	
	/*
	 * Pull data from yellowpages
	 */
	private final static String YELLOWPAGES = "yellowpagesDataJob";
	
	/*
	 * pull data from yelp
	 */
	private final static String YELP = "yelpDataJob";
	
	/*
	 * Generate sitemap
	 */
	private final static String SITEMAP = "crawlerSiteMapJob";
	
	/*
	 * update the id field of the user table 
	 */
	private final static String USERID = "userIdUpdateJob";
	
	/*
	 * These updates the longitude and latitude of stores
	 */
	private final static String CLEAN_STORE = "cleanStoreJob";
	private final static String CLEAN_STORE_SCRAPPER = "cleanScrapperJob";
	
	/*
	 * Does not have job entry for saving job state
	 */
	private final static String YELP_LINK_JOB = "yelpLinksJob";
	
	private final static String YELLOWPAGES_LINK_JOB = "generateLinksJob";
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length > 0) {
			BatchJob job = null;
			String jobName = args[0];
			
			if(STATIC_MAPS.equals(jobName)) {
				job = new StaticMapsJob(args);
			}
			else if(CLEAN_STORE.equals(jobName) || CLEAN_STORE_SCRAPPER.equals(jobName)) {
				job = new StoreDataJob(args);
			}
			else if(SITEMAP.equals(jobName) || STORE_DATA_UPDATE.equals(jobName)) {
				job = new SitemapAndStoreUpdateJob(args);
			}
			else if(YELLOWPAGES.equals(jobName) || YELP.equals(jobName)) {
				job = new YelpAndYellowpageJob(args);
			}
			else if(YELP_LINK_JOB.equals(jobName)) {
				job = new YelpLinksJob(args);
			}
			else if(YELLOWPAGES_LINK_JOB.equals(jobName)) {
				job = new YellowpagesLinksJob(args);
			}
			else {
				job = new OtherJobs(args);
			}
			
			try {
				if(job != null) {
					job.execute();
				}
			}
			catch(Exception e) {
				logger.info("Job Failed [" + jobName + "]");
				System.exit(1);
			}
		}
		
		System.exit(0);
	}
}
