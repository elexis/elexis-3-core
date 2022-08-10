/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

/**
 * A PersistentObjectLoader for flat tables. This is also an
 * ILazyContentProvider for Structured Viewers and a ContentProvider for @see
 * CommonViewer
 *
 * @author Gerry
 *
 */
public class FlatDataLoader extends PersistentObjectLoader implements ILazyContentProvider {
	// private static final String LOADMESSAGE = "Lade Daten..."; //$NON-NLS-1$
	private List<? extends PersistentObject> filtered = null;

	public FlatDataLoader(CommonViewer cv, Query<? extends PersistentObject> qbe) {
		super(cv, qbe);
	}

	/**
	 * Constructor without CommonViewer. Do not in Connection with CommonViewers
	 *
	 * @param qbe
	 */
	public FlatDataLoader(Query<? extends PersistentObject> qbe) {
		super(null, qbe);
	}

	/**
	 * From @see DelayableJob.IWorker
	 */
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params) {
		if (isSuspended()) {
			return Status.CANCEL_STATUS;
		}
		final TableViewer tv = (TableViewer) cv.getViewerWidget();
		synchronized (this) {
			filtered = Collections.emptyList();
			setQuery();
			applyQueryFilters();
			if (orderFields != null) {
				qbe.orderBy(false, orderFields);
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			filtered = qbe.execute();
			int itemCount = filtered.size();
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			UiDesk.asyncExec(new Runnable() {
				public void run() {
					// Avoid access to disposed table
					if (tv != null && !tv.getTable().isDisposed()) {
						tv.setItemCount(0);
						tv.setItemCount(itemCount);
					}
				}
			});
		}

		return Status.OK_STATUS;
	}

	/**
	 * prepare the query so it returns the appropriate Objects on execute(). The
	 * default implemetation lets the ControlFieldProvider set the query. Subclasses
	 * may override
	 */
	protected void setQuery() {
		qbe.clear();
		ControlFieldProvider cfp = cv.getConfigurer().getControlFieldProvider();
		if (cfp != null) {
			cfp.setQuery(qbe);
		}
	}

	public void updateElement(int index) {
		if (filtered != null) {
			synchronized (this) {
				if (index >= 0 && index < filtered.size()) {
					Object o = filtered.get(index);
					if (o != null) {
						TableViewer tv = (TableViewer) cv.getViewerWidget();
						tv.replace(filtered.get(index), index);
					}
				}
			}
		}
	}

}
