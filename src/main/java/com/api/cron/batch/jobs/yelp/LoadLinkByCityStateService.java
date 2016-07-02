package com.api.cron.batch.jobs.yelp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.metadata.YelpInfo;
import com.api.cron.batch.metadata.YelpInfo.YelpBuilder;
import com.api.cron.batch.task.TaskException;
import com.api.cron.batch.task.YelpPagesTask;

/*
 * Runs independently.  Given a list of categories, pull all the pages from Yelp with that category
 */
@Component("yelpLoadLinkByCityService")
public class LoadLinkByCityStateService  { 
	
	private final static Logger logger = Logger.getLogger(BusinessDataItemReader.class);
	
	private int APPROX_ITEMS_PER_PAGE = 10;
	
	private String DEFAULT_PAGE = "0";
	
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
					YelpInfo info = new YelpBuilder().setCategory(category).setLocation(city + ", " + state).setPage(i+"").execute(); 
						localJdbcTemplate.execute("INSERT INTO yelp_links (link, city, state, category, pages) VALUES ('" + info.getServiceEndpoint() + "','" + city + "','" + state + "','" + category + "', '" + i + "');");
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
	
	private int buildInfos(String category, String page, String city, String state) {
		try {
			YelpInfo info = new YelpBuilder().setNaics(null).setCategory(category).setLocation(city + ", " + state).setPage(page).execute(); 
			
			// get the first page and determine how many results for the given category and location
			YelpPagesTask task = new YelpPagesTask();
			
			/*
			 * read the first request for business info and update infos list one time
			 */
			task.execute(info);

			int pages = task.getTotalBusinesses() / APPROX_ITEMS_PER_PAGE;
			if(task.getTotalBusinesses() % APPROX_ITEMS_PER_PAGE > 0) {
				pages ++;
			}			
			try {
				localJdbcTemplate.execute("INSERT INTO  yelp_links (link, city, state, category, pages) VALUES ('" + info.getServiceEndpoint() + "','" + city + "','" + state + "','" + category + "', '" + page + "');");
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
