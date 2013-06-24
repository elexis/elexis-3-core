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

import java.sql.ResultSet;
import java.util.Iterator;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

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
		Stm stm = j.getStatement();
		while (it.hasNext()) {
			String a = (String) it.next();
			String v = get(a, null);
			StringBuilder sql = new StringBuilder(300);
			sql.append("SELECT ").append(valueColumn).append(" FROM ").append(tbl)
				.append(" WHERE ");
			if (constraint != null) {
				sql.append(constraint).append(" AND ");
			}
			sql.append(paramColumn).append("=").append(JdbcLink.wrap(a));
			// String
			// sql="SELECT wert FROM "+tbl+" WHERE "+constraint+" AND param="+JdbcLink.wrap(a);
			if (stm.queryString(sql.toString()) != null) {
				sql = new StringBuilder(200);
				if (v == null) {
					sql.append("DELETE FROM ").append(tbl).append(" WHERE ");
					if (constraint != null) {
						sql.append(constraint).append(" AND ");
					}
					sql.append(paramColumn).append("=").append(JdbcLink.wrap(a));
					// sql=new
					// StringBuilder("DELETE from "+tbl+" WHERE "+constraint+" AND param="+JdbcLink.wrap(a))
				} else {
					sql.append("UPDATE ").append(tbl).append(" SET ").append(valueColumn)
						.append("=").append(JdbcLink.wrap(v)).append(" WHERE ");
					if (constraint != null) {
						sql.append(constraint).append(" AND ");
					}
					sql.append(paramColumn).append("=").append(JdbcLink.wrap(a));
					// sql=new
					// StringBuilder("UPDATE "+tbl+" SET wert="+JdbcLink.wrap(v)+" WHERE "+constraint+" AND param="+JdbcLink.wrap(a));
				}
			} else {
				if (v == null) {
					continue;
				}
				sql = new StringBuilder(200);
				String[] cn = null;
				sql.append("INSERT INTO ").append(tbl).append("(").append(paramColumn).append(",")
					.append(valueColumn);
				if (constraint != null) {
					cn = constraint.split("=");
					sql.append(",").append(cn[0]);
				}
				sql.append(") VALUES (").append(JdbcLink.wrap(a)).append(",")
					.append(JdbcLink.wrap(v));
				if (constraint != null) {
					sql.append(",").append(cn[1]);
				}
				sql.append(")");
				// sql="INSERT INTO "+tbl+" (param,wert,"+cn[0]+") VALUES ("+JdbcLink.wrap(a)+","+JdbcLink.wrap(v)+","+cn[1]+")";
			}
			stm.exec(sql.toString());
		}
		j.releaseStatement(stm);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.rgw.tools.Settings#undo()
	 */
	public void undo(){
		ResultSet r;
		Stm stm = j.getStatement();
		try {
			String sql = "SELECT * from " + tbl;
			if (constraint != null) {
				sql += " WHERE " + constraint;
			}
			r = stm.query(sql);
			while ((r != null) && r.next()) {
				String parm = r.getString(paramColumn);
				String val = r.getString(valueColumn);
				set(parm, val);
			}
			cleaned();
		} catch (Exception ex) {
			ExHandler.handle(ex);
		} finally {
			j.releaseStatement(stm);
		}
	}
	
}
