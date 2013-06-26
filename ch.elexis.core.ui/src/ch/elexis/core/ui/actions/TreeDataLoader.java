/*******************************************************************************
 * Copyright (c) 2009-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.rgw.tools.LazyTree;
import ch.rgw.tools.LazyTree.LazyTreeListener;
import ch.rgw.tools.Tree;

/**
 * A PersistentObjectLoader for Tree-like structures. This reads its contents from a table that has
 * a "parent"-field to denote ancestry
 * 
 * @author gerry
 * 
 */
public class TreeDataLoader extends PersistentObjectLoader implements ILazyTreeContentProvider {
	protected String parentColumn;
	protected String orderBy;
	protected LazyTree<PersistentObject> root;
	
	/**
	 * Create a TreeDataLoader from a @see CommonViewer
	 * 
	 * @param cv
	 *            he CommonViewer
	 * @param qbe
	 *            the Query to load the data
	 * @param parentField
	 *            the name of the field that contains ancestry information
	 */
	public TreeDataLoader(CommonViewer cv, Query<? extends PersistentObject> query,
		String parentField, String orderBy){
		super(cv, query);
		parentColumn = parentField;
		this.orderBy = orderBy;
		
		root =
			(LazyTree<PersistentObject>) new LazyTree<PersistentObject>(null, null,
				new LazyTreeListener() {
					@Override
					public boolean fetchChildren(LazyTree<?> l){
						List<PersistentObject> children = null;
						synchronized (qbe) {
							PersistentObject p = (PersistentObject) l.contents;
							if (l.getParent() == null) {
								setQuery("NIL");
							} else {
								if (p == null) {
									return false;
								}
								setQuery(p.getId());
							}
							children = (List<PersistentObject>) qbe.execute();
							for (PersistentObject po : children) {
								new LazyTree(l, po, this);
							}
						}
						return children.size() > 0;
					}
					
					@Override
					public boolean hasChildren(LazyTree<?> l){
						return fetchChildren(l);
					}
					
				});
	}
	
	public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params){
		monitor.beginTask(Messages.getString("TreeDataLoader.0"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		synchronized (qbe) {
			root.clear();
			setQuery("NIL");
			
			for (PersistentObject po : qbe.execute()) {
				new Tree<PersistentObject>(root, po);
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				monitor.worked(1);
			}
			monitor.done();
			
			UiDesk.asyncExec(new Runnable() {
				public void run(){
					((TreeViewer) cv.getViewerWidget()).setChildCount(cv.getViewerWidget()
						.getInput(), root.getChildren().size());
				}
			});
		}
		return Status.OK_STATUS;
	}
	
	public Object getParent(Object element){
		if (element instanceof Tree) {
			return ((Tree) element).getParent();
		}
		return null;
	}
	
	public void updateChildCount(Object element, int currentChildCount){
		int num = 0;
		if (element instanceof Tree) {
			Tree<PersistentObject> t = (Tree<PersistentObject>) element;
			if (!t.hasChildren()) {
				setQuery(t.contents.getId());
				for (PersistentObject po : qbe.execute()) {
					new Tree<PersistentObject>(t, po);
				}
			}
			num = t.getChildren().size();
		} else {
			num = root.getChildren().size();
		}
		((TreeViewer) cv.getViewerWidget()).setChildCount(element, num);
	}
	
	public void updateElement(Object parent, int index){
		Tree<PersistentObject> t;
		if (parent instanceof Tree) {
			t = (Tree<PersistentObject>) parent;
		} else {
			t = root;
		}
		Tree elem = t.getChildren().toArray(new Tree[0])[index];
		((TreeViewer) cv.getViewerWidget()).replace(parent, index, elem);
		updateChildCount(elem, 0);
	}
	
	protected void setQuery(String parent){
		qbe.clear();
		ControlFieldProvider cfp = cv.getConfigurer().getControlFieldProvider();
		if (cfp != null) {
			if (cfp.isEmpty()) {
				qbe.add(parentColumn, Query.EQUALS, parent);
			} else {
				cfp.setQuery(qbe);
			}
		}
		applyQueryFilters();
		if (orderBy != null) {
			qbe.orderBy(true, orderBy);
		}
	}
}
