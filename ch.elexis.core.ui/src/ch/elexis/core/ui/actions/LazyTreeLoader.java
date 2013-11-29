/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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
import ch.rgw.tools.LazyTree;
import ch.rgw.tools.LazyTree.LazyTreeListener;
import ch.rgw.tools.Tree;

/**
 * Ein Job, der eine Baumstruktur "Lazy" aus der Datenbank lädt. D.h. es werden immer nur die gerade
 * benötigten Elemente geladen. Die Baumstruktur muss so in einer Tabelle abgelegt sein, dass eine
 * Spalte auf das Elternelement verweist.
 * 
 * @author gerry
 * 
 * @param <T>
 */
@Deprecated
public class LazyTreeLoader<T> extends AbstractDataLoaderJob implements LazyTreeListener {
	String parentColumn;
	String parentField;
	IFilter filter;
	IProgressMonitor monitor;
	
	public LazyTreeLoader(final String Jobname, final Query q, final String parent,
		final String[] orderBy){
		super(Jobname, q, orderBy);
		setReverseOrder(true);
		parentColumn = parent;
	}
	
	public void setFilter(final IFilter f){
		filter = f;
		if (isValid() == true) {
			((Tree) result).setFilter(f);
		}
	}
	
	public void setParentField(final String f){
		parentField = f;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IStatus execute(final IProgressMonitor moni){
		monitor = moni;
		if (monitor != null) {
			monitor.subTask(getJobname());
		}
		result = new LazyTree<T>(null, null, filter, this);
		qbe.clear();
		qbe.add(parentColumn, "=", "NIL"); //$NON-NLS-1$ //$NON-NLS-2$
		List<T> list = load();
		for (T t : list) {
			((LazyTree) result).add(t, this);
			if (monitor != null) {
				monitor.worked(1);
			}
		}
		if (filter != null) {
			((Tree) result).setFilter(filter);
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public int getSize(){
		return qbe.size();
	}
	
	@SuppressWarnings("unchecked")
	public boolean fetchChildren(final LazyTree l){
		qbe.clear();
		PersistentObject obj = (PersistentObject) l.contents;
		if (obj != null) {
			qbe.add(parentColumn, "=", parentField == null ? obj.getId() : obj.get(parentField)); //$NON-NLS-1$
			List ret = load();
			for (PersistentObject o : (List<PersistentObject>) ret) {
				l.add(o, this);
			}
			return ret.size() > 0;
		}
		return false;
	}
	
	public boolean hasChildren(final LazyTree l){
		fetchChildren(l);
		return (l.getFirstChild() != null);
	}
	
}
