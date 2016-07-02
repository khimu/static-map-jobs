package com.api.cron.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.api.cron.batch.model.JobStateCrons;

/**
 * 
 * @author Ung
 *
 */
public class JobStateMapper implements RowMapper<JobStateCrons>{

	@Override
	public JobStateCrons mapRow(ResultSet arg0, int arg1) throws SQLException {
			JobStateCrons cron = new JobStateCrons();
		cron.setId(arg0.getInt("id"));
		cron.setJobName(arg0.getString("job_name"));
		cron.setLastProcessedKey(arg0.getInt("last_processed_key"));
		cron.setQuota(arg0.getInt("quota"));
		cron.setAlreadyExist(arg0.getInt("already_exist"));
		cron.setSuccess(arg0.getInt("success"));
		cron.setFailed(arg0.getInt("failed"));
		cron.setLastProcessedDate(arg0.getDate("last_processed_date"));
		return cron;
	}

}
