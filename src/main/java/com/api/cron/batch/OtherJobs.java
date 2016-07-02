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

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.mapper.JobStateMapper;
import com.api.cron.batch.model.JobStateCrons;

/*
 * userid generator will go through this
 */
public class OtherJobs implements BatchJob {
	private final static Logger logger = Logger.getLogger(OtherJobs.class);

	private String[] args;
	
	public OtherJobs() {}
	
	public OtherJobs(String[] args) {
		this.args = args;
	}
	
	@Override
	public void execute() throws CronJobException {
		String[] springConfig  = 
			{	
				"spring/batch/jobs/common-context.xml",
				"spring/batch/database.xml"
			};
		

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
	
			
			JdbcTemplate jdbcTemplate = (JdbcTemplate) context.getBean("localJdbcTemplate");
			JobState jobState = (JobState) context.getBean("jobState");
			
			
			try {			
	
				String selectMaxId = props.getProperty("cron.job.select.max.job.id");
				String selectDataProcessingJob = props.getProperty("cron.job.select.data.processing.job");
				
				logger.info(selectMaxId);
				logger.info(selectDataProcessingJob);
				
				
				List<JobStateCrons> crons = null;
				try {
					Integer maxId = jdbcTemplate.queryForObject(selectMaxId, new Object[]{jobName}, Integer.class);
					crons = jdbcTemplate.query(selectDataProcessingJob, new Object[]{maxId}, new JobStateMapper());
				} catch(Exception e) {
					logger.warn("Unable to retrieve job");
					System.exit(0);
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
							System.exit(1);
						}else {
							logger.info("Batch job successful");
							System.exit(0);
						}
					} catch (Exception e) {
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
}
