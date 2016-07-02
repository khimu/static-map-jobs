package com.api.cron.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.api.cron.batch.model.Link;

public class LinkMapper implements RowMapper<Link>  {

	@Override
	public Link mapRow(ResultSet rs, int arg1) throws SQLException {
		Link link = new Link();
		link.setId(rs.getInt("id"));
		link.setLink(rs.getString("link"));
		link.setCity(rs.getString("city"));
		link.setState(rs.getString("state"));
		link.setPage(rs.getInt("pages"));
		link.setCategory(rs.getString("category"));
		return link;
	}
	
}
