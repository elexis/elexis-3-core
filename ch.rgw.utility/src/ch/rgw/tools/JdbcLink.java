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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;
import java.util.logging.Level;

/**
 * Weiterer Abstraktionslayer zum einfacheren Zugriff auf eine jdbc-fähige Datenbank
 */

public class JdbcLink {
	public static final String getVersion(){
		return "3.2.1";
	}
	
	public int lastErrorCode;
	public String lastErrorString;
	public int verMajor = 0;
	public int verMinor = 0;
	public String DBFlavor = null;
	private String sDrv;
	private String sConn;
	private String sUser;
	private String sPwd;
	java.sql.Connection conn = null;
	private Vector<Stm> statements;
	public int keepStatements = 10;
	private boolean bPoolable = true;
	
	private static Log log;
	
	public static final int CONNECT_SUCCESS = 0;
	public static final int CONNECT_CLASSNOTFOUND = 1;
	public static final int CONNECT_FAILED = 2;
	public static final int CONNECT_UNKNOWN_ERROR = 10;
	public static final int TRANSACTION_COMMIT_FAILED = 21;
	public static final int TRANSACTION_ROLLBACK_FAILED = 22;
	public static final int TRANSACTION_COMMIT_NOT_SUPPORTED = 23;
	public static final int CONNECTION_CANT_CREATE_STATEMENT = 30;
	public static final int CONNECTION_CANT_PREPARE_STAMENT = 31;
	public static final int CONNECTION_SQL_ERROR = 40;
	
	static {
		log = Log.get("jdbcLink");
	}
	
	@SuppressWarnings("unused")
	private JdbcLink(){ /* intentionally blank */
	}
	
	/**
	 * Bequemlichkeitsmethode, um einen JdcbLink auf eine MySQL-Datenbank zu erhalten
	 */
	public static JdbcLink createMySqlLink(String host, String database){
		log.log(Level.INFO, "Creating MySQL-Link");
		String driver = "com.mysql.jdbc.Driver";
		String[] hostdetail = host.split(":");
		String hostname = hostdetail[0];
		String hostport = hostdetail.length > 1 ? hostdetail[1] : "3306";
		String connect = "jdbc:mysql://" + hostname + ":" + hostport + "/" + database;
		return new JdbcLink(driver, connect, "mysql");
	}
	
	/**
	 * Bequemlichkeitsmethode, um einen JdbcLink auf eine InProcess laufende HSQL-Datenbank zu
	 * erhalten
	 * 
	 * @param database
	 *            ein Dateiname für die zu erzeugende bzw. zu verwendende Datenbank
	 */
	public static JdbcLink createInProcHsqlDBLink(String database){
		log.log(Level.INFO, "Creating HSQL-In-Proc-Link");
		String driver = "org.hsqldb.jdbcDriver";
		String connect = "jdbc:hsqldb:" + database;
		return new JdbcLink(driver, connect, "hsqldb");
	}
	
	/**
	 * Bequemlichkeitsmethode, um einen JdbcLink auf eine als Server laufende HSQL-Datenbank zu
	 * erhalten
	 * 
	 * @param host
	 *            Server, auf dem die Datenbank läuft.
	 */
	public static JdbcLink createHsqlDBLink(String host){
		log.log(Level.INFO, "Creating HSQL-Link");
		String driver = "org.hsqldb.jdbcDriver";
		String connect = "jdbc:hsqldb:hsql://" + host;
		return new JdbcLink(driver, connect, "hsqldb");
	}
	
	/**
	 * Bequemlichkeitsmethode, um einen Link auf eine H2-Datenbank zu bekommen. Da der
	 * mysql-compatibility-mode für ALTER commands nicht korrekt funktioniert, wird ein h2 DBFlavor
	 * für die übersetzung der Statements übergeben.
	 * 
	 * @param database
	 * @return
	 */
	public static JdbcLink createH2Link(String database){
		log.log(Level.INFO, "Creating H2-Link");
		String driver = "org.h2.Driver";
		String prefix = "jdbc:h2:";
		if (database.contains(".zip!")) {
			prefix += "zip:";
		}
		String connect = prefix + database + ";AUTO_SERVER=TRUE";
		return new JdbcLink(driver, connect, "h2");
	}
	
	/**
	 * Bequemlichkeitsmethode für einen JdbcLink auf einen 4D-Server
	 * 
	 * @param host
	 *            de Server, auf dem die 4D-Datenbnak läuft
	 * @return
	 */
	public static JdbcLink create4DLink(String host){
		log.log(Level.INFO, "Creating 4D-Link");
		String driver = "com.fourd.jdbc.DriverImpl";
		String connect = "jdbc:4d:" + host + ":19813";
		return new JdbcLink(driver, connect, "4d");
	}
	
	/**
	 * Bequemlichkeitsmethode für einen JdbcLink auf einen PostgreSQL- Server
	 * 
	 * @param host
	 * @return
	 */
	public static JdbcLink createPostgreSQLLink(String host, String database){
		log.log(Level.INFO, "Creating PostgreSQL-Link");
		String driver = "org.postgresql.Driver";
		String[] hostdetail = host.split(":");
		String hostname = hostdetail[0];
		String hostport = hostdetail.length > 1 ? hostdetail[1] : "5432";
		
		String connect = "jdbc:postgresql://" + hostname + ":" + hostport + "/" + database;
		return new JdbcLink(driver, connect, "postgresql");
	}
	
	public static JdbcLink createODBCLink(String dsn){
		log.log(Level.INFO, "Creating ODBC-Link");
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
		String connect = "jdbc:odbc:" + dsn;
		return new JdbcLink(driver, connect, "ODBC");
	}
	
	/**
	 * Erstelle einen neuen jdbcLink. Es wird kein Connect-Versuch gemacht, das heisst, der
	 * Konstruktor wird nie scheitern.
	 * 
	 * @param driver
	 *            Treiber-String (wie org.hsql.jdbc)
	 * @param connect
	 *            Connect-String (wie jdbc:odbc:data)
	 */
	public JdbcLink(String driver, String connect, String flavor){
		sDrv = driver;
		sConn = connect;
		DBFlavor = flavor.toLowerCase();
	}
	
	/**
	 * Verbindung zur Datenbank herstellen
	 * 
	 * TODO return value is always true because exception is thrown on error
	 * 
	 * @param user
	 *            Username, kann null sein
	 * @param password
	 *            Passwort, kann null sein
	 * @return errcode
	 * 
	 * @throws JdbcLinkException
	 */
	public boolean connect(String user, String password){
		Exception cause = null;
		try {
			// Driver
			// D=(Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
			sUser = user;
			sPwd = password;
			Driver D = (Driver) Class.forName(sDrv).newInstance();
			verMajor = D.getMajorVersion();
			verMinor = D.getMinorVersion();
			
			// Class.forName("org.firebirdsql.jdbc.FBDriver");
			// "jdbc:mysql://<host>:<port>/<dbname>"
			// "jdbc:odbc:<dsn>
			log.log(Level.INFO, "Loading database driver " + sDrv);
			log.log(Level.INFO, "Connecting with database " + sConn);
			conn = getConnectionWithRetry(user, password);
			statements = new Vector<Stm>();
			lastErrorCode = CONNECT_SUCCESS;
			lastErrorString = "Connect successful";
			log.log("Connect successful", Log.DEBUGMSG);
			return true;
		} catch (ClassNotFoundException ex) {
			lastErrorCode = CONNECT_CLASSNOTFOUND;
			lastErrorString = "Class not found exception: " + ex.getMessage();
			cause = ex;
		} catch (SQLException ex) {
			lastErrorCode = CONNECT_FAILED;
			lastErrorString = "SQL exception: " + ex.getMessage();
			cause = ex;
		} catch (InstantiationException e) {
			lastErrorCode = CONNECT_UNKNOWN_ERROR;
			lastErrorString = "Instantiation exception: " + e.getMessage();
			cause = e;
		} catch (IllegalAccessException e) {
			lastErrorCode = CONNECT_UNKNOWN_ERROR;
			lastErrorString = "Illegal access exception: " + e.getMessage();
			cause = e;
		}
		throw JdbcLinkExceptionTranslation.translateException("Connect failed: " + lastErrorString,
			cause);
	}
	
	private Connection getConnectionWithRetry(String user, String password) throws SQLException{
		SQLException lastException;
		int retryCount = 5;
		do {
			try {
				return DriverManager.getConnection(sConn, user, password);
			} catch (SQLException sqlEx) {
				lastException = sqlEx;
				// The two SQL states that are 'retry-able' are communication error or concurrency
				JdbcLinkException je = JdbcLinkExceptionTranslation.translateException(sqlEx);
				if (je instanceof JdbcLinkResourceException
					|| je instanceof JdbcLinkConcurrencyException) {
					retryCount--;
				} else {
					retryCount = 0;
				}
			}
		} while (retryCount > 0);
		throw lastException;
	}
	
	private void checkConn(){
		if (conn == null)
			throw new JdbcLinkException("Connection not valid!");
	}
	
	public JdbcLink(Connection c){
		conn = c;
		statements = new Vector<Stm>();
	}
	
	public void setPoolable(boolean poolable){
		bPoolable = poolable;
	}
	
	/**
	 * Utility-Funktion zum Einpacken von Strings in Hochkommata und escapen illegaler Zeichen
	 * 
	 * @param s
	 *            der String
	 * @return Datenbankkonform eingepackte String
	 */
	public static String wrap(String s){
		if (StringTool.isNothing(s)) {
			return "''";
		}
		try {
			return wrap(s.getBytes("UTF-8"), "mysql");
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return wrap(s.getBytes(), "mysql");
		}
	}
	
	public String wrapFlavored(String s){
		if (StringTool.isNothing(s)) {
			return "''";
		}
		try {
			return wrap(s.getBytes("UTF-8"), DBFlavor);
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return wrap(s.getBytes(), DBFlavor);
		}
	}
	
	/**
	 * Utility-Funktion zum Datenbankkonformen Verpacken von byte arrays zwecks Einfügen in
	 * BLOB-Felder.
	 * 
	 * @param flavor
	 *            TODO
	 * @param b
	 *            das rohe byte array
	 * @return das verpackte array in Form eines String
	 */
	public static String wrap(byte[] in, String flavor){
		
		byte[] out = new byte[2 * in.length + 2];
		int j = 0;
		out[j++] = '\'';
		for (int i = 0; i < in.length; i++) {
			switch (in[i]) {
			case 0:
			case 34:
				
			case '\'':
				if (flavor.startsWith("hsql")) {
					out[j++] = '\'';
					break;
				}
			case 92:
				boolean before = (i > 1 && in[i - 1] == 92);
				boolean after = (i < in.length - 1 && in[i + 1] == 92);
				if (!before && !after) {
					out[j++] = '\\';
				}
			}
			out[j++] = in[i];
		}
		out[j++] = '\'';
		try {
			return new String(out, 0, j, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return null;
		}
	}
	
	public Connection getConnection(){
		return conn;
	}
	
	public String getDriverName(){
		return sDrv;
	}
	
	public String getConnectString(){
		return sConn;
	}
	
	/**
	 * Ent Statement aus dem pool beziehen. Jedes mit getStatement bezogene Statement MUSS mit
	 * releaseStatement wieder zurückgegeben werden.
	 * 
	 * @return ein Stm (JdbcLink-spezifische Statement-Variante)
	 */
	public Stm getStatement(){
		if (!bPoolable) {
			return createStatement();
		}
		if (statements == null) {
			lastErrorCode = CONNECT_UNKNOWN_ERROR;
			lastErrorString = "Keine Verbindung zur Datenbank";
			throw new JdbcLinkException(lastErrorString);
		}
		synchronized (statements) {
			if (statements.isEmpty()) {
				return createStatement();
			} else {
				Stm stm = statements.remove(0);
				if (stm.isClosed()) {
					return createStatement();
				} else {
					return stm;
				}
				
			}
		}
	}
	
	private Stm createStatement(){
		try {
			return new Stm();
		} catch (SQLException ex) {
			lastErrorCode = CONNECTION_CANT_CREATE_STATEMENT;
			lastErrorString = ex.getMessage();
			throw JdbcLinkExceptionTranslation.translateException(lastErrorString, ex);
		}
	}
	
	/**
	 * Ein Stm - Statement in den pool zurückgeben. Die Zahl der im pool zu haltenden Statements
	 * wird mit keepStatements definiert.
	 * 
	 * @param s
	 */
	
	public void releaseStatement(Stm s){
		if (!bPoolable) {
			s.delete();
		}
		synchronized (statements) {
			if (s != null) {
				if (statements.size() < keepStatements) {
					statements.add(s);
				} else {
					s.delete();
				}
			}
		}
	}
	
	/**
	 * Ein Prepared Statement anlegen
	 * 
	 * @param sql
	 *            Abfrage für das statement (eizusetzende Parameter müssen als ? gesetzt sein
	 * @return das vorkompilierte PreparedStatement
	 */
	public PreparedStatement prepareStatement(String sql){
		checkConn();
		try {
			return conn.prepareStatement(sql);
		} catch (SQLException ex) {
			lastErrorCode = CONNECTION_CANT_PREPARE_STAMENT;
			lastErrorString = ex.getMessage();
			throw JdbcLinkExceptionTranslation.translateException(lastErrorString, ex);
		}
	}
	
	public static final int INTEGRAL = 1;
	public static final int TEXT = 2;
	public static final int BINARY = 3;
	public static final int OTHER = 4;
	
	public static int generalType(int t){
		switch (t) {
		case Types.BIGINT:
		case Types.BIT:
		case Types.BOOLEAN:
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.TINYINT:
			return INTEGRAL;
			
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.LONGVARCHAR:
			return TEXT;
			
		case Types.BINARY:
		case Types.BLOB:
		case Types.CLOB:
		case Types.LONGVARBINARY:
		case Types.VARBINARY:
			return BINARY;
			
		default:
			return OTHER;
			
		}
	}
	
	/**
	 * Einen String-Value aus dem aktuellen Datensatz des ResultSets holen. Es wird garantiert, dass
	 * immer etwas zurückgeliefert wird (" " für den leeren Sring)
	 * 
	 * @param r
	 *            ResultSet
	 * @param field
	 *            Feldname
	 * @return den String
	 * @throws Exception
	 *             Wenn das Feld nicht definiert ist.
	 */
	public static String getString(ResultSet r, String field) throws Exception{
		String res = r.getString(field);
		if (StringTool.isNothing(res)) {
			return " ";
		}
		
		return res;
	}
	
	/**
	 * Verbindung zur Datenbank lösen
	 * 
	 */
	public synchronized void disconnect(){
		try {
			while ((statements != null) && (!statements.isEmpty())) {
				Stm stm = statements.remove(0);
				stm.delete();
				stm = null;
			}
			if (conn != null) {
				conn.close();
			}
			statements = null;
			log.log("Disconnected", Log.INFOS);
		} catch (SQLException e) {
			ExHandler.handle(e);
		}
	}
	
	/**
	 * Anfrage, ob die Verbindung steht
	 * 
	 * @todo Muss implementiert werden
	 * @return true wenn die Verbindung steht.
	 */
	public boolean isAlive(){
		return true;
	}
	
	public boolean setAutoCommit(boolean value){
		if (conn == null)
			return false;
		try {
			conn.setAutoCommit(value);
			return true;
		} catch (SQLException e) {
			ExHandler.handle(e);
			lastErrorCode = TRANSACTION_COMMIT_NOT_SUPPORTED;
			lastErrorString = e.getMessage();
			return false;
		}
	}
	
	public boolean commit(){
		if (conn == null)
			return false;
		try {
			conn.commit();
			return true;
		} catch (SQLException e) {
			ExHandler.handle(e);
			lastErrorCode = TRANSACTION_COMMIT_FAILED;
			lastErrorString = e.getMessage();
			return false;
		}
		
	}
	
	public boolean rollback(){
		if (conn == null)
			return false;
		try {
			conn.rollback();
			return true;
		} catch (SQLException e) {
			ExHandler.handle(e);
			lastErrorCode = TRANSACTION_ROLLBACK_FAILED;
			lastErrorString = e.getMessage();
			return false;
		}
	}
	
	/**
	 * Unscharfes Suchen im ResultSet.
	 * 
	 * @param r
	 *            das zu durchsuchende ResultSet
	 * @param field
	 *            Name des interessiernden Felds
	 * @param m
	 *            (vorher konfigurierter) fuzzyMatcher mit der Suchbedingung
	 * @see ch.rgw.tools.FuzzyMatcher
	 * @return true wenn gefunden; das ResultSet steht auf der ersten oder einzigen Fundstelle.
	 */
	public static boolean nextMatch(ResultSet r, String field, FuzzyMatcher m){
		try {
			while (r.next()) {
				if (m.match(r.getString(field))) {
					return true;
				}
			}
			return false;
		} catch (SQLException ex) {
			ExHandler.handle(ex);
		}
		return false;
	}
	
	public String dbDriver(){
		return sDrv;
	}
	
	/**
	 * Einen String-Wert abfragen. Temporäres Statement erzeugen
	 * 
	 * @param sql
	 *            SQL-String, der ein VARCHAR-oder Text-Feld liefern sollte
	 * @return den gefundenen String oder null: nicht gefunden
	 */
	public String queryString(String sql){
		Stm stm = getStatement();
		String res = stm.queryString(sql);
		releaseStatement(stm);
		return res;
	}
	
	public int queryInt(String sql){
		Stm stm = getStatement();
		int res = stm.queryInt(sql);
		releaseStatement(stm);
		return res;
	}
	
	public boolean execScript(InputStream i, boolean translate, boolean stopOnError){
		Stm stm = getStatement();
		boolean ret = stm.execScript(i, translate, stopOnError);
		releaseStatement(stm);
		return ret;
	}
	
	/**
	 * Wrapper für Stm#exec
	 * 
	 * @author gerry
	 * 
	 */
	public int exec(final String sql){
		Stm stm = getStatement();
		int res = stm.exec(sql);
		releaseStatement(stm);
		return res;
	}
	
	public class Stm {
		private Statement stm;
		
		private void checkStm(){
			if (stm == null)
				throw new JdbcLinkException("Statement not valid!");
		}
		
		private boolean reconnect(){
			if (conn == null)
				return false;
			try {
				log.log(Level.WARNING, "Stm()Trying reconnect");
				connect(sUser, sPwd);
				stm = conn.createStatement();
				return true;
			} catch (SQLException ex) {
				log.log(Level.WARNING, "Reconnect failed " + ex.getMessage());
				lastErrorCode = ex.getErrorCode();
				lastErrorString = ex.getMessage();
				return false;
			} catch (JdbcLinkException je) {
				log.log(Level.WARNING, "Reconnect failed " + je.getMessage());
				return false;
			}
		}
		
		Stm() throws SQLException{
			checkConn();
			try {
				stm = conn.createStatement();
			} catch (SQLException se) {
				log.log(Level.WARNING, "need reconnect " + se.getMessage());
				if (!reconnect()) {
					throw se;
				}
			}
		}
		
		public boolean isClosed(){
			checkStm();
			if ("postgresql".equals(DBFlavor)) {
				return false;
			}
			try {
				return stm.isClosed();
			} catch (SQLException ex) {
				ExHandler.handle(ex);
				return false;
			} catch (UnsupportedOperationException ex) {
				ExHandler.handle(ex);
				return false;
			}
		}
		
		public void delete(){
			try {
				// stm.cancel();
				if (stm != null) {
					stm.close();
				}
			} catch (SQLException ex) {
				ExHandler.handle(ex);
				/* egal */
			}
			stm = null;
		}
		
		/**
		 * Eine execute auf die Datanbank ausführen
		 * 
		 * @param SQLText
		 *            Von der Datenbank verstandener SQL-String
		 * @return Zahl der affected rows.
		 */
		public int exec(final String sql){
			return internalExec(sql, false);
		}
		
		private int internalExec(final String SQLText, final boolean inError){
			checkStm();
			// log.log("executing " + SQLText, Log.DEBUGMSG);
			try {
				return stm.executeUpdate(SQLText);
			} catch (SQLException e) {
				if (!inError) {
					if (connect(sUser, sPwd)) {
						return internalExec(SQLText, true);
					}
				}
				throw JdbcLinkExceptionTranslation.translateException("Fehler bei: " + SQLText, e);
			}
		}
		
		/**
		 * Eine SQL-Anfrage an die Datenbank senden. Versucht bei einem Fehler zuerst die Verbindung
		 * wieder herzustellen
		 * 
		 * @param SQLText
		 *            ein Query String in von der Datenbank verstandener Syntax
		 * @return ein ResultSet oder null bei Fehler
		 * @throws JdbcException
		 */
		public ResultSet query(final String SQLText){
			return internalQuery(SQLText, false);
		}
		
		private ResultSet internalQuery(final String SQLText, final boolean inError){
			checkStm();
			ResultSet res = null;
			// log.log("querying " + SQLText, Log.DEBUGMSG);
			try {
				res = stm.executeQuery(SQLText);
				return res;
			} catch (SQLException e) {
				if (!inError) {
					// try to solve the problem with a simpel reconnect
					if (reconnect()) {
						log.log(Level.WARNING, "Reconnect");
						return internalQuery(SQLText, true);
					}
				}
				lastErrorString = e.getMessage();
				lastErrorCode = CONNECTION_SQL_ERROR;
				throw JdbcLinkExceptionTranslation.translateException(lastErrorString, e);
			}
		}
		
		/**
		 * Eine Anzahl Werte als Vector zurückliefern
		 * 
		 * @param sql
		 *            SQL-String, der die Werte liefert
		 * @param fields
		 *            interessierende Felder
		 * @return einen Vector aus Object[] Arrays mit den interessierenden Feldern aller
		 *         gefundenen Datensätze
		 */
		@SuppressWarnings("unchecked")
		public Vector queryList(String sql, String[] fields){
			Vector rs = new Vector();
			log.log("executing " + sql, Log.DEBUGMSG);
			ResultSet res = internalQuery(sql, false);
			try {
				if (res != null) {
					while (res.next()) {
						Object[] o = new Object[fields.length];
						for (int i = 0; i < fields.length; i++) {
							o[i] = res.getObject(fields[i]);
						}
						if (fields.length == 1) {
							rs.add(o[0]);
						} else {
							rs.add(o);
						}
					}
				}
			} catch (SQLException ex) {
				ExHandler.handle(ex);
			}
			return rs;
		}
		
		public String queryString(String sql){
			ResultSet res = internalQuery(sql, false);
			try {
				if (res != null && res.next()) {
					String r = res.getString(1);
					if ((r == null) || (r.equals("null")) || (r.equals(""))) {
						return "";
					}
					return r;
				}
				return null;
			} catch (SQLException ex) {
				ExHandler.handle(ex);
			}
			return null;
		}
		
		/**
		 * Einen Integer-Wert abfragen.
		 * 
		 * @param sql
		 *            SQL-String, der ein Integer-Feld liefern sollte
		 * @return den ersten der Suchbedingung entsprechenden Integer-Wert oder -1: Wert nicht
		 *         gefunden.
		 */
		public int queryInt(String sql){
			ResultSet res = internalQuery(sql, false);
			try {
				if (res != null && res.next()) {
					return res.getInt(1);
				}
			} catch (SQLException ex) {
				ExHandler.handle(ex);
			}
			return -1;
		}
		
		/**
		 * Ein SQL-Script einlesen und ausführen. alles nach # bis zum Zeilenende wird ignoriert
		 * 
		 * @param s
		 *            der InputStream mit dem Script
		 * @param translate
		 *            true, wenn das Script zu den bekannten Dialekten übersetzt werden soll
		 * @param stopOnError
		 *            true: Abbruch des Scripts, wenn ein Fehler auftritt
		 * @return false wenn ein Fehler passiert ist.
		 */
		public boolean execScript(InputStream s, boolean translate, boolean stopOnError){
			String sql = "<none>";
			if (s == null) {
				return false;
			}
			// autoc=conn.getAutoCommit();
			// setAutoCommit(false);
			while ((sql = readStatement(s)) != null) {
				log.log(sql, Log.DEBUGMSG);
				if (translate) {
					sql = translateFlavor(sql);
				}
				System.out.println(sql);
				try {
					stm.execute(sql);
				} catch (SQLException ex) {
					ExHandler.handle(ex);
					if (stopOnError == true) {
						return false;
					}
				}
			}
			// commit();
			return true;
		}
		
	}
	
	public static String readStatement(InputStream is){
		StringBuffer inp = new StringBuffer(1000);
		String sql = "<none>";
		try {
			int c;
			boolean comment = false;
			while (((c = is.read()) != -1)) {
				if (c == ';') {
					break;
				}
				if (c == '#') {
					comment = true;
				}
				if ((c == '\r') || (c == '\n')) {
					comment = false;
				}
				if (comment == false) {
					inp.append((char) c);
				}
			}
			// sql=inp.toString().replace("#.+$","");
			// sql=sql.replace("--[^\\r]*","").trim();
			sql = inp.toString().replaceAll("[\\n\\r\\t]", " ");
			sql = sql.replaceAll(" {2,}", " ").trim();
			if (sql.length() < 4) {
				return null;
			}
			return sql;
		} catch (IOException ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	public boolean dumpTable(BufferedWriter w, String name) throws Exception{
		Stm stm = getStatement();
		ResultSet res = stm.query("SELECT * from " + name);
		ResultSetMetaData rm = res.getMetaData();
		int cols = rm.getColumnCount();
		String[] ColNames = new String[cols];
		int[] colTypes = new int[cols];
		w.write("CREATE TABLE " + name + "(");
		for (int i = 0; i < cols; i++) {
			ColNames[i] = rm.getColumnName(i + 1);
			colTypes[i] = rm.getColumnType(i + 1);
			w.write(ColNames[i] + " " + colTypes[i] + ",\n");
		}
		w.write(");");
		
		while ((res != null) && (res.next() == true)) {
			w.write("INSERT INTO " + name + " (");
			for (int i = 0; i < cols; i++) {
				w.write(ColNames[i]);
				if (i < cols - 1) {
					w.write(",");
				}
			}
			w.write(") VALUES (");
			for (int i = 0; i < cols; i++) {
				Object o = res.getObject(ColNames[i]);
				switch (JdbcLink.generalType(colTypes[i])) {
				case JdbcLink.INTEGRAL:
					if (o == null) {
						w.write("0");
					} else {
						w.write(Integer.toString(((Integer) o).intValue()));
					}
					break;
				case JdbcLink.TEXT:
					if (o == null) {
						w.write(JdbcLink.wrap("null"));
					} else {
						w.write(JdbcLink.wrap((String) o));
					}
					break;
				
				default:
					String t = o.getClass().getName();
					log.log("Unknown type " + t, Log.ERRORS);
					throw new Exception("Cant write " + t);
					
				}
				if (i < cols - 1) {
					w.write(",");
				}
			}
			w.write(");");
			w.newLine();
		}
		res.close();
		releaseStatement(stm);
		return true;
	}
	
	/**
	 * Einen SQL-String in die bekannten flavors übersetzen. Basisdialekt ist mysql
	 */
	public String translateFlavor(String sql){
		// sql=sql.toLowerCase();
		// TODO: Konzept für case-sensitiveness klarer definieren
		if (DBFlavor.equalsIgnoreCase("postgresql")) {
			sql = sql.replaceAll("BLOB", "BYTEA");
			sql = sql.replaceAll("DROP INDEX (.+?) ON .+?;", "DROP INDEX $1;");
			sql = sql.replaceAll("MODIFY\\s+(\\w+)\\s+(.+)", "ALTER COLUMN $1 TYPE $2");
			sql = sql.replaceAll("SIGNED", "INT");
		} else if (DBFlavor.startsWith("hsqldb") || DBFlavor.startsWith("h2")) {
			sql = sql.replaceAll("TEXT", "LONGVARCHAR");
			sql = sql.replaceAll("BLOB", "LONGVARBINARY");
			sql = sql.replaceAll("CREATE +TABLE", "CREATE CACHED TABLE");
			sql = sql.replaceAll("DROP INDEX (.+?) ON .+?;", "DROP INDEX $1;");
			sql = sql.replaceAll("MODIFY (.+)", "ALTER COLUMN $1");
		} else if (DBFlavor.equalsIgnoreCase("mysql")) {
			sql = sql.replaceAll("BLOB", "LONGBLOB");
			sql = sql.replaceAll("TEXT", "LONGTEXT");
			/* experimental - do not use */
		} else if (DBFlavor.equalsIgnoreCase("db2")) {
			sql = sql.replaceAll("VARCHAR\\s*\\([0-9]+\\)", "VARCHAR");
			sql = sql.replaceAll("TEXT", "CLOB");
			/* /experimental */
		}
		return sql;
	}
}