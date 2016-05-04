/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.rgw.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkExceptionTranslation;

/**
 * settings-IMplementation, die eine SQL-Datenbank zur Speicherung verwendet. In der jetzigen
 * Version wird nur eine flat table ähnlich wie bei cfgSettings verwendet. Mehrere Anwendungen
 * können dieselbe Datenbank verwenden, wenn sie unterschiedliche Tabellennamen verwenden.
 */

public class SqlSettings extends Settings {
	
	private static final long serialVersionUID = 7848755852540263456L;
	
	public static final String Version(){
		return "1.2.0";
	}
	
	volatile JdbcLink j;
	volatile String tbl;
	volatile String constraint = null;
	volatile String paramColumn = "param";
	volatile String valueColumn = "wert";
	
	public SqlSettings(JdbcLink j, String tablename, String paramColumn, String valueColumn,
		String constraint){
		this.j = j;
		tbl = tablename;
		this.constraint = constraint;
		this.paramColumn = paramColumn;
		this.valueColumn = valueColumn;
		undo();
	}
	
	public SqlSettings(JdbcLink j, String tablename){
		this.j = j;
		tbl = tablename;
		undo();
	}
	
	@Override
	public void remove(String key){
		super.remove(key);
		Stm stm = j.getStatement();
		StringBuilder sql = new StringBuilder(300);
		sql.append("DELETE FROM ").append(tbl).append(" WHERE ");
		if (constraint != null) {
			sql.append(constraint).append(" AND ");
		}
		sql.append(paramColumn).append(" LIKE ").append(JdbcLink.wrap(key + "%"));
		stm.exec(sql.toString());
		j.releaseStatement(stm);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.rgw.tools.Settings#flush()
	 */
	protected void flush_absolute(){
		Iterator it = iterator();
		PreparedStatement selectStatement = null;
		PreparedStatement deleteStatement = null;
		PreparedStatement updateStatement = null;
		PreparedStatement insertStatement = null;
		try {
			String constraintKey = null;
			String constraintValue = null;
			if (constraint != null) {
				String[] constraintParts = constraint.split("=");
				if (constraintParts.length == 2) {
					constraintKey = unwrap(constraintParts[0]);
					constraintValue = unwrap(constraintParts[1]);
				}
			}
			
			// prepare the select statement
			StringBuilder sql = new StringBuilder(300);
			sql.append("SELECT ").append(valueColumn).append(" FROM ").append(tbl)
				.append(" WHERE ");
			sql.append(paramColumn).append("= ?");
			if (constraintKey != null && constraintValue != null) {
				sql.append(" AND ").append(constraintKey).append("= ?");
			}
			selectStatement = j.getPreparedStatement(sql.toString());
			
			// prepare the delete statement
			sql = new StringBuilder(200);
			sql.append("DELETE FROM ").append(tbl).append(" WHERE ");
			sql.append(paramColumn).append("= ?");
			if (constraintKey != null && constraintValue != null) {
				sql.append(" AND ").append(constraintKey).append("= ?");
			}
			deleteStatement = j.getPreparedStatement(sql.toString());
			
			// prepare the update statement
			sql = new StringBuilder(200);
			sql.append("UPDATE ").append(tbl).append(" SET ").append(valueColumn).append("= ?")
				.append(" WHERE ");
			sql.append(paramColumn).append("= ?");
			if (constraintKey != null && constraintValue != null) {
				sql.append(" AND ").append(constraintKey).append("= ?");
			}
			updateStatement = j.getPreparedStatement(sql.toString());
			
			// prepare the insert statement
			sql = new StringBuilder(200);
			sql.append("INSERT INTO ").append(tbl).append("(").append(paramColumn).append(",")
				.append(valueColumn);
			if (constraintKey != null && constraintValue != null) {
				sql.append(",").append(constraintKey);
			}
			sql.append(") VALUES (").append("?").append(",").append("?");
			if (constraintKey != null && constraintValue != null) {
				sql.append(",?");
			}
			sql.append(")");
			insertStatement = j.getPreparedStatement(sql.toString());
			
			while (it.hasNext()) {
				String a = (String) it.next();
				String v = get(a, null);
				selectStatement.setString(1, a);
				if (constraintKey != null && constraintValue != null) {
					selectStatement.setString(2, constraintValue);
				}
				// String
				// sql="SELECT wert FROM "+tbl+" WHERE "+constraint+" AND param="+JdbcLink.wrap(a);
				ResultSet res = selectStatement.executeQuery();
				if (res.next()) {
					String existingValue = res.getString(1);
					if (existingValue != null && !existingValue.equals(v)) {
						if (v == null) {
							deleteStatement.setString(1, a);
							if (constraintKey != null && constraintValue != null) {
								deleteStatement.setString(2, constraintValue);
							}
							deleteStatement.executeUpdate();
							// sql=new
							// StringBuilder("DELETE from "+tbl+" WHERE "+constraint+" AND param="+JdbcLink.wrap(a))
						} else {
							updateStatement.setString(1, v);
							updateStatement.setString(2, a);
							if (constraintKey != null && constraintValue != null) {
								updateStatement.setString(3, constraintValue);
							}
							updateStatement.executeUpdate();
							// sql=new
							// StringBuilder("UPDATE "+tbl+" SET wert="+JdbcLink.wrap(v)+" WHERE "+constraint+" AND param="+JdbcLink.wrap(a));
						}
					}
				} else {
					if (v == null) {
						continue;
					}
					insertStatement.setString(1, a);
					insertStatement.setString(2, v);
					if (constraintKey != null && constraintValue != null) {
						insertStatement.setString(3, constraintValue);
					}
					insertStatement.executeUpdate();
					// sql="INSERT INTO "+tbl+" (param,wert,"+cn[0]+") VALUES ("+JdbcLink.wrap(a)+","+JdbcLink.wrap(v)+","+cn[1]+")";
				}
				res.close();
			}
		} catch (SQLException e) {
			throw JdbcLinkExceptionTranslation.translateException(e);
		} finally {
			j.releasePreparedStatement(selectStatement);
			j.releasePreparedStatement(deleteStatement);
			j.releasePreparedStatement(updateStatement);
			j.releasePreparedStatement(insertStatement);
		}
	}
	
	private String unwrap(String wrapped){
		if (wrapped.startsWith("\'") && wrapped.endsWith("\'")) {
			return wrapped.substring(1, wrapped.length() - 1);
		}
		return wrapped;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.rgw.tools.Settings#undo()
	 */
	public void undo(){
		PreparedStatement selectStatement = null;
		try {
			String constraintKey = null;
			String constraintValue = null;
			if (constraint != null) {
				String[] constraintParts = constraint.split("=");
				if (constraintParts.length == 2) {
					constraintKey = unwrap(constraintParts[0]);
					constraintValue = unwrap(constraintParts[1]);
				}
			}
			StringBuilder sql = new StringBuilder(300);
			sql.append("SELECT * FROM ").append(tbl);
			if (constraintKey != null && constraintValue != null) {
				sql.append(" WHERE ").append(constraintKey).append("= ?");
			}
			selectStatement = j.getPreparedStatement(sql.toString());
			if (constraintKey != null && constraintValue != null) {
				selectStatement.setString(1, constraintValue);
			}
			ResultSet resultSet = selectStatement.executeQuery();
			while ((resultSet != null) && resultSet.next()) {
				String parm = resultSet.getString(paramColumn);
				String val = resultSet.getString(valueColumn);
				set(parm, val);
			}
			cleaned();
		} catch (SQLException e) {
			throw JdbcLinkExceptionTranslation.translateException(e);
		} finally {
			j.releasePreparedStatement(selectStatement);
		}
	}
}
