package com.nordea.dompap.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copied from com.nordea.next.service.dao
 */
public class JdbcUtil {
    public static <T> T exactQuery(PreparedStatement ps, RowMapper<T> mapper) throws SQLException {
        T element;
        try (ResultSet rs = ps.executeQuery()) {
            int rowNum = 1;
            if (rs.next()) {
                element = mapper.mapRow(rs, rowNum++);
            } else {
                // No results
                return null;
            }
            if (rs.next()) {
                throw new SQLException("Query "+ps.toString()+" returned more than one result");
            }
        }
        return element;
    }

    public static int countQuery(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Query: "+ps+" did not return a count as expected.");
    }

    public static <T> List<T> listQuery(PreparedStatement ps, RowMapper<T> mapper) throws SQLException {
        List<T> list = new ArrayList<T>();
        try (ResultSet rs = ps.executeQuery()) {
            int rowNum = 1;
            while (rs.next()) {
                list.add(mapper.mapRow(rs, rowNum++));
            }
        }
        return list;
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet rows, int rowNum) throws SQLException;
    }

    public static java.sql.Timestamp toTimestamp(Date date) {
        if (date!=null) {
            return new java.sql.Timestamp(date.getTime());
        } else {
            return null;
        }
    }

}