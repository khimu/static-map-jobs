package com.api.cron.batch.task;

/**
 * 
 * @author Ung
 *
 */
public class ServiceNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ServiceNotFoundException(String msg) {
		super(msg);
	}

}
