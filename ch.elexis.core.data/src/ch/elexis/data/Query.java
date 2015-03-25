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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static final String LIKE = "LIKE";
	private StringBuilder sql;
	private PersistentObject template;
	private Method load;
	private final static String SELECT_ID_FROM = "SELECT ID FROM ";
	private String link = " WHERE ";
	private String lastQuery = "";
	private final LinkedList<IFilter> postQueryFilters = new LinkedList<IFilter>();
	private String ordering;
	private final ArrayList<String> exttables = new ArrayList<String>(2);
	
	/**
	 * Konstruktor
	 * 
	 * @param cl
	 *            Die Klasse, auf die die Abfrage angewendet werden soll (z.B. Patient.class)
	 */
	public Query(final Class<? extends PersistentObject> cl){
		this(cl, null, null);
	}
	
	/**
	 * Bequemlichkeits-Konstruktor, der gleich eine Bedingung einträgt
	 * 
	 * @param cl
	 *            Klasse, auf die Abfrage angewendet wird
	 * @param field
	 *            Feldname
	 * @param value
	 *            Gesuchter Wert von Feldname
	 */
	public Query(@NonNull final Class<? extends PersistentObject> cl, @Nullable final String field,
		@Nullable final String value){
		try {
			template = CoreHub.poFactory.createTemplate(cl);
			load = cl.getMethod("load", new Class[] {
				String.class
			});
			clear();
			if (field != null && value != null) {
				add(field, "=", value);
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
			// template=cl.newInstance();
			load = cl.getMethod("load", new Class[] {
				String.class
			});
			sql = new StringBuilder(500);
			sql.append(string);
			ordering = null;
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
		sql.append(SELECT_ID_FROM).append(table);
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
			sql.append(" ").append(a);
		}
		if (link.equals(" WHERE ") || link.equals("")) {
			link = " AND ";
		}
	}
	
	/**
	 * Folgende Ausdrücke bis endGroup gruppieren
	 */
	public void startGroup(){
		append("(");
		link = "";
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
	 * Bedingung zufügen. Mehrere Bedingungen können hinzugefügt werden, indem jeweils zwischen
	 * zwei add() Aufrufen and() oder or() aufgerufen wird.
	 * Die Abfrage wird noch nicht ausgeführt, sondern erst beim abschliessenden execute()
	 * @param feld	Das Feld, für das die Bedingung gilt
	 * @param operator Vergleich (z.B. "=", "LIKE", ">", "<")
	 * @param wert Der Wert, der gesucht wird. Für Wildcard suche kann der Wert % enthalten,
	 * der Operator muss dann aber "LIKE" sein
	 * @param toLower bei true werden die Parameter mit der SQL-Funktion "lower()" in
     * Kleinschreibung umgewandelt, so dass die Gross-/Kleinschreibung egal ist.
	 * @return false bei Fehler in der Syntax oder nichtexistenten Feldern
	 */
	public boolean add(final String feld, String operator, String wert, final boolean toLower){
		String mapped;
		mapped = template.map(feld);
		// treat date parameter separately
		// TODO This works only for european-style dates (dd.mm.yyyy)
		if (mapped.startsWith("S:D:")) {
			mapped = mapped.substring(4);
			// if a date should be matched partially
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
			
		} else if (mapped.matches(".*:.*")) {
			log.error("Ungültiges Feld " + feld);
			return false;
		}
		
		if (wert == null) {
			if (operator.equalsIgnoreCase("is") || operator.equals("=")) {
				// let's be a bit fault tolerant
				operator = "";
			}
			append(mapped, "is", operator, "null");
		} else {
			wert = PersistentObject.getConnection().wrapFlavored(wert);
			// wert = JdbcLink.wrap(wert);
			if (toLower) {
				mapped = "lower(" + mapped + ")";
				wert = "lower(" + wert + ")";
			}
			append(mapped, operator, wert);
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
		String op = "=";
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
	
	public PreparedStatement getPreparedStatement(final PreparedStatement previous){
		try {
			if (previous != null) {
				previous.close();
			}
			PreparedStatement ps =
				PersistentObject.getConnection().prepareStatement(sql.toString());
			return ps;
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Fehler beim PreparedStatement " + ex.getMessage(), ex, ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		}
	}
	
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
				if (mapped.startsWith("S:D:")) {
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
		if (ordering != null) {
			sql.append(ordering);
		}
		lastQuery = sql.toString();
		// log.log("Executing query: "+lastQuery,Log.DEBUGMSG);
		LinkedList<T> ret = new LinkedList<T>();
		return (List<T>) queryExpression(lastQuery, ret);
	}
	
	public Collection<T> execute(final Collection<T> collection){
		if (ordering != null) {
			sql.append(ordering);
		}
		lastQuery = sql.toString();
		return queryExpression(lastQuery, collection);
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
		if (ret == null) {
			ret = new LinkedList<T>();
		}
		Stm stm = null;
		try {
			stm = PersistentObject.getConnection().getStatement();
			ResultSet res = stm.query(expr);
			log.debug("Executed " + expr);
			while ((res != null) && (res.next() == true)) {
				String id = res.getString(1);
				T o = (T) load.invoke(null, new Object[] {
					id
				});
				if (o == null) {
					continue;
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
			res.close();
			return ret;
			
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Fehler bei Datenbankabfrage " + ex.getMessage(), ex, ElexisStatus.LOG_ERRORS);
			log.warn("Fehler bei Datenbankabfrage: " + ex.getMessage());
			throw new PersistenceException(status);
		} finally {
			PersistentObject.getConnection().releaseStatement(stm);
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