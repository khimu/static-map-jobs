package com.api.cron.batch;

/**
 * 
 * @author Ung
 *
 */
public class CronJobException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CronJobException(String msg) {
		super(msg);
	}

}
