package com.api.cron.batch.jobs.yellowpages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.metadata.YellowPageInfo;
import com.api.cron.batch.metadata.YellowPageInfo.YellowPageMetadataBuilder;
import com.api.cron.batch.task.TaskException;
import com.api.cron.batch.task.YellowPagesTask;

/**
 * Give a list of categories in the job argument, query yellowpages and pull the pages and store in DB for reading in the second job
 * 
 * @author khimung
 *
 */
@Component("loadLinkByCityService")
public class LoadLinkByCityStateService  { 
	
	private final static Logger logger = Logger.getLogger(BusinessDataItemReader.class);
	
	private int APPROX_ITEMS_PER_PAGE = 30;
	
	private String DEFAULT_PAGE = "1";
	
	@Resource(name = "localJdbcTemplate")
	private JdbcTemplate localJdbcTemplate;
	
	@Resource
	private JobState jobState;
	
	@Transactional(value="transactionManager", timeout=60000, isolation=Isolation.READ_UNCOMMITTED)
	public void load(String city, String state, String[] categories) {

		/*
		 * One category only one info
		 */
		for(String category : categories) {
			int pages = buildInfos(category, DEFAULT_PAGE, city, state);
			logger.info("Total pages " + pages);
			if(pages > 1) {
				for(int i = 2; i < pages; i ++) {
					try {
						YellowPageInfo info = new YellowPageMetadataBuilder().setCategory(category).setLocation(city + ", " + state).setPage(i+"").execute(); 
						localJdbcTemplate.execute("INSERT INTO yellowpages_links (link, city, state, category, pages) VALUES ('" + info.getServiceEndpoint() + "','" + city + "','" + state + "','" + category + "', '" + i + "');");
						jobState.incrementSuccess();
					}catch(Exception e) {
						logger.error("Unable to insert yelp link record due to " + e.getMessage());
					}
				}	
			}
			logger.info("Finished with category " + category);
		}

		logger.info("DONE");
	}
	
	@Transactional
	private int buildInfos(String category, String page, String city, String state) {
		try {
			YellowPageInfo info = new YellowPageMetadataBuilder().setNaics(null).setCategory(category).setLocation(city + ", " + state).setPage(page).execute(); 
			
			// get the first page and determine how many results for the given category and location
			YellowPagesTask task = new YellowPagesTask();
			
			/*
			 * read the first request for business info and update infos list one time
			 */
			task.execute(info);

			int pages = task.getTotalBusinesses() / APPROX_ITEMS_PER_PAGE;
			if(task.getTotalBusinesses() % APPROX_ITEMS_PER_PAGE > 0) {
				pages ++;
			}		
			try {
				localJdbcTemplate.execute("INSERT INTO yellowpages_links (link, city, state, category, pages) VALUES ('" + info.getServiceEndpoint() + "','" + city + "','" + state + "','" + category + "', '" + page + "');");
				jobState.incrementSuccess();
			}catch(Exception e) {
				logger.error("Unable to insert yelp link record due to " + e.getMessage());
			}
			
			/*
			 * Build infos from it
			 */
			return pages;
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} catch (TaskException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}

}
