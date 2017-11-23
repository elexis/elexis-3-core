/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Class for manipulation of Multiplikator. <br>
 * 
 * @author thomashu
 * 
 */
public class MultiplikatorList {
	private java.util.List<MultiplikatorInfo> list;
	private String typ;
	private String table;
	
	public MultiplikatorList(String table, String typ){
		this.typ = typ;
		this.table = table;
	}
	
	/**
	 * Update multiRes with ResultSet of all existing Multiplikators
	 */
	private void fetchResultSet(){
		Stm statement = PersistentObject.getConnection().getStatement();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(table).append(" WHERE TYP=").append(JdbcLink.wrap(typ));
		ResultSet res = statement.query(sql.toString());
		try {
			list = new ArrayList<MultiplikatorList.MultiplikatorInfo>();
			while (res.next()) {
				list.add(new MultiplikatorInfo(res.getString("DATUM_VON"), res
					.getString("DATUM_BIS"), res.getString("MULTIPLIKATOR")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (statement != null)
				PersistentObject.getConnection().releaseStatement(statement);
			
			if (res != null) {
				try {
					res.close();
					res = null;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void insertMultiplikator(TimeTool dateFrom, String value){
		TimeTool dateTo = null;
		Stm statement = PersistentObject.getConnection().getStatement();
		try {
			fetchResultSet();
			Iterator<MultiplikatorInfo> iter = list.iterator();
			// update existing multiplier for that date
			while (iter.hasNext()) {
				MultiplikatorInfo info = iter.next();
				TimeTool fromDate = new TimeTool(info.validFrom);
				TimeTool toDate = new TimeTool(info.validTo);
				if (dateFrom.isAfter(fromDate) && dateFrom.isBefore(toDate)) { // if contains
																				// update the to
																				// value of the
																				// existing
																				// multiplikator
					StringBuilder sql = new StringBuilder();
					// update the old to date
					TimeTool newToDate = new TimeTool(dateFrom);
					newToDate.addDays(-1);
					sql.append("UPDATE ")
						.append(table)
						.append(
							" SET DATUM_BIS="
								+ JdbcLink.wrap(newToDate.toString(TimeTool.DATE_COMPACT))
								+ " WHERE DATUM_VON="
								+ JdbcLink.wrap(fromDate.toString(TimeTool.DATE_COMPACT))
								+ " AND TYP=" + JdbcLink.wrap(typ));
					statement.exec(sql.toString());
					// set to date of new multiplikator to to date of old multiplikator
					dateTo = new TimeTool(toDate);
				} else if (dateFrom.isEqual(fromDate)) { // if from equals update the value
					StringBuilder sql = new StringBuilder();
					// update the value and return
					TimeTool newToDate = new TimeTool(dateFrom);
					newToDate.addDays(-1);
					sql.append("UPDATE ")
						.append(table)
						.append(
							" SET MULTIPLIKATOR=" + JdbcLink.wrap(value) + " WHERE DATUM_VON="
								+ JdbcLink.wrap(fromDate.toString(TimeTool.DATE_COMPACT))
								+ " AND TYP=" + JdbcLink.wrap(typ));
					statement.exec(sql.toString());
					return;
				}
			}
			// if we have not found a to Date yet search for oldest existing
			if (dateTo == null) {
				fetchResultSet();
				iter = list.iterator();
				dateTo = new TimeTool("99991231");
				while (iter.hasNext()) {
					MultiplikatorInfo info = iter.next();
					TimeTool fromDate = new TimeTool(info.validFrom);
					if (fromDate.isBefore(dateTo)) {
						dateTo.set(fromDate);
						dateTo.addDays(-1);
					}
				}
			}
			// create a new entry
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ")
				.append(table)
				.append(
					" (ID,DATUM_VON,DATUM_BIS,MULTIPLIKATOR,TYP) VALUES ("
						+ JdbcLink.wrap(StringTool.unique("prso")) +","
						+ JdbcLink.wrap(dateFrom.toString(TimeTool.DATE_COMPACT)) + ","
						+ JdbcLink.wrap(dateTo.toString(TimeTool.DATE_COMPACT)) + ","
						+ JdbcLink.wrap(value) + "," + JdbcLink.wrap(typ) + ");");
			statement.exec(sql.toString());
		} finally {
			PersistentObject.getConnection().releaseStatement(statement);
		}
	}
	
	public void removeMultiplikator(TimeTool dateFrom, String value){
		PreparedStatement statement = PersistentObject.getDefaultConnection().getPreparedStatement(getPreparedStatementSql());
		try {
			statement.setString(1, value);
			statement.setString(2, dateFrom.toString(TimeTool.DATE_COMPACT));
			statement.setString(3, typ);
			statement.execute();
		} catch (SQLException e) {
			LoggerFactory.getLogger(getClass()).error("Could not delete multiplikator", e);
		} finally {
			PersistentObject.getDefaultConnection().releasePreparedStatement(statement);
		}
	}
	
	private String getPreparedStatementSql(){
		return "DELETE FROM " + table + " WHERE MULTIPLIKATOR=? AND DATUM_VON=? AND TYP=?";
	}
	
	public synchronized double getMultiplikator(TimeTool date){
		// get Mutliplikator for date
		fetchResultSet();
		Iterator<MultiplikatorInfo> iter = list.iterator();
		while (iter.hasNext()) {
			MultiplikatorInfo info = iter.next();
			TimeTool fromDate = new TimeTool(info.validFrom);
			TimeTool toDate = new TimeTool(info.validTo);
			if (date.isAfterOrEqual(fromDate) && date.isBeforeOrEqual(toDate)) {
				String value = info.multiplikator;
				if (value != null && !value.isEmpty()) {
					try {
						return Double.parseDouble(value);
					} catch (NumberFormatException nfe) {
						ExHandler.handle(nfe);
						return 0.0;
					}
				}
			}
		}
		return 1.0;
	}
	
	private static class MultiplikatorInfo {
		String validFrom;
		String validTo;
		String multiplikator;
		
		MultiplikatorInfo(String validFrom, String validTo, String multiplikator){
			this.validFrom = validFrom;
			this.validTo = validTo;
			this.multiplikator = multiplikator;
		}
	}
	
	private static String[] getEigenleistungUseMultiSystems(){
		String systems =
			CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, "");
		return systems.split("\\|\\|");
	}
	
	public static boolean isEigenleistungUseMulti(String system){
		String[] systems = getEigenleistungUseMultiSystems();
		for (String string : systems) {
			if (system.equals(string)) {
				return true;
			}
		}
		return false;
	}
	
	public static void setEigenleistungUseMulti(String system){
		String systems =
			CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, "");
		if (!systems.isEmpty()) {
			systems = systems.concat("||");
		}
		systems = systems.concat(system);
		CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, systems);
		CoreHub.globalCfg.flush();
	}
	
	public static void removeEigenleistungUseMulti(String system){
		String[] systems = getEigenleistungUseMultiSystems();
		StringBuilder sb = new StringBuilder();
		for (String string : systems) {
			if (!system.equals(string)) {
				if (!(sb.length() == 0)) {
					sb.append("||");
				}
				sb.append(string);
			}
		}
		CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS,
			sb.toString());
		CoreHub.globalCfg.flush();
	}
}
