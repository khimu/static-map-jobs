package com.api.cron.batch.jobs.sitemap;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.api.cron.batch.storekeywords.StoreKeywordItemReader.CountryCode;

public class SitemapMapper implements RowMapper<XmlUrl>  {
	 
	@Override
	public XmlUrl mapRow(ResultSet rs, int arg1) throws SQLException {
		StringBuilder publicStoreKeyBuilder = new StringBuilder();
		
		publicStoreKeyBuilder.append(rs.getString("name") == null ? "" : rs.getString("name").replaceAll("[\\s|\\W]+", "-"));
		publicStoreKeyBuilder.append("=");
		publicStoreKeyBuilder.append(rs.getString("address_line_1") == null ? "" : rs.getString("address_line_1").replaceAll("[\\s|\\W]+", "-"));
		publicStoreKeyBuilder.append("-");
		publicStoreKeyBuilder.append(rs.getString("city") == null ? "" : rs.getString("city").replaceAll("([\\s|\\W]+)", "-"));
		publicStoreKeyBuilder.append("-");
		publicStoreKeyBuilder.append(rs.getString("state") == null ? "" : rs.getString("state").replaceAll( "([\\s|\\W]+)", "-"));
		publicStoreKeyBuilder.append("-");		
		publicStoreKeyBuilder.append(rs.getString("zipcode") == null ? "" : rs.getString("zipcode").replaceAll("([\\s|\\W]+)", "-"));
		publicStoreKeyBuilder.append("-");
		publicStoreKeyBuilder.append(rs.getString("country_code") == null ? "" : CountryCode.getFullName(rs.getString("country_code")));
		publicStoreKeyBuilder.append(".html");
		
		XmlUrl url = new XmlUrl();
		url.setLoc(Constant.PAGE_URL + publicStoreKeyBuilder.toString().toLowerCase());
		return url;
	}
}
