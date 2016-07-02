package com.api.cron.batch;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
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

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.mapper.JobStateMapper;
import com.api.cron.batch.model.JobStateCrons;

/*
 * Update business_data with longitude and latitude
 * 
 * cleanStoreJob
 * 
 */
public class StoreDataJob implements BatchJob {
	private final static Logger logger = Logger.getLogger(StoreDataJob.class);

	private String[] args;
	
	public StoreDataJob() {}
	
	public StoreDataJob(String[] args) {
		this.args = args;
	}
	
	@Override
	public void execute() throws CronJobException {
		String[] springConfig  = 
			{	
				"spring/batch/jobs/common-context.xml",
				"spring/batch/database.xml"
			};

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
	
			
			JdbcTemplate jdbcTemplate = (JdbcTemplate) context.getBean("localJdbcTemplate");
			JobState jobState = (JobState) context.getBean("jobState");
			Integer jobId = null;
			
			try {			
				List<JobStateCrons> crons = null;
				try {
					jobId = jdbcTemplate.queryForObject(props.getProperty("cron.job.select.max.job.id"), new Object[]{jobName}, Integer.class);
					crons = jdbcTemplate.query(props.getProperty("cron.job.select.data.processing.job"), new Object[]{jobId}, new JobStateMapper());
				} catch(Exception e) {
					logger.warn("Unable to retrieve job");
					System.exit(1);
				}
				
				if(crons != null && !crons.isEmpty()) {
					logger.info("Job Launching " + jobName + " quota["+crons.get(0).getQuota()+"] lastProcessedKey[" + crons.get(0).getLastProcessedKey() + "]");
					JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
					Job job = (Job) context.getBean(jobName);
	
					try {
						
						JobParametersBuilder builder = new JobParametersBuilder();
						builder.addString("quota", crons.get(0).getQuota() + "");
						builder.addString("lastProcessedKey", crons.get(0).getLastProcessedKey() + "");
						builder.addString("jobName", jobName);
	
						
						JobExecution execution = jobLauncher.run(job, builder.toJobParameters());

						if(execution.getAllFailureExceptions().size() > 0) {
							for(Throwable t : execution.getAllFailureExceptions()) {
								t.printStackTrace();
							}
						}else {
							logger.info("No exeptions found");
						}
	
						logger.info("Exit Status : " + execution.getStatus());
						
						if(execution.getStatus() == BatchStatus.FAILED) {
							logger.error("Batch job failed");
							recordError(jobState, jobId.toString(), jdbcTemplate);
							System.exit(1);
						}else {
							logger.info("Batch job successful");
							if(jobState.getSuccess() == 0 && jobState.getFailed() == 0 && jobState.getAlreadyExist() == 0) {
								logger.info("No stores found with empty longitude and latitude");
								jobState.setLastProcessedKey(crons.get(0).getQuota());
								recordError(jobState, jobId.toString(), jdbcTemplate);
							}
							
							System.exit(0);
						}
					} catch (Exception e) {
						recordError(jobState, jobId.toString(), jdbcTemplate);
						logger.info("Error : " + e.getMessage());
						e.printStackTrace();
					}
				}
				logger.info("End of Job " + jobName);
				System.exit(0);
			} catch (Exception e1) {
				logger.info("Error : " + e1.getMessage());
				e1.printStackTrace();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.exit(1);
	}
	
	private void recordError(JobState jobState, String dataProcessingJobId, JdbcTemplate jdbcTemplate) {
	    PreparedStatementCallback<Boolean> psFunction = (ps) -> {
	        ps.setInt(1,jobState.getAlreadyExist());  
	        ps.setInt(2, jobState.getSuccess());  
	        ps.setInt(3, jobState.getFailed());
	        ps.setInt(4, jobState.getLastProcessedKey());
	        ps.setInt(5, Integer.parseInt(dataProcessingJobId));
	        
	        return (Boolean) ps.execute();    
	    };
	    Boolean success = jdbcTemplate.execute("UPDATE job_state set already_exist = ?, success = ?, failed = ?, last_processed_key = ? where id = ?", psFunction);
	}
}
