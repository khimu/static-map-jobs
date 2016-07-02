package com.api.cron.batch;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.api.cron.batch.jobitems.JobState;
import com.api.cron.batch.jobs.yellowpages.LoadLinkByCityStateService;

/**
 * Create links in the DB and then runs the yellowpages scrapper job to read businesses from each link.
 * This job can pick up where it left off
 * 
 * @author khimung
 *
 */
public class YellowpagesLinksJob implements BatchJob {
	private final static Logger logger = Logger.getLogger(YellowpagesLinksJob.class);

	private String[] args;

	public YellowpagesLinksJob() {
	}

	public YellowpagesLinksJob(String[] args) {
		this.args = args;
	}

	@Override
	@Transactional(value = "localTransactionManager", timeout = 60000, isolation = Isolation.READ_UNCOMMITTED)
	public void execute() throws CronJobException {

		if (args.length < 3) {
			logger.error("The job name provided does not exist.  Job is missing parameters.");
			logger.info("Exit Code 1");
			System.exit(1);
		}
		
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					"spring/batch/jobs/common-context.xml", "spring/batch/database.xml", args[1]);

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

			String city = args[3].split("=")[1];
			String state = args[4].split("=")[1];
			String[] categories = args[5].split("=")[1].split(",");
			
			JdbcTemplate storeJdbcTemplate = (JdbcTemplate) context.getBean("jdbcTemplate");
			
			Integer lastProcessedKey = storeJdbcTemplate.queryForObject("select max(id) from yellowpages_links ", Integer.class);
			
			if(lastProcessedKey == null){
				lastProcessedKey = 0;
			}
			
			LoadLinkByCityStateService loadLinkByCityStateService = (LoadLinkByCityStateService) context.getBean("loadLinkByCityService");				
			loadLinkByCityStateService.load(city, state, categories);
			
			JobState jobState = (JobState) context.getBean("jobState");
			
			JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
			Job job = (Job) context.getBean("simpleYellowpagesJob");
			

			JobParametersBuilder builder = new JobParametersBuilder();
			builder.addString("quota", jobState.getSuccess() + "");
			builder.addString("lastProcessedKey", lastProcessedKey.toString());
			builder.addString("jobName", "simpleYellowpagesJob");
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

			
			System.exit(0);
		} catch (Exception e1) {
			logger.info("Error : " + e1.getMessage());
			e1.printStackTrace();
			throw new CronJobException("Job did not exit correctly.  Job completed with failed status.");
		}
	}

}
