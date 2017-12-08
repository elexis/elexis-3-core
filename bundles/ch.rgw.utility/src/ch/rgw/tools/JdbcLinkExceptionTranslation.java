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

package ch.rgw.tools;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Simple translator class for SQLExceptions. Currently translating to
 * <li>
 * {@link JdbcLinkResourceException} for connection problems, or resource problems on the database</li>
 * <li>
 * {@link JdbcLinkSyntaxException} for problems with the syntax of the SQL statement</li>
 * <li>
 * {@link JdbcLinkConcurrencyException} for problems with concurrent access</li>
 * </p>
 * <p>
 * Translation Sets based on the SQLStateSQLExceptionTranslator class of the spring framework.
 * </p>
 * 
 * @author thomas
 * 
 */
public class JdbcLinkExceptionTranslation {
	
	private static final Set<String> BAD_SQL_GRAMMAR_CODES = new HashSet<String>(8);
	
	private static final Set<String> DATA_INTEGRITY_VIOLATION_CODES = new HashSet<String>(8);
	
	private static final Set<String> DATA_ACCESS_RESOURCE_FAILURE_CODES = new HashSet<String>(8);
	
	private static final Set<String> TRANSIENT_DATA_ACCESS_RESOURCE_CODES = new HashSet<String>(8);
	
	private static final Set<String> CONCURRENCY_FAILURE_CODES = new HashSet<String>(4);
	
	static {
		BAD_SQL_GRAMMAR_CODES.add("07"); // Dynamic SQL error
		BAD_SQL_GRAMMAR_CODES.add("21"); // Cardinality violation
		BAD_SQL_GRAMMAR_CODES.add("2A"); // Syntax error direct SQL
		BAD_SQL_GRAMMAR_CODES.add("37"); // Syntax error dynamic SQL
		BAD_SQL_GRAMMAR_CODES.add("42"); // General SQL syntax error
		BAD_SQL_GRAMMAR_CODES.add("65"); // Oracle: unknown identifier
		BAD_SQL_GRAMMAR_CODES.add("S0"); // MySQL uses this - from ODBC error codes?
		
		DATA_INTEGRITY_VIOLATION_CODES.add("01"); // Data truncation
		DATA_INTEGRITY_VIOLATION_CODES.add("02"); // No data found
		DATA_INTEGRITY_VIOLATION_CODES.add("22"); // Value out of range
		DATA_INTEGRITY_VIOLATION_CODES.add("23"); // Integrity constraint violation
		DATA_INTEGRITY_VIOLATION_CODES.add("27"); // Triggered data change violation
		DATA_INTEGRITY_VIOLATION_CODES.add("44"); // With check violation
		
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("08"); // Connection exception
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("53"); // PostgreSQL: insufficient resources (e.g.
														// disk full)
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("54"); // PostgreSQL: program limit exceeded (e.g.
														// statement too complex)
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("57"); // DB2: out-of-memory exception / database not
														// started
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("58"); // DB2: unexpected system error
		
		TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("JW"); // Sybase: internal I/O error
		TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("JZ"); // Sybase: unexpected I/O error
		TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("S1"); // DB2: communication failure
		
		CONCURRENCY_FAILURE_CODES.add("40"); // Transaction rollback
		CONCURRENCY_FAILURE_CODES.add("61"); // Oracle: deadlock
	}
	
	public static JdbcLinkException translateException(Exception ex){
		if (ex instanceof SQLException) {
			return translateSQLException(null, (SQLException) ex);
		}
		return new JdbcLinkException(null, ex);
	}
	
	public static JdbcLinkException translateException(String message, Exception ex){
		if (ex instanceof SQLException) {
			return translateSQLException(message, (SQLException) ex);
		}
		return new JdbcLinkException(message, ex);
	}
	
	private static JdbcLinkException translateSQLException(String message, SQLException sql){
		String state = sql.getSQLState();
		
		if (state != null && state.length() >= 2) {
			String stateClass = state.substring(0, 2);
			
			if (BAD_SQL_GRAMMAR_CODES.contains(stateClass)) {
				return new JdbcLinkSyntaxException(message + " (SQLState: " + state + ")", sql);
			} else if (DATA_ACCESS_RESOURCE_FAILURE_CODES.contains(stateClass)) {
				return new JdbcLinkResourceException(message + " (SQLState: " + state + ")", sql);
			} else if (CONCURRENCY_FAILURE_CODES.contains(stateClass)) {
				return new JdbcLinkConcurrencyException(message + " (SQLState: " + state + ")", sql);
			}
		}
		return new JdbcLinkException(message + " (SQLState: " + state + ")", sql);
	}
}
