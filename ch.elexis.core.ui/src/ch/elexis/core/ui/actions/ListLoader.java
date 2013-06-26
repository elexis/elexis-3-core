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

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;

/**
 * Ein Background-Job, der Datensätze aus einer Tabelle liest und in Form eines Arrays zurückliefert
 * 
 * @author gerry
 * 
 */
public class ListLoader<T extends PersistentObject> extends AbstractDataLoaderJob {
	
	/**
	 * Der einzige KOnstruktor
	 * 
	 * @param name
	 *            Name für diesen background-Job
	 * @param q
	 *            Query, die die passenden Datensätze liefert
	 * @param order
	 *            Felder, nach denen sortiert werden soll
	 */
	public ListLoader(String name, Query q, String[] order){
		super(name, q, order);
	}
	
	/**
	 * Ladevorgang synchron ausführen. Üblicherweise sollte das aber nicht execute, sondern über
	 * JobPool.activate bzw. Hub.jobPool.Queue erfolgen, was den job asynchron ausführt
	 * 
	 * @see JobPool
	 */
	public IStatus execute(IProgressMonitor monitor){
		qbe.clear();
		List list = load();
		if (list != null) {
			result = list.toArray();
			return Status.OK_STATUS;
		}
		return new Status(Status.ERROR,
			"ch.elexis", 1, Messages.getString("ListLoader.CouldntLoadData"), null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public int getSize(){
		return qbe.size();
	}
	
}
