package com.nordea.dompap.jdbc;

import lombok.Value;

import java.sql.ResultSet;
import java.sql.SQLException;

@Value
public class StringMapper implements JdbcUtil.RowMapper<String> {
	String columnName;

	@Override
	public String mapRow(ResultSet rows, int rowNum) throws SQLException {			
		return rows.getString(columnName);
	}		
}
