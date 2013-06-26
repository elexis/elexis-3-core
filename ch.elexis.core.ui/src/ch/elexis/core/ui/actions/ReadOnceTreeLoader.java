/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.selectors.SelectorPanel;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.SelectorPanelProvider;

/**
 * A TreeLoader designed to read only once (for immutable data)
 * 
 * @author gerry
 * 
 */
public class ReadOnceTreeLoader extends PersistentObjectLoader implements ITreeContentProvider {
	
	protected String parentColumn;
	protected String orderBy;
	private HashMap<PersistentObject, HashMap<PersistentObject, ?>> allNodes =
		new HashMap<PersistentObject, HashMap<PersistentObject, ?>>();
	private PersistentObject[] root;
	
	TreeViewer tv;
	int size = 0;
	SelectorPanelProvider slp;
	ViewerFilter filter;
	Object[] expanded = null;
	
	public ReadOnceTreeLoader(CommonViewer cv, Query<? extends PersistentObject> qbe,
		String parentField, String orderBy){
		super(cv, qbe);
		parentColumn = parentField;
		this.orderBy = orderBy;
		setQuery("NIL");
		root = qbe.execute().toArray(new PersistentObject[0]);
	}
	
	@Override
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params){
		UiDesk.asyncExec(new Runnable() {
			
			@Override
			public void run(){
				ProgressMonitorDialog dialog =
					new ProgressMonitorDialog(cv.getViewerWidget().getControl().getShell());
				try {
					dialog.run(false, false, new IRunnableWithProgress() {
						
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
							InterruptedException{
							monitor.beginTask("Durchsuche Tarmed....", -1);
							tv.refresh(false);
							if (slp.isEmpty()) {
								if (expanded != null) {
									tv.setExpandedElements(expanded);
									expanded = null;
								}
							} else {
								if (expanded == null) {
									expanded = tv.getExpandedElements();
								}
								tv.expandAll();
							}
							monitor.done();
						}
					});
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		return Status.OK_STATUS;
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		return root;
	}
	
	@Override
	public Object getParent(Object element){
		return null;
	}
	
	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	@Override
	public Object[] getChildren(Object parent){
		PersistentObject par = (PersistentObject) parent;
		
		HashMap children = allNodes.get(par);
		if (children == null) {
			children = new HashMap<PersistentObject, HashMap>();
			setQuery(par.getId());
			List<PersistentObject> ch = (List<PersistentObject>) qbe.execute();
			for (PersistentObject po : ch) {
				children.put(po, new HashMap<PersistentObject, HashMap>());
			}
			allNodes.put(par, children);
		}
		return children.keySet().toArray();
	}
	
	protected void setQuery(String parent){
		qbe.clear();
		qbe.add(parentColumn, Query.EQUALS, parent);
		applyQueryFilters();
	}
	
	@Override
	public void init(){
		if (slp == null) {
			slp = (SelectorPanelProvider) cv.getConfigurer().getControlFieldProvider();
		}
		if (filter == null) {
			filter = new TreeFilter(slp.getPanel());
		}
		tv = (TreeViewer) cv.getViewerWidget();
		if (orderBy != null) {
			tv.setSorter(new ViewerSorter() {
				
				@Override
				public int compare(Viewer viewer, Object e1, Object e2){
					String c1 = ((PersistentObject) e1).get(orderBy);
					String c2 = ((PersistentObject) e2).get(orderBy);
					return c1.compareTo(c2);
				}
				
			});
		}
		tv.setFilters(new ViewerFilter[] {
			filter
		});
		
	}
	
	@Override
	public boolean hasChildren(Object element){
		HashMap children = allNodes.get(element);
		if (children == null) {
			return getChildren(element).length > 0;
		}
		return children.size() > 0;
	}
	
	class TreeFilter extends ViewerFilter {
		SelectorPanel panel;
		
		TreeFilter(SelectorPanel sp){
			panel = sp;
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			PersistentObject po = (PersistentObject) element;
			HashMap<String, String> vals = panel.getValues();
			if (po.isMatching(vals, PersistentObject.MATCH_AUTO, true)) {
				return true;
			} else {
				for (Object poc : getChildren(po)) {
					if (select(viewer, po, poc)) {
						return true;
					}
				}
				return false;
				
			}
		}
		
	}
	
}
