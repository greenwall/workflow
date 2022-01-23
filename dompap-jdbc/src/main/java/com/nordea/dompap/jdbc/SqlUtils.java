package com.nordea.dompap.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 
 * @author G90511(Hitesh Karel)
 *
 */
public class SqlUtils {

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(new Date().getTime());
    }

    public static String getIntervalCompatibleString(long intervalInMinute) throws IllegalArgumentException {
        long day = 0;
        long hour = 0;
        long minute;
        StringBuilder interval = new StringBuilder();
        if (intervalInMinute < 0) {
            throw new IllegalArgumentException("Interval can not be negative,please provide valid value");
        }
        if (intervalInMinute >= 60) {
            hour = intervalInMinute / 60;
            if (hour >= 24) {
                day = hour / 24;
                hour = hour % 24;
            }
            minute = intervalInMinute % 60;
        } else {
            minute = intervalInMinute;
        }
        interval.append(day).append(" ").append(hour).append(":").append(minute).append(":00");
        return interval.toString();
    }

    /**
     * Returns the first rows from a result set using mapper, ignoring if more rows and null if zero rows.
     */
	public static <T> T first(PreparedStatement ps, JdbcUtil.RowMapper<T> mapper) throws SQLException {
		T element;
		try (ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				element = mapper.mapRow(rs, 1);
			} else {
				// No results
				return null;
			}
		}
		return element;
	}
    
}
