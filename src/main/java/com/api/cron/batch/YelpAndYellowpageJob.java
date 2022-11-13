package com.api.cron.batch;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.mapper.JobStateMapper;
import com.api.cron.batch.model.JobStateCrons;
import com.api.cron.batch.model.KeyValuePair;

/**
 * Scraps data from yelp or yellowpages 
 * 
 * @author khimung
 *
 */
public class YelpAndYellowpageJob implements BatchJob {
	private final static Logger logger = Logger.getLogger(YelpAndYellowpageJob.class);


	private String[] args;
	
	public YelpAndYellowpageJob() {}
	
	public YelpAndYellowpageJob(String[] args) {
		this.args = args;
	}
	
	@Override
	@Transactional(value="localTransactionManager", timeout=60000, isolation=Isolation.READ_UNCOMMITTED)
	public void execute() throws CronJobException {
		
		/*
		 * argument 2
		 *   spring/batch/jobs/staticmap.xml
		 *   spring/batch/jobs/crawler-sitemap.xml
		 */
		if(args.length < 3) {
			logger.error("The job name provided does not exist.  Job is missing parameters.");
			logger.info("Exit Code 1");
			System.exit(1);
		}
		
		try {
			ClassPathXmlApplicationContext context =  new ClassPathXmlApplicationContext("spring/batch/jobs/common-context.xml", "spring/batch/database.xml", args[1]);
	
			logger.info("args " + args[0] + " " + args[1] + " " + args[2]);
			 
			String jobName = args[0];
	
			String propertiesFile = args[2];
			
			Properties props = new Properties();
			
			try {
				props.load(new FileInputStream(propertiesFile));
			} catch (IOException e2) {
				logger.error("Error loading properties file");
				System.exit(1);
			}
	
			
			JdbcTemplate jobJdbcTemplate = (JdbcTemplate) context.getBean("localJdbcTemplate");
			JobState jobState = (JobState) context.getBean("jobState");
			Integer jobId = null;
			
			try {
	
				if(args.length < 5) {
					logger.error("Business data job requires 5 arguments.");
					System.out.println("Exit Code 1");
					System.exit(1);
				}
				
				/*
				 * build category id map
				 */
				JdbcTemplate storeJdbcTemplate = (JdbcTemplate) context.getBean("jdbcTemplate");

				
				
				
				Map<Integer, Integer> categoryLevel2Mapping = (Map<Integer, Integer>) context.getBean("categoryLevel2Mapping");

				/*
				 * get map of category id to category name
				 */
				storeJdbcTemplate.query("SELECT NOW()", (rs, rowNum) -> {
					KeyValuePair<Integer, Integer> pair = new KeyValuePair<Integer, Integer>();
					//pair.setKey(rs.getInt("id"));
					//pair.setValue(rs.getInt("_category_id"));
					return pair;
				})
				.stream()
				.forEach(pair -> categoryLevel2Mapping.put(pair.getKey(), pair.getValue()));

				
				
				
				
				logger.debug("categories size " + categoryLevel2Mapping.size());
				for(Map.Entry<Integer, Integer> entry : categoryLevel2Mapping.entrySet()) { 
					logger.debug("categoryid " + entry.getKey() + " category names size " + entry.getValue());
				}
				
				
				/*
				 * end category map
				 */
				

				List<JobStateCrons> crons = null;
				try {
					Integer maxId = jobJdbcTemplate.queryForObject("select max(id) from job_state where job_name = ?", new Object[]{jobName}, Integer.class);
					crons = jobJdbcTemplate.query("select * from job_state where id = ?", new Object[]{maxId}, new JobStateMapper());
					
				} catch(Exception e) {
					logger.warn("Unable to retrieve job");
					System.exit(0);
				}
				
				if(crons != null && !crons.isEmpty()) {
					/*
					 * get the id to update the job in case of failure
					 */
					jobId = crons.get(0).getId();
					
					logger.info("Job Launching " + jobName + " quota["+crons.get(0).getQuota()+"] lastProcessedKey[" + crons.get(0).getLastProcessedKey() + "]");
					JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
					Job job = (Job) context.getBean(jobName);

					JobParametersBuilder builder = new JobParametersBuilder();
					builder.addString("quota", crons.get(0).getQuota() + "");
					builder.addString("lastProcessedKey", crons.get(0).getLastProcessedKey() + "");
					builder.addString("jobName", jobName);

					logger.info("args3 is " + args[3] + " args4 is " + args[4]);
					
					String city = args[3].split("=")[1];
					String state = args[4].split("=")[1];

					builder.addString("city", city);
					builder.addString("state", state);

					JobExecution execution = jobLauncher.run(job, builder.toJobParameters());
				
					if(execution.getAllFailureExceptions().size() > 0) {
						for(Throwable t : execution.getAllFailureExceptions()) {
							t.printStackTrace();
						}
						throw new CronJobException("Exceptions in job");
					}else {
						logger.info("No exeptions found");
					}
	
					logger.info("Exit Status : " + execution.getStatus());
					
					if(execution.getStatus() == BatchStatus.FAILED) {
						logger.error("Batch job failed");
						throw new CronJobException("Job did not exit correctly.  Job completed with failed status.");
					}else {
						logger.info("Batch job successful");
					}
				}
				logger.info("End of Job " + jobName);
			} catch (Exception e1) {
				logger.info("Error : " + e1.getMessage());
				e1.printStackTrace();
				recordError(jobState, jobId.toString(), jobJdbcTemplate);
				throw new CronJobException("Job did not exit correctly.  Job completed with failed status.");
			}
		} catch (Exception e1) {
			logger.info("Error : " + e1.getMessage());
			e1.printStackTrace();
			throw new CronJobException("Job did not exit correctly.  Job completed with failed status.");
		}
	}
	
	private void recordError(JobState jobState, String dataProcessingJobId, JdbcTemplate jdbcTemplate) {
	    PreparedStatementCallback<Boolean> psFunction = (ps) -> {
	        ps.setInt(1,jobState.getAlreadyExist());  
	        ps.setInt(2, jobState.getSuccess());  
	        ps.setInt(3, jobState.getFailed());
	        ps.setInt(4, Integer.parseInt(dataProcessingJobId));
	        return (Boolean) ps.execute();    
	    };
	    Boolean success = jdbcTemplate.execute("UPDATE job_state set already_exist = ?, success = ?, failed = ? where id = ?", psFunction);
	}
}
