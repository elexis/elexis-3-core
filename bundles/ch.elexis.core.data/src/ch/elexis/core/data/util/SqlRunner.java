/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.ElexisStatusProgressMonitor;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkException;

public class SqlRunner {
	static Logger log = LoggerFactory.getLogger(SqlRunner.class.getName());
	
	enum SqlStatus {
		NONE, EXECUTE, SUCCESS, FAIL
	};
	
	private List<String> sqlStrings;
	private List<UpdateDbSql> sql;
	private String pluginId;
	private JdbcLink jdbcl;
	
	public SqlRunner(String[] sql, String pluginId){
		sqlStrings = new ArrayList<String>();
		for (int i = 0; i < sql.length; i++) {
			sqlStrings.add(sql[i]);
		}
		this.pluginId = pluginId;
	}
	
	/**
	 * 
	 * @param sql
	 *            as in {@link SqlRunner#sql}
	 * @param pluginId
	 *            as in {@link SqlRunner#pluginId}
	 * @param jdbcl
	 *            optionally assign a jdbclink to use to perform the updates
	 */
	public SqlRunner(String[] sql, String pluginId, @Nullable
	JdbcLink jdbcl){
		this(sql, pluginId);
		this.jdbcl = jdbcl;
	}
	
	public boolean runSql(){
		sql = new ArrayList<UpdateDbSql>();
		// create UpdateDbSql objects from input list
		for (String sqlString : sqlStrings) {
			sql.add(new UpdateDbSql(sqlString));
		}
		
		// this code should only be reached during tests!
		IProgressMonitor ipm = new ElexisStatusProgressMonitor("Running DB Script", sql.size());
		ElexisEvent progress =
			new ElexisEvent(ipm, ElexisStatusProgressMonitor.class,
				ElexisEvent.EVENT_OPERATION_PROGRESS, ElexisEvent.PRIORITY_HIGH);
		ElexisEventDispatcher.getInstance().fire(progress);
		for (UpdateDbSql update : sql) {
			ipm.subTask(update.getSql());
			update.run();
			ipm.worked(1);
		}
		ipm.done();
		
		// determine if all updates were successful
		for (UpdateDbSql update : sql) {
			if (update.getStatus() == SqlStatus.FAIL)
				return false;
		}
		return true;
	}
	
	/**
	 * Class holding information about one sql and its execution
	 * 
	 * @author thomas
	 */
	protected class UpdateDbSql implements Runnable {
		private String sql;
		private SqlStatus status;
		
		protected UpdateDbSql(String sql){
			this.sql = sql;
			status = SqlStatus.NONE;
		}
		
		@Override
		public void run(){
			Stm statement = null;
			JdbcLink link = null;
			try {
				link = (jdbcl == null) ? PersistentObject.getConnection() : jdbcl;
				statement = link.getStatement();
				setStatus(SqlStatus.EXECUTE);
				// do not use execScript method here as it will catch the
				// exceptions
				ByteArrayInputStream scriptStream =
					new ByteArrayInputStream(this.sql.getBytes("UTF-8"));
				String sqlString;
				while ((sqlString = JdbcLink.readStatement(scriptStream)) != null) {
					try {
						statement.exec(link.translateFlavor(sqlString));
					} catch (JdbcLinkException e) {
						setStatus(SqlStatus.FAIL);
						log.error("Error " + e.getMessage() + " during db update", e);
						e.printStackTrace();
						try {
							ElexisStatus status =
								new ElexisStatus(ElexisStatus.ERROR, pluginId,
									ElexisStatus.CODE_NONE, "Error " + e.getMessage()
										+ " during db update", e);
							ElexisEventDispatcher.getInstance().fireElexisStatusEvent(status);
						} catch (AssertionFailedException appnotinit) {
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				setStatus(SqlStatus.FAIL);
				try {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, pluginId, ElexisStatus.CODE_NONE,
							"Error " + e.getMessage() + " during db update", e);
					ElexisEventDispatcher.fireElexisStatusEvent(status);
				} catch (AssertionFailedException appnotinit) {
					log.error("Error " + e.getMessage() + " during db update", appnotinit);
				}
				return;
			} finally {
				if (link != null && statement != null)
					link.releaseStatement(statement);
			}
			if (getStatus() == SqlStatus.EXECUTE)
				setStatus(SqlStatus.SUCCESS);
		}
		
		public void setStatus(SqlStatus status){
			this.status = status;
		}
		
		public SqlStatus getStatus(){
			return status;
		}
		
		public String getSql(){
			return sql;
		}
	}
}
