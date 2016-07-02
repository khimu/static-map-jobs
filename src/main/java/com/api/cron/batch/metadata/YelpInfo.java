package com.api.cron.batch.metadata;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.cron.batch.model.Topic;

public class YelpInfo implements TaskInfo {

	/*
	 * http://www.yelp.com/search?find_desc=target&find_loc=100+Willow+Park+Ctr,+los+angeles,+ca&start=10
	 */
	private String host;
	
	private Topic naics;
	
	public YelpInfo(String serviceEndpoint, Topic naics) {
		this.host = serviceEndpoint;
		this.naics = naics;
	}

	/*
	 * Requires GoogleMetadataBuilder to generate the correct download URL
	 */
	public String getServiceEndpoint(){
		return this.host;
	}
	
	public Topic getNaics() {
		return this.naics;
	}
	
	public static class YelpBuilder {
		private final static Logger logger = LoggerFactory.getLogger(YelpBuilder.class);
		private String serviceEndpoint = "http://www.yelp.com/search?find_desc={category}&find_loc={location}&start={page}";
		private Map<String, String> placeholder = new HashMap<String, String>();
		private Topic naics;

		public YelpBuilder setServiceEndpoint(String serviceEndpoint) {
			this.serviceEndpoint = serviceEndpoint;
			return this;
		}

		public YelpBuilder setCategory(String category) throws UnsupportedEncodingException {
			logger.info("Setting Category " + category);
			this.placeholder.put("\\{category\\}", category);
			return this;
		}
		
		public YelpBuilder setLocation(String location) throws UnsupportedEncodingException {
			this.placeholder.put("\\{location\\}", location);
			return this;
		}
		
		public YelpBuilder setPage(String page) {
			this.placeholder.put("\\{page\\}", page);
			return this;
		}
		
		public YelpBuilder setNaics(Topic naics) {
			this.naics = naics;
			return this;
		}

		/*
		 * Build the metadata class
		 */
		public YelpInfo execute() {
			for(Map.Entry<String, String> entry : placeholder.entrySet()) {
				this.serviceEndpoint = this.serviceEndpoint.replaceAll(entry.getKey(), entry.getValue());
			}
			
			return new YelpInfo(this.serviceEndpoint, this.naics);
		}

	}

}
