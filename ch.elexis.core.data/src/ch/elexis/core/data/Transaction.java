/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.data;

import java.util.ArrayList;

/**
 * Transaktionsorientierte Datenspeicherung
 * 
 * @author Gerry
 * @deprecated we don't need this really
 */
@Deprecated
public class Transaction {
	ArrayList<String> fields, values;
	PersistentObject o;
	
	private Transaction(){/* leer */}
	
	/**
	 * Transaktion für ein Objekt beginnen. Alle nun folgenden append()- Operationen werden zunächst
	 * nur in diese Transaktion �bernommen, und erst bei commit() in die Datenbank �bertragen.
	 * 
	 * @param o
	 *            das Objekt, für das die Transaktion erstellt wird
	 * @see commit()
	 */
	Transaction(PersistentObject o){
		this.o = o;
		fields = new ArrayList<String>();
		values = new ArrayList<String>();
	}
	
	/**
	 * Einen neuen Speichervorgang der Transaktion zuf�gen
	 * 
	 * @param field
	 *            Feld
	 * @param value
	 *            wert
	 */
	public void append(String field, String value){
		fields.add(field);
		values.add(value);
	}
	
	/**
	 * Einen Integer Wert der Transaktion zufügen
	 * 
	 * @param field
	 *            Feld
	 * @param value
	 *            Wert
	 */
	public void append(String field, int value){
		fields.add(field);
		values.add(Integer.toString(value));
	}
	
	/**
	 * Der Transaktion mehrere Felder auf einmal zufügen
	 * 
	 * @param flds
	 *            Liste der Feldnamen
	 * @param vals
	 *            Liste der Werte (müssen gleichviele sein wie fields)
	 */
	public void append(String[] flds, String... vals){
		for (int i = 0; i < flds.length; i++) {
			fields.add(flds[i]);
			values.add(vals[i]);
		}
	}
	
	/**
	 * Aktuelle Transaktion in die Datenbank schreiben. Alle zuvor eingetragenen Werte werden
	 * fortgeschrieben. Die Transaktion ist anschliessend wieder leer und kann erneut benutzt
	 * werden.
	 */
	public synchronized void commit(){
		PersistentObject.getConnection().setAutoCommit(false);
		o.set((String[]) fields.toArray(), (String[]) values.toArray());
		PersistentObject.getConnection().commit();
		PersistentObject.getConnection().setAutoCommit(true);
		rollback();
	}
	
	/**
	 * Transaktion abbrechen. Alle seit Erstellung der Transaktion bzw. dem letzten commit()
	 * gemachten �nderungen verfallen.
	 */
	public void rollback(){
		fields.clear();
		values.clear();
	}
}
