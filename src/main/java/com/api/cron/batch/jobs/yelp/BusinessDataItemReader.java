package com.api.cron.batch.jobs.yelp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.api.cron.batch.jobitems.BusinessItem;
import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.metadata.YelpInfo;
import com.api.cron.batch.metadata.YelpInfo.YelpBuilder;
import com.api.cron.batch.model.KeyValuePair;
import com.api.cron.batch.model.Topic;
import com.api.cron.batch.model.Store;
import com.api.cron.batch.task.TaskException;
import com.api.cron.batch.task.YellowPagesTask;
import com.api.cron.batch.task.YelpPagesTask;

/**
 * Use category to retrieve business name and address from yellowpage
 * 
 * categories_level_2_naics_mapping; maps naics_code to _category_level_2_id - put memcache in front of this to speed up the job
 * 
 * if(categories_level_2_naics_mapping._category_level_2_id = category_level_2.id) then
 * create store_sub_categories with _category_id, _store_id
 * 
 * 
 * @author Ung
 *
 */
@Component("yelpItemReader")
@Scope("step")
@Transactional(value="transactionManager", timeout=60000, isolation=Isolation.READ_UNCOMMITTED)
public class BusinessDataItemReader implements ItemReader<BusinessItem> { 
	
	private final static Logger logger = Logger.getLogger(BusinessDataItemReader.class);
	
	private int APPROX_ITEMS_PER_PAGE = 10;
	
	@Value("#{jobParameters['city']}")
	private String city;
	
	@Value("#{jobParameters['state']}")
	private String state;
	
	private String DEFAULT_PAGE = "0";
	
	private Queue<YelpInfo> infos;
	
	@Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name = "naicsCategory")
	private List<Topic> naicsCategory;

	@Value("#{stepExecutionContext[fromId]}")
	private Integer fromId;
	
	@Value("#{stepExecutionContext[toId]}")
	private Integer toId;
	
	//@Value("${cron.job.business.data.category.query}")
	//private String naicsCategoryQuery;
	
	//@Value("${cron.job.business.data.naics.category.mapping.query}")
	//private String naicsCategoryMappingQuery;
	
	@Resource(name = "categoryLevel2ToNaicsMapping")
	private Map<String, Integer> categoryLevel2ToNaicsMapping;
	
	
	@Resource
	private JobState jobState;
	
	@PostConstruct
	public void init() {
		
		logger.info("Categories list size " + this.naicsCategory.size() + " from Id " + fromId + " toId " + toId);
		
		
		StringBuilder naicsCodeList = new StringBuilder();
		
		List<String> dupList = new ArrayList<String>();

		/*
		 * get map of store id to category id
		 */
		jdbcTemplate.query("SELECT id, code from topic where id >= ? and id <= ?", new Object[] {fromId, toId}, (rs, rowNum) -> {
			Topic naics = new Topic();
			naics.setId(rs.getInt("id"));
			naics.setCode(rs.getString("code").trim());
			return naics;
		})
		.stream()
		.filter(item -> {
			if(item.getId() > jobState.getLastProcessedKey()) {
				jobState.setLastProcessedKey(item.getId());
			}
			if(!dupList.contains(item.getCode().toLowerCase())) {
				logger.info("Not a dup " + item.getCode());
				return true;
			}
			return false;
		})
		.forEach(item -> {
			dupList.add(item.getCode().toLowerCase());
			naicsCategory.add(item);	
			naicsCodeList.append(item.getCode() +",");
		});
		
		logger.info(naicsCodeList.toString() + " dupList " + dupList.size());
		
		
		if(naicsCodeList != null && naicsCodeList.toString().length() > 0) {
			/*
			 * needed to create a record to map a store to a naics category via the category_level_2 table by adding an
			 * entry in store_sub_category. 
			 */
			/*
			jdbcTemplate.query(naicsCategoryMappingQuery, new Object[] {naicsCodeList.toString().substring(0, naicsCodeList.toString().length() - 1)}, (rs, rowNum) -> {
				KeyValuePair<String, Integer> pair = new KeyValuePair<String, Integer>();
				pair.setKey(rs.getString("naics_code"));
				pair.setValue(rs.getInt("_category_level_2_id"));
				return pair;
			})
			.stream()
			.forEach(item -> categoryLevel2ToNaicsMapping.put(item.getKey(), item.getValue()));	
			*/
			
			
			infos = new LinkedList<YelpInfo>();
			
			logger.info("naics size: " + naicsCategory.size());
			
			/*
			 * One category only one info
			 */
			for(Topic naic : naicsCategory) {
				int pages = buildInfos(naic, DEFAULT_PAGE);
				logger.info("Total pages " + pages);
				if(pages > 0) {
					for(int i = 1; i < pages; i ++) {
						try {
							YelpInfo info = new YelpBuilder().setNaics(naic).setCategory(naic.getCode()).setLocation(city + ", " + state).setPage((i* APPROX_ITEMS_PER_PAGE) + "").execute(); 
							infos.add(info);
						} catch (UnsupportedEncodingException e) {
							logger.error(e.getMessage());
						} 
					}	
				}
				logger.info("Finished with naic " + naic.getCode());
			}
		}
		
		logger.info("Store Categories list size " + this.naicsCategory.size());
	}
	
	private int buildInfos(Topic naic, String page) {
		try {
			YelpInfo info = new YelpBuilder().setNaics(naic).setCategory(naic.getCode()).setLocation(city + ", " + state).setPage(page).execute(); 
			
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
			 
			infos.add(info);
			
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

	
	@Override
	public BusinessItem read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException, TaskException {
		
		if(infos == null) {
			logger.info("ifos is null");
			return null;
		}
		
		if(infos.isEmpty()) {
			logger.info("ifos is empty");
			return null;
		}
		
		try {
			YelpInfo info = infos.remove();
			logger.info("working on " + info.getServiceEndpoint() + " naics " + info.getNaics().getCode() + " for id range " + fromId + " " + toId);
			
			YelpPagesTask task = new YelpPagesTask();
			task.execute(info);
			
			logger.info("executed ");
			BusinessItem item = new BusinessItem();
			while(task.hasNext()) {
				logger.info("hasNext");
				try {
					// Update or create a store record
					Store store = new Store();
					String storeName = task.getBusinessName();
					if(storeName == null) {
						logger.info("Store name is null for category " + info.getNaics().getCode());
						task.next();
						continue;
					}
					logger.info("Found store " + storeName);
					store.setName(storeName);
					store.setPhoneNumber(task.getNextPhones() == null ? "" : task.getNextPhones());
					store.setState(state);
					store.setCity(city);
					store.setCategory(task.getCategory());
					store.setFullAddress(task.getNextFullAddress());
					logger.info("after parsing");
	
					logger.info("store name " + store.getName() + " full address " + store.getFullAddress() + " ");

					item.getStores().add(store);
				}catch(Exception e){
					logger.error("Unable to extract store");
				}
				finally {
					task.next();
				}
			}
			
			item.setNaics(info.getNaics());
			logger.info("Found stores " + item.getStores().size());
			return item;
		} catch(Exception e) {
			e.printStackTrace();
			throw new TaskException("Unexpected error.  Skipping " + e.getMessage());
		}

	}

}
