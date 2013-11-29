/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.LinkedList;
import java.util.List;

import ch.elexis.data.Query;

/**
 * BackgroundJob which loads data from the database in background. The definition of the access to
 * the data is left to tzzhe implementing class.
 * 
 * BackgroundJob, der für das Laden von Daten aus der Datenbank im Hintergrund zuständig ist. Die
 * konkrete Definition des Datenbankzugriffs ist Sache der implementierenden Unterklassen.
 * 
 * @deprecated Die Funktionalität der BackgroundJobs kann seit Eclipse 3.0 von Eclipse-Jobs und
 *             Joblisteners übernommen werden. Neuer Code sollte direkt mit Eclipse Jobs arbeiten.
 * @author Gerry
 * 
 */
public abstract class AbstractDataLoaderJob extends BackgroundJob {
	/**
	 * A FilterProvider is a class than can apply a filter to a DataLoader, that controls the read
	 * data.
	 * 
	 * Ein Filterprovider ist eine Klasse, die bei Bedarf einen Filter auf den Dataloader anwenden
	 * kann, welcher die zu lesenden Datensätze limitiert.
	 * 
	 * @author gerry
	 * 
	 */
	public interface FilterProvider {
		public void applyFilter();
	}
	
	protected Query qbe;
	protected String[] orderBy;
	private boolean orderReverse;
	protected LinkedList<FilterProvider> fp = new LinkedList<FilterProvider>();
	
	protected AbstractDataLoaderJob(String name, Query q, String[] order){
		super(name);
		qbe = q;
		orderBy = order;
	}
	
	protected List load(){
		if (fp != null) {
			for (AbstractDataLoaderJob.FilterProvider f : fp) {
				if (f != null) {
					f.applyFilter();
				}
			}
		}
		qbe.orderBy(orderReverse, orderBy);
		return qbe.execute();
	}
	
	/**
	 * set sorting order Umgekehrte Sortierreihenfolge setzen
	 * 
	 * @param reverse
	 *            true: sort reversely
	 */
	public void setReverseOrder(boolean reverse){
		orderReverse = reverse;
	}
	
	/**
	 * Request this job's Query object
	 * 
	 * Das Query-Objekt dieses Jobs erfragen
	 * 
	 * @return das Query-Objekt
	 * @see ch.elexis.core.datatypes.Query
	 */
	public Query getQuery(){
		return qbe;
	}
	
	/**
	 * Set Fields after which the data should be sorted
	 * 
	 * @param order
	 */
	public void setOrder(String... order){
		orderBy = order;
		invalidate();
	}
	
	public String[] getOrder(){
		return orderBy;
	}
	
	/**
	 * Einen FilterProvider einsetzen oder löschen
	 * 
	 * @param f
	 *            der Filterprovider
	 */
	public void addFilterProvider(FilterProvider f){
		fp.add(f);
	}
	
	public void removeFilterProvider(FilterProvider f){
		fp.remove(f);
	}
	
}
