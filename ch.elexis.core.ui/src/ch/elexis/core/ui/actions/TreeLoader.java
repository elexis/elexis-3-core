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

package ch.elexis.core.ui.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.Tree;

/**
 * Ein BackgroundJob, der Datensätze aus einer Tabelle einliest, und als Tree zurückliefert
 * 
 * @author gerry
 * 
 * @see ch.rgw.tools.Tree
 */
@Deprecated
public class TreeLoader<T> extends AbstractDataLoaderJob {
	private String parentColumn;
	// private int worked;
	private IProgressMonitor monitor;
	// private boolean loadAll;
	private IFilter filter;
	
	/**
	 * Der einzige Konstruktor
	 * 
	 * @param Jobname
	 *            Name für den Background-Job
	 * @param q
	 *            Query, die die Datensätze liefert
	 * @param parent
	 *            Name des Felds, das auf das �bergeordnete Element verweist
	 * @param orderBy
	 *            Felder, nach denen sortiert werden soll
	 * @see ch.elexis.core.datatypes.Query
	 */
	public <U> TreeLoader(String Jobname, Query q, String parent, String[] orderBy){
		super(Jobname, q, orderBy);
		parentColumn = parent;
		filter = null;
	}
	
	/**
	 * Einen Filter auf den Tree setzen
	 * 
	 * @param f
	 *            ein Filter
	 */
	public void setFilter(IFilter f){
		filter = f;
		if (isValid() == true) {
			((Tree) result).setFilter(f);
		}
	}
	
	/**
	 * Diesen Job synchron ausführen. Normalerweise sollte ein Dataloader aber asynchron viea
	 * Hub.jobPool.activate oder Hub.jobPool.Queue ausgeführt werden. execute() eignet sich nur,
	 * wenn man gleich auf das Ergebnis warten will.
	 * 
	 * @see JobPool
	 * @see AbstractDataLoaderJob
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public IStatus execute(IProgressMonitor moni){
		monitor = moni;
		// worked=0;
		if (monitor != null) {
			monitor.subTask(getJobname());
		}
		
		result = new Tree<T>(null, null, filter);
		loadChildren((Tree<T>) result, "NIL"); //$NON-NLS-1$
		return Status.OK_STATUS;
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	private void loadChildren(Tree<T> branch, String parent){
		qbe.clear();
		qbe.add(parentColumn, "=", parent); //$NON-NLS-1$
		List<T> list = load();
		for (T t : list) {
			Tree<T> ch = branch.add(t);
			if (monitor != null) {
				monitor.worked(1);
			}
			loadChildren(ch, ((PersistentObject) t).getId());
		}
	}
	
	public int getSize(){
		return qbe.size();
	}
	
}
