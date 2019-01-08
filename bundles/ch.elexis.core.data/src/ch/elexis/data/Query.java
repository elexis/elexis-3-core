/*******************************************************************************
 * Copyright (c) 2005-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - case insenitive add()
 * 	  MEDEVIT <office@medevit.at>
 *******************************************************************************/

package ch.elexis.data;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Query manages all database queries of PersistentObjects and derived classes
 * 
 * Die Query-Klasse erledigt alle Datenbankabfragen auf PersistentObjects und davon abgeleitete
 * Klassen.
 * 
 * @author Gerry Weirich
 */

public class Query<T> {
	private static Logger log = LoggerFactory.getLogger(Query.class);
	
	public static final String EQUALS = "=";
	public static final String GREATER = ">";
	public static final String LESS = "<";
	public static final String LESS_OR_EQUAL = "<=";
	public static final String GREATER_OR_EQUAL = ">=";
	public static final String NOT_EQUAL = "<>";
	
	private final static String SELECT_ID_FROM = "SELECT ID FROM ";
	public static final String LIKE = "LIKE";
	private String link = " WHERE ";
	private final boolean clearEntityCache;
	
	private StringBuilder sql;
	private PersistentObject template;
	private Method load;
	private String lastQuery = "";
	private final LinkedList<IFilter> postQueryFilters = new LinkedList<IFilter>();
	private String ordering;
	private final ArrayList<String> exttables = new ArrayList<String>(2);
	
	private final String[] ID_FETCH_VAL = new String[] {
		"ID"
	};
	private final String[] fetchVals;
	
	/**
	 * @param cl
	 *            the class to apply the query on
	 */
	public Query(@NonNull final Class<? extends PersistentObject> cl){
		this(cl, null, null);
	}
	
	/**
	 * convenience constructor adding a query condition
	 * 
	 * @param cl
	 *            the class to apply the query on
	 * @param field
	 * @param value
	 *            value that field should be equal with
	 */
	public Query(final Class<? extends PersistentObject> cl, @Nullable final String field,
		@Nullable final String value){
		this(cl, field, value, null, (String[]) null);
	}
	
	/**
	 * Initialize a query with optional pre-fetch
	 * 
	 * @param cl
	 * @param field
	 *            an initial condition that {@link #EQUALS} to the parameter value, can be
	 *            <code>null</code>
	 * @param value
	 * @param tableName
	 *            the name of the database table the values are stored in
	 * @param prefetch
	 *            array of values to pre-fetch, or <code>null</code>. If not <code>null</code>, must
	 *            map to columns in the database.
	 * @throws UnsupportedOperationException
	 *             for unsupported pre-fetch fields
	 * @since 3.1
	 */
	public Query(final Class<? extends PersistentObject> cl, @Nullable
	final String field, @Nullable
	final String value, @Nullable
	final String tableName, @Nullable
	final String[] prefetch){
		
		this(cl, tableName, field, value, false, prefetch);
	}
	
	/**
	 * Initialize a query with optional value prefetch (i.e. cache population)
	 * 
	 * @param cl
	 * @param tableName
	 *            the name of the database table the values are stored in
	 * @param field
	 *            an initial condition that {@link #EQUALS} to the parameter value, can be
	 *            <code>null</code>
	 * @param value
	 * @param clearCache
	 *            clears all cached attributes for this element, only populating the cache with the
	 *            current prefetches. ONLY use on objects supporting
	 *            {@link PersistentObject#clearCachedAttributes()}
	 * @param prefetch
	 *            array of values to pre-fetch, or <code>null</code>. If not <code>null</code>, must
	 *            map to columns in the database.
	 * @throws UnsupportedOperationException
	 *             for unsupported prefetch fields, or on objects not supporting
	 *             {@link #clearEntityCache}
	 * @since 3.7
	 */
	public Query(final Class<? extends PersistentObject> cl, String tableName, @Nullable
	final String field, @Nullable
	final String value, boolean clearCache, String[] prefetch){
		try {
			// load class first to make sure field mapping is initialized
			template = CoreHub.poFactory.createTemplate(cl);
			load = cl.getMethod("load", new Class[] {
				String.class
			});
			
			clearEntityCache = clearCache;
			
			if (prefetch != null) {
				// resolve the delivered field names to the real database columns
				// consider the resp. datatypes stored
				List<String> mappedPrefetchValues =
					new ArrayList<String>(Arrays.asList(ID_FETCH_VAL));
				for (int i = 0; i < prefetch.length; i++) {
					String map = PersistentObject.map(tableName, prefetch[i]);
					if (!map.contains(":")) {
						mappedPrefetchValues.add(map);
					} else if (map.startsWith("S:")) {
						mappedPrefetchValues.add(map.substring(4));
					} else {
						throw new UnsupportedOperationException(
							"prefetch value not supported: " + prefetch[i] + " maps to " + map);
					}
				}
				
				fetchVals = mappedPrefetchValues.toArray(new String[] {});
			} else {
				fetchVals = ID_FETCH_VAL;
			}
			clear(false);
			
			if (field != null && value != null) {
				add(field, EQUALS, value);
			}
			
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Query: Konnte Methode load auf " + cl.getName() + " nicht auflösen", ex,
					ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		}
	}
	
	/**
	 * Convenience constructor
	 * 
	 * @param cl
	 * @param clearCache
	 * @param prefetch
	 * @see #Query(Class, String, String, boolean, String[])
	 * @since 3.7
	 */
	public Query(final Class<? extends PersistentObject> cl, String tableName, boolean clearCache, String[] prefetch){
		this(cl, tableName, null, null, clearCache, prefetch);
	}
	
	/**
	 * This method allows to set a custom sql query string; E.g. The original Query does not support
	 * the usage of INNER JOINS, to use them nevertheless we need to provide a direct method to set
	 * query strings
	 * 
	 * @param cl
	 *            the persistent object to set the query for
	 * @param string
	 *            the SQL query string
	 * @author Marco Descher
	 */
	public Query(Class<? extends PersistentObject> cl, final String string){
		try {
			template = CoreHub.poFactory.createTemplate(cl);
			load = cl.getMethod("load", new Class[] {
				String.class
			});
			sql = new StringBuilder(500);
			sql.append(string);
			ordering = null;
			fetchVals = ArrayUtils.EMPTY_STRING_ARRAY;
			clearEntityCache = false;
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Query: Konnte Methode load auf " + cl.getName() + " nicht auflösen", ex,
					ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		}
	}

	/**
	 * Delete query to e.g. re-use the query for a new execution run
	 * 
	 * @see #clear(boolean)
	 */
	public void clear(){
		clear(false);
	}
	
	/**
	 * Delete query to e.g. re-use the query for a new execution run
	 * 
	 * @param includeDeletedEntriesInQuery
	 *            to include deleted elements in your query initialize your query with
	 *            {@link #clear(boolean)} == <code>true</code>, the default as executed by
	 *            {@link #clear()} is <code>false</code>
	 */
	public void clear(boolean includeDeletedEntriesInQuery){
		sql = new StringBuilder(500);
		String table = template.getTableName();
		
		sql.append("SELECT ");
		for (int i = 0; i < fetchVals.length; i++) {
			sql.append(fetchVals[i]);
			if (i + 1 < fetchVals.length) {
				sql.append(", ");
			}
		}
		sql.append(" FROM ").append(table);
		
		String cns = template.getConstraint();
		if (cns.equals("")) {
			if (includeDeletedEntriesInQuery) {
				link = " WHERE ";
			} else {
				sql.append(" WHERE deleted=").append(JdbcLink.wrap("0"));
				link = " AND ";
			}
		} else {
			sql.append(" WHERE ").append(cns);
			if (!includeDeletedEntriesInQuery) {
				sql.append(" AND deleted=").append(JdbcLink.wrap("0"));
			}
			link = " AND ";
		}
		ordering = null;
		exttables.clear();
	}
	
	private void append(final String... s){
		sql.append(link);
		for (String a : s) {
			sql.append(StringConstants.SPACE).append(a);
		}
		if (link.equals(" WHERE ") || link.equals(StringConstants.EMPTY)) {
			link = " AND ";
		}
	}
	
	/**
	 * Folgende Ausdrücke bis endGroup gruppieren
	 */
	public void startGroup(){
		append("(");
		link = StringConstants.EMPTY;
	}
	
	/**
	 * Gruppierung ende
	 */
	public void endGroup(){
		sql.append(")");
	}
	
	/** Bedingung einsetzen, die immer erfüllt ist */
	public void insertTrue(){
		append("1=1");
	}
	
	/** Bedingung einsetzen, die nie erfüllt ist */
	public void insertFalse(){
		append("1=0");
	}
	
	/**
	 * AND-Verknüpfung anfügen.
	 */
	public void and(){
		if (link.equals(" OR ")) {
			link = " AND ";
		}
	}
	
	/**
	 * OR-Verknüpfung anfügen
	 */
	public void or(){
		link = " OR ";
	}
	
	/**
	 * Bedingung zufügen. Mehrere Bedingungen können hinzugefügt werden, indem jeweils zwischen zwei
	 * add() Aufrufen and() oder or() aufgerufen wird. Die Abfrage wird noch nicht ausgeführt,
	 * sondern erst beim abschliessenden execute().
	 *
	 * @param feld
	 *            Das Feld, für das die Bedingung gilt
	 * @param operator
	 *            Vergleich (z.B. "=", "LIKE", ">", "<")
	 * @param wert
	 *            Der Wert, der gesucht wird. Für Wildcard suche kann der Wert % enthalten, der
	 *            Operator muss dann aber "LIKE" sein.
	 *            Falls ein Feldname angegeben wird, wird die Vergleichsoperation auf dem Feld
	 *            ausgeführt. Es ist auch <code>null</code> erlaubt.
	 * @param toLower
	 *            ei true werden die Parameter mit der SQL-Funktion "lower()" in Kleinschreibung
	 *            umgewandelt, so dass die Gross-/Kleinschreibung egal ist.
	 * @return bei Fehler in der Syntax oder nichtexistenten Feldern
	 */
	public boolean add(final String feld, String operator, String wert, final boolean toLower){
		String mapped = template.map(feld);
		String mappedValue =
			(wert == null) ? null : template.map(template.getTableName(), wert, false);
		// treat date parameter separately
		// TODO This works only for european-style dates (dd.mm.yyyy)
		if (mapped.startsWith("S:D:")) {
			mapped = mapped.substring(4);
			// if a date should be matched partially
			wert = (wert == null) ? StringConstants.EMPTY : wert;
			if (operator.equalsIgnoreCase("LIKE") && !wert.matches("[0-9]{8,8}")) {
				StringBuilder sb = null;
				wert = wert.replaceAll("%", "");
				final String filler = "%%%%%%%%";
				// are we looking for the year?
				if (wert.matches("[0-9]{3,}")) {
					sb = new StringBuilder(wert);
					sb.append(filler);
					wert = sb.substring(0, 8);
				} else {
					// replace single digits as in 1.2.1932 with double digits
					// as in 01.02.1932
					wert = wert.replaceAll("[^0-9]([0-9])\\.", "0$1.");
					// remove dots
					sb = new StringBuilder(wert.replaceAll("\\.", ""));
					// String must consist of 8 or more digits (ddmmYYYY)
					sb.append(filler);
					// convert to YYYYmmdd format
					wert = sb.substring(4, 8) + sb.substring(2, 4) + sb.substring(0, 2);
				}
			} else {
				TimeTool tm = new TimeTool();
				if (tm.set(wert) == true) {
					wert = tm.toString(TimeTool.DATE_COMPACT);
				}
			}
		} else if (mapped.startsWith("EXT:")) {
			int ix = mapped.indexOf(':', 5);
			if (ix == -1) {
				log.error("Ungültiges Feld " + feld);
				return false;
			}
			String table = mapped.substring(4, ix);
			mapped = table + "." + mapped.substring(ix + 1);
			String firsttable = template.getTableName() + ".";
			if (!exttables.contains(table)) {
				exttables.add(table);
				sql.insert(SELECT_ID_FROM.length(), table + ",");
				ix = sql.indexOf("deleted=");
				if (ix != -1) {
					sql.insert(ix, firsttable);
				}
			}
			
			if (exttables.size() == 1) {
				sql.insert(7, firsttable); // Select ID from
				// firsttable,secondtable
			}
			append(table + ".ID=" + firsttable + "ID");
			// append(mapped,operator,wert);
			
		} else if ((mapped.startsWith("S:N:"))) {
			mapped = mapped.substring(4);
		} else if (mapped.matches(".*:.*")) {
			log.error("Ungültiges Feld " + feld);
			return false;
		}
		
		if (wert == null) {
			if (operator.equalsIgnoreCase("is") || operator.equals("=")) {
				// let's be a bit fault tolerant
				operator = "";
			} else if(NOT_EQUAL.equalsIgnoreCase(operator)){
				operator = "NOT";
			}
			append(mapped, "IS", operator, "NULL");
		} else {
			if (mappedValue != null && !mappedValue.equals(wert)) {
				if(mappedValue.startsWith("S:N:")) {
					mappedValue = mappedValue.substring(4);
				}
				append(mapped, operator, mappedValue);
			} else {
				wert = PersistentObject.getDefaultConnection().wrapFlavored(wert);
				if (toLower) {
					mapped = "lower(" + mapped + ")";
					wert = "lower(" + wert + ")";
				}
				append(mapped, operator, wert);

			}
		}
		
		return true;
	}
	
	public boolean add(final String feld, final String operator, final String wert){
		return add(feld, operator, wert, false);
	}
	
	/** Unverändertes Token in den SQL-String einfügen */
	public void addToken(final String token){
		append(token);
	}
	
	/**
	 * Bequemlichkeitsmethode für eine Abfrage, die nur einen einzigen Treffer liefern soll. Die
	 * Syntax ist wie bei der add() Methode, aber die Abfrage wird gleich ausgeführt
	 * 
	 * @param f
	 *            Feld
	 * @param op
	 *            Vergleichsoperator (s. auch unter add())
	 * @param v
	 *            Wert (@see Query#add() )
	 * @return Die ID des gefundenen Objekts oder null, wenn nicht gefunden
	 */
	public String findSingle(final String f, final String op, final String v){
		clear();
		sql.append(link).append(template.map(f)).append(op).append(JdbcLink.wrap(v));
		String ret = PersistentObject.getConnection().queryString(sql.toString());
		return ret;
	}
	
	/**
	 * Finds one entry - in difference to {@link #findSingle(String, String, String)} the value is
	 * wrapped by the underlying db flavor
	 * 
	 * @param f
	 * @param op
	 * @param v
	 * @param wrapValue
	 * @return
	 */
	public String findOne(final String f, final String op, final String v){
		clear();
		JdbcLink connection = PersistentObject.getConnection();
		sql.append(link).append(template.map(f)).append(op).append(connection.wrapFlavored(v));
		String ret = connection.queryString(sql.toString());
		return ret;
	}
	
	/**
	 * Bequemlichkeitsmethode, um gleich mehrere Felder auf einmal anzugeben, welche mit AND
	 * verknüpft werden. Dies ist dasselbe, wie mehrere Aufrufe nacheinander von add() und and(),
	 * aber die Abfrage wird gleich ausgeführt und die Resultate werden nach den übergebenen Feldern
	 * sortiert, in der Reihenfolge, in der sie übergeben wurden.
	 * 
	 * @param fields
	 *            Die Felder, die in die abfrage eingesetzt werden sollen
	 * @param values
	 *            die Werte, nach denen gesucht werden soll. Wenn values für ein Feld leer ist (null
	 *            oder ""), dann wird dieses Feld aus der Abfrage weggelassen
	 * @param exact
	 *            false, wenn die Abfrage mit LIKE erfolgen soll, sonst mit =
	 * @return eine Liste mit den gefundenen Objekten
	 */
	public List<T> queryFields(final String[] fields, final String[] values, final boolean exact){
		clear();
		String op = EQUALS;
		if (exact == false) {
			op = " LIKE ";
		}
		and();
		for (int i = 0; i < fields.length; i++) {
			if (StringTool.isNothing(values[i])) {
				continue;
			}
			add(fields[i], op, values[i]);
		}
		return execute();
	}
	
	/**
	 * Sortierung angeben. Dies muss als letzter Befehl nach einer Reihe von add() Sequenzen
	 * erfolgen.
	 * 
	 * @param reverse
	 *            true bei umgekehrter Sortierung
	 * @param n1
	 *            Beliebig viele Strings, die in absteigender Priorität die Felder angeben, nach
	 *            denen sortiert werden soll.
	 */
	public void orderBy(final boolean reverse, final String... n1){
		if (n1 != null && n1.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(" ORDER BY ");
			for (String s : n1) {
				String mapped = template.map(s);
				if (mapped.matches("[A-Z]{2,}:.+")) {
					log.error("Ungültiges Feld " + s);
					return;
				}
				if (mapped.startsWith("S:D:") || mapped.startsWith("S:N:")) {
					mapped = mapped.substring(4);
				}
				sb.append(mapped);
				if (reverse == true) {
					sb.append(" DESC");
				}
				sb.append(",");
			}
			sb.delete(sb.length() - 1, 10000);
			ordering = sb.toString();
		}
	}
	
	/**
	 * Die zusammengestellte Abfrage ausführen Dies kann aufgerufen werden, nachdem alle nötigen
	 * add(), AND(), or() und orderBy() Operationen eingegeben wurden und liefert das Ergebnis
	 * dieser Abfrage. execute() kann mit derselben Abfrage beliebig oft aufgerufen werden (und kann
	 * unzterschiedliche Resultate liefern, wenn von anderer Stelle zwischenzeitlich eine Änderung
	 * der Datenbank erfolgte)
	 * 
	 * @return eine Liste aus Objekten, die das Resultat der Abfrage sind.
	 */
	public List<T> execute(){
		return execute(PersistentObject.getDefaultConnection());
	}
	
	/**
	 * Execute the query on the specified DBConnection.
	 * 
	 * @param connection
	 * @return
	 */
	public List<T> execute(DBConnection connection){
		LinkedList<T> ret = new LinkedList<T>();
		return (List<T>) execute(ret, connection);
	}
	
	public Collection<T> execute(final Collection<T> collection){
		return execute(collection, PersistentObject.getDefaultConnection());
	}
	
	/**
	 * Execute the query on the specified DBConnection. The collection will be used to store the
	 * results.
	 * 
	 * @param connection
	 * @return
	 */
	public Collection<T> execute(final Collection<T> collection, DBConnection connection){
		if (ordering != null) {
			sql.append(ordering);
		}
		lastQuery = sql.toString();
		return queryExpression(lastQuery, collection, connection);
	}
	
	/**
	 * Execute the {@link PreparedStatement} on the database.
	 * 
	 * @param ps
	 * @param values
	 * @return
	 */
	public ArrayList<String> execute(final PreparedStatement ps, final String[] values){
		try {
			for (int i = 0; i < values.length; i++) {
				ps.setString(i + 1, values[i]);
			}
			if (ps.execute() == true) {
				ArrayList<String> ret = new ArrayList<String>();
				ResultSet res = ps.getResultSet();
				while (res.next()) {
					ret.add(res.getString(1));
				}
				return ret;
			}
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Fehler beim Ausführen von " + sql.toString(), ex, ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		}
		return null;
	}
	
	/**
	 * Eine komplexe selbst zusammengestellte Abfrage ausführen. Die Methoden von Query erlauben
	 * eine einfache Zusammenstellung einer SQL-Abfrage, Für spezielle Fälle will man aber
	 * vielleicht die SQL-Abfrage doch selber direkt angeben. Dies kann hier erfolgen.
	 * 
	 * @param expr
	 *            ein für die verwendete Datenbank akzeptabler SQL-String. Es soll nach Möglichkeit
	 *            nur Standard-SQL verwendet werden, um sich nicht von einer bestimmten Datenbank
	 *            abhängig zu machen. Die Abfrage muss nur nach dem Feld ID fragen; das Objekt wird
	 *            von query selbst hergestellt.
	 * @return Eine Liste der Objekte, die als Antwort auf die Anfrage geliefert wurden.
	 */
	@SuppressWarnings("unchecked")
	public Collection<T> queryExpression(final String expr, Collection<T> ret){
		return queryExpression(expr, ret, PersistentObject.getDefaultConnection());
	}
	
	/**
	 * Execute the query on the database using the specified {@link DBConnection}.
	 * 
	 * @param expr
	 * @param ret
	 * @param connection
	 * @return
	 */
	public Collection<T> queryExpression(final String expr, Collection<T> ret,
		DBConnection connection){
		if (ret == null) {
			ret = new LinkedList<T>();
		}
		// loaded objects should use provided connection if it is not the default connection
		boolean setConnection = connection != PersistentObject.getDefaultConnection();
		
		Stm stm = connection.getStatement();
		try (ResultSet res = stm.query(expr)) {
			log.debug("Executed " + expr);
			while ((res != null) && (res.next() == true)) {
				final String id = res.getString(1);
				T o = (T) load.invoke(null, new Object[] {
					id
				});
				if (o == null) {
					continue;
				} else if (setConnection) {
					((PersistentObject) o).setDBConnection(connection);
				}
				
				PersistentObject po = (PersistentObject) o;
				
				if(clearEntityCache) {
					po.clearCachedAttributes();
				}
				
				if (fetchVals.length > 1) {
					for (int i = 1; i < fetchVals.length; i++) {
						Object prefetchVal = res.getObject(i + 1);
						po.putInCache(fetchVals[i], prefetchVal);
					}
				}
				
				boolean bAdd = true;
				for (IFilter fi : postQueryFilters) {
					if (fi.select(o) == false) {
						bAdd = false;
						break;
					}
				}
				if (bAdd == true) {
					ret.add(o);
				}
				
			}
			return ret;
			
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Fehler bei Datenbankabfrage " + ex.getMessage(), ex, ElexisStatus.LOG_ERRORS);
			log.warn("Fehler [{}] bei Datenbankabfrage:\n[{};]", ex.getMessage(), expr);
			throw new PersistenceException(status);
		} finally {
			connection.releaseStatement(stm);
		}
	}
	
	/**
	 * Die Grösse des zu erwartenden Resultats abfragen. Dieses Resultat stimmt nur ungefähr, da es
	 * bis zur tatsächlichen Abfrage noch Änderungen geben kann, und da allfällige postQueryFilter
	 * das Resultat verkleinern könnten.
	 * 
	 * @return die ungefähre Zahl der erwarteten Objekte.
	 */
	public int size(){
		try {
			Stm stm = PersistentObject.getConnection().getStatement();
			String res = stm.queryString("SELECT COUNT(*) FROM " + template.getTableName());
			PersistentObject.getConnection().releaseStatement(stm);
			return Integer.parseInt(res);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return 10000;
		}
	}
	
	public String getLastQuery(){
		return lastQuery;
	}
	
	public String getActualQuery(){
		return sql.toString();
	}
	
	/**
	 * PostQueryFilters sind Filter-Objeckte, die <i>nach</i> der Datenbankanfrage auf das
	 * zurückgelieferte Resultat angewendet werden. Diese sind weniger effizient, als Filter, die
	 * bereits im Query-String enthalten sind, aber sie erlauben Datenbankunabhängig feinere
	 * Filterungen. Sie sind auch die einzige Möglichkeit, auf komprimierte oder codierte Felder zu
	 * filtern.
	 * 
	 * @param f
	 *            ein Filter
	 */
	public void addPostQueryFilter(final IFilter f){
		postQueryFilters.add(f);
	}
	
	public void removePostQueryFilter(final IFilter f){
		postQueryFilters.remove(f);
	}
}