package com.api.cron.batch.jobs.sitemap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sitemap")
public class SitemapFile {
	private String loc;
	private String lastMod;

	@XmlElement(name = "loc")
	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	@XmlElement(name = "lastmod")
	public String getLastMod() {
		return lastMod;
	}

	public void setLastMod(String lastMod) {
		this.lastMod = lastMod;
	}

}
