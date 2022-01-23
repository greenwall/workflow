package com.nordea.dompap.jdbc;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for combining building of SQL statement with binding parameters to the PreparedStatement.
 * It combines a StringBuilder (supporting append(String)) and a List of parameters.
 * The PreparedStatements  set...(index, value) methods are replaced with add...(value) to avoid the indexed binding. 
 *
 * Replace:
 * <pre>
 * String sql = "update TABLE set COLA=?, COLB=? where COLC = ?";
 * try (PreparedStatement ps = con.prepareStatement(sql)) {
 * 	ps.setTimestamp(1, toTimestamp(colA));
 * 	ps.setString(2, colB);
 * 	ps.setString(3, colC);
 * 
 * 	updatedRows = ps.executeUpdate();
 * }
 * </pre>
 * 
 * with (the binding of parameters are now next to parameter definition, and no need to keep track of index): 
 * 
 * <pre>
 * SqlStatement sql = new SqlStatement();
 * sql.append("update TABLE set COLA=?, COLB=? where COLC=?")
 * 	.addTimestamp(toTimestamp(colA))
 * 	.addString(colB)
 * 	.addString(colC);
 * 
 * try (PreparedStatement ps = sql.prepareStatement(con)) {
 * 		updatedRows = ps.executeUpdate();
 * }
 * // or just updatedRows = sql.executeUpdate(con);
 * 
 * </pre>
 * 
 * @author G93283
 *
 */
public class SqlStatement {
	private final StringBuilder b = new StringBuilder();
	private final List<AddParam> addParams = new ArrayList<>();

	public SqlStatement(String str) {
		append(str);
	}
	
	public SqlStatement append(String str) {
		b.append(str);
		return this;
	}
	
	public SqlStatement addObject(final Object param) {
		addParams.add((ps, index) -> ps.setObject(index, param));
		return this;
	}
	
	public SqlStatement addString(final String param) {
		return addString(0, param);
	}
	
	public SqlStatement addString(final int maxLength, final String param) {
		addParams.add((ps, index) -> {
			if (maxLength>0) {
				ps.setString(index, StringUtils.left(param, maxLength));
			} else {
				ps.setString(index, param);
			}
		});
		return this;
	}

	public SqlStatement addTimestamp(final Timestamp param) {
		addParams.add((ps, index) -> ps.setTimestamp(index, param));
		return this;
	}
	
	public SqlStatement addBinaryStream(final byte[] data) {
		addParams.add((ps, index) -> {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ps.setBinaryStream(index, bais, data.length);
		});
		return this;
	}
		
	public SqlStatement addCharacterStream(final String data) {
		addParams.add((ps, index) -> {
			if (data!=null) {
				ps.setCharacterStream(7, new StringReader(data), data.length());
			} else {
				ps.setCharacterStream(7, null, 0);
			}
		});
		return this;
	}

	public boolean execute(Connection con) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(toString())) {			
			addParameters(ps);
			return ps.execute();			
		}
	}

	public int executeUpdate(Connection con) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(toString())) {			
			addParameters(ps);
			return ps.executeUpdate();			
		}
	}
	
	public void addParameters(PreparedStatement stmt) throws SQLException {
		int ix = 1;
		for (AddParam param : addParams) {
			param.addTo(stmt, ix++);
		}
	}
	
	/**
	 * Builds a PreparedStatement with the given sql and binds given parameters.
	 * Caller must close statement (preferably using try-with-resource).
	 */
	public PreparedStatement prepareStatement(Connection con) throws SQLException {
		PreparedStatement stmt = con.prepareStatement(toString());
		addParameters(stmt);
		return stmt;
	}
	
	public String toString() {
		return b.toString();
	}
	
	protected List<AddParam> parameters() {
		return addParams;
	}
	
	/**
	 * Add both sql and parameters from other statement to this.
	 */
	public SqlStatement add(SqlStatement other) {
		b.append(other.b);
		addParams.addAll(other.addParams);
		return this;
	}
	
	interface AddParam {
		void addTo(PreparedStatement ps, int index) throws SQLException;
	}
}
