package com.api.cron.batch.jobitems;

import java.util.ArrayList;
import java.util.List;

import com.api.cron.batch.metadata.GoogleInfo;
import com.api.cron.batch.model.Topic;
import com.api.cron.batch.model.Store;
import com.api.cron.batch.model.StoreSubCategories;

public class BusinessItem extends BaseItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Topic naics;
	private String fullAddress;
	private StoreSubCategories storeSubCategories;
	private GoogleInfo geocodeApi;
	private GoogleInfo placesApi;

	public List<Store> stores = new ArrayList<Store>();

	public List<Store> getStores() {
		return stores;
	}

	public void setStores(List<Store> stores) {
		this.stores = stores;
	}	

	public Topic getNaics() {
		return naics;
	}

	public void setNaics(Topic naics) {
		this.naics = naics;
	}

	public String getFullAddress() {
		return fullAddress;
	}

	public void setFullAddress(String fullAddress) {
		this.fullAddress = fullAddress;
	}

	public StoreSubCategories getStoreSubCategories() {
		return storeSubCategories;
	}

	public void setStoreSubCategories(StoreSubCategories storeSubCategories) {
		this.storeSubCategories = storeSubCategories;
	}

	public GoogleInfo getGeocodeApi() {
		return geocodeApi;
	}

	public void setGeocodeApi(GoogleInfo geocodeApi) {
		this.geocodeApi = geocodeApi;
	}

	public GoogleInfo getPlacesApi() {
		return placesApi;
	}

	public void setPlacesApi(GoogleInfo placesApi) {
		this.placesApi = placesApi;
	}

}
