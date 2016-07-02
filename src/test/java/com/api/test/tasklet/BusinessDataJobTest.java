package com.api.test.tasklet;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.api.cron.batch.MainJob;

public class BusinessDataJobTest {

	private final static Logger logger = Logger.getLogger(CronJobTest.class);
	
	@Test
	public void testSitemap() {
		MainJob job = new MainJob();
		job.main(new String[] { "crawlerSiteMapJob", "spring/batch/jobs/crawler-sitemap.xml", "/opt/sitemaps/googleapi.properties"});
	}
		
	
	@Test
	public void testcleanStoreData() {
		MainJob job = new MainJob();
		job.main(new String[] { "cleanStoreJob", "spring/batch/jobs/clean-store-data.xml", "/opt/geocode/googleapi.properties"});
	}
	
	@Test
	public void testGenerateLinks() {
		MainJob job = new MainJob();
		job.main(new String[] { "generateLinksJob", "spring/batch/jobs/simple-yellowpages.xml", "/opt/yellowpages-data/googleapi.properties", "--city=los angeles",
				"--state=ca", "--categories=pink berry" });
	}
	
	
	@Test
	public void testYpData() {
		MainJob job = new MainJob();
		job.main(new String[] { "yellowpagesDataJob", "spring/batch/jobs/yellowpages-data.xml", "/opt/yellowpages-data/googleapi.properties", "--city=los angeles", "--state=ca" });
	}
	
	
	@Test
	public void testYelpData() {
		MainJob job = new MainJob();
		job.main(new String[] { "yelpDataJob", "spring/batch/jobs/yelps-data.xml", "/opt/yelp/googleapi.properties", "--city=los angeles", "--state=ca" });
	}

	@Test
	public void testYelpLinks() {
		MainJob job = new MainJob();
		job.main(new String[] { "yelpLinksJob", "spring/batch/jobs/link-yelppages.xml", "/opt/yelp/googleapi.properties", "--city=los angeles", "--state=ca", "--categories=target,walmart,sears,nordstrom,barneys,saks fith ave,neiman marcus,brookstone,image" });
	}
	
	
	@Test
	public void testStaticMaps() {
		MainJob job = new MainJob();
		job.main(new String[] { "staticMapJob", "spring/batch/jobs/staticmap.xml", "/opt/static-maps/googleapi.properties" });
	}
	
	@Test
	public void testString() {
		System.out.println("(310) 763-0467".replaceAll("[(|)|\\s|-]+", ""));
	}
 
}
