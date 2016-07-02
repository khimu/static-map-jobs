package com.api.cron.batch.model;

/**
 * 
 * @author Ung
 *
 */
public class Users extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String businessId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String cloutId) {
		this.businessId = cloutId;
	}

}
