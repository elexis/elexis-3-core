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

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.ui.actions.DelayableJob.IWorker;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

/**
 * This is a replacement for the former BackgroundJob-System. Since it became clear that the
 * database access takes less than 10% of the total time needed for reload of a CommonViewer, the
 * BackgroundJobs were not adequate for this task. Furthermore, there were several issues with those
 * widely used jobs.
 * 
 * PersistentObjectLoader is a much simpler replacement and does not load in background. Instead it
 * uses a @see DelayableJob to perform loading.
 * 
 * @author Gerry
 * 
 */
public abstract class PersistentObjectLoader implements ICommonViewerContentProvider, IWorker {
	public final static String PARAM_FIELDNAMES = "fieldnames"; //$NON-NLS-1$
	public final static String PARAM_VALUES = "fieldvalues"; //$NON-NLS-1$
	protected CommonViewer cv;
	protected Query<? extends PersistentObject> qbe;
	private final LinkedList<QueryFilter> queryFilters = new LinkedList<QueryFilter>();
	// protected IFilter viewerFilter;
	protected DelayableJob dj;
	protected String[] orderFields;
	private boolean bSuspended;
	
	public PersistentObjectLoader(CommonViewer cv, Query<? extends PersistentObject> qbe){
		this.cv = cv;
		this.qbe = qbe;
		dj = new DelayableJob(Messages.PersistentObjectLoader_LoadingData, this); //$NON-NLS-1$
	}
	
	public Query<? extends PersistentObject> getQuery(){
		return qbe;
	}
	
	/**
	 * start listening the selector fields of the ControlField of the loader's CommonViewer. If the
	 * user enters text or clicks the headings, a changed() or reorder() event will be fired
	 */
	public void startListening(){
		// viewerFilter =
		// cv.getConfigurer().getControlFieldProvider().createFilter();
		cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
	}
	
	/**
	 * stop listening the selector fields
	 */
	public void stopListening(){
		cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}
	
	public Object[] getElements(Object inputElement){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void dispose(){
		stopListening();
		if (dj != null) {
			dj.cancel();
		}
	}
	
	/**
	 * This will be called by the CommonViewer on construction
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		dj.launch(0);
	}
	
	/**
	 * One or more of the ControlField's selectors habe been changed. We'll wait a moment for more
	 * changes before we launch the loader. Use this method also to force a restart of the loader
	 * programatically (values can be null)
	 * 
	 * @param values
	 *            the new values
	 */
	public void changed(HashMap<String, String> values){
		ControlFieldProvider cfp = cv.getConfigurer().getControlFieldProvider();
		if (cfp != null) {
			if (cfp.isEmpty()) {
				cv.notify(CommonViewer.Message.empty);
			} else {
				cv.notify(CommonViewer.Message.notempty);
			}
		}
		dj.setRuntimeData(PARAM_VALUES, values);
		dj.launch(DelayableJob.DELAY_ADAPTIVE);
	}
	
	/**
	 * The user request reordering of the table
	 * 
	 * @param field
	 *            the field name after which the table should e reordered
	 */
	public void reorder(String field){
		setOrderFields(new String[] {
			field
		});
		dj.launch(20);
	}
	
	public void selected(){
		
	}
	
	public void addQueryFilter(QueryFilter fp){
		synchronized (queryFilters) {
			queryFilters.add(fp);
		}
	}
	
	public void removeQueryFilter(QueryFilter fp){
		synchronized (queryFilters) {
			queryFilters.remove(fp);
		}
	}
	
	public void applyQueryFilters(){
		synchronized (queryFilters) {
			for (QueryFilter fp : queryFilters) {
				fp.apply(qbe);
			}
		}
	}
	
	public void setOrderFields(String... name){
		orderFields = name;
	}
	
	/**
	 * a QueryFilter can modify the Query of this Loader. It will be called before each reload.
	 * 
	 * @author Gerry
	 * 
	 */
	public interface QueryFilter {
		public void apply(Query<? extends PersistentObject> qbe);
	}
	
	public void setSuspended(boolean bSuspend){
		bSuspended = bSuspend;
	}
	
	public boolean isSuspended(){
		return bSuspended;
	}
	
	public void init(){}
}
