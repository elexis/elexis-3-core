/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.preferences.inputs;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.ACE;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Anwender;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.Tree;

/**
 * @deprecated
 */
public class ACLPreferenceTree extends Composite {
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	Tree<ACE> acls;
	TreeViewer tv;
	org.eclipse.swt.widgets.List lbGroups;
	org.eclipse.swt.widgets.List lbUsers;
	List<Anwender> lUsers;
	
	private Tree<ACE> findParent(ACE t){
		ACE parent = t.getParent();
		if (parent.equals(ACE.ACE_ROOT)) {
			return acls;
		}
		Tree<ACE> parentTree = acls.find(parent, true);
		if (parentTree != null) {
			return parentTree;
		}
		Tree<ACE> grandParentTree = findParent(parent);
		if (grandParentTree == null) {
			System.out.println("Fehler");
			return new Tree<ACE>(acls, parent);
		}
		return new Tree<ACE>(grandParentTree, parent);
		
	}
	
	public ACLPreferenceTree(Composite parent, ACE... acl){
		super(parent, SWT.NONE);
		acls = new Tree<ACE>(null, null);
		for (ACE s : acl) {
			Tree<ACE> mine = acls.find(s, true);
			if (mine == null) {
				Tree<ACE> parentTree = findParent(s);
				if (parentTree != null) {
					new Tree<ACE>(parentTree, s);
				} else {
					log.error("Could not find parent ACE " + s.getName());
				}
			}
		}
		
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv = new TreeViewer(this);
		tv.setContentProvider(new ITreeContentProvider() {
			
			public Object[] getChildren(Object parentElement){
				Tree tree = (Tree) parentElement;
				return tree.getChildren().toArray();
			}
			
			public Object getParent(Object element){
				return ((Tree) element).getParent();
			}
			
			public boolean hasChildren(Object element){
				Tree tree = (Tree) element;
				return tree.hasChildren();
			}
			
			public Object[] getElements(Object inputElement){
				return acls.getChildren().toArray();
			}
			
			public void dispose(){
				// TODO Auto-generated method stub
				
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// TODO Auto-generated method stub
				
			}
		});
		tv.setLabelProvider(new LabelProvider() {
			
			@Override
			public String getText(Object element){
				return (String) ((Tree<ACE>) element).contents.getLocalizedName();
			}
			
		});
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite cBottom = new Composite(this, SWT.NONE);
		cBottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cBottom.setLayout(new GridLayout(2, true));
		new Label(cBottom, SWT.NONE).setText(StringConstants.ROLES_DEFAULT);
		new Label(cBottom, SWT.NONE).setText(StringConstants.ROLE_USERS);
		lbGroups = new org.eclipse.swt.widgets.List(cBottom, SWT.MULTI | SWT.V_SCROLL);
		lbUsers = new org.eclipse.swt.widgets.List(cBottom, SWT.MULTI | SWT.V_SCROLL);
		lbUsers.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lbGroups.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lUsers = Hub.getUserList();
		for (Anwender an : lUsers) {
			lbUsers.add(an.getLabel());
		}
//		List<String> lGroups = CoreHub.acl.getGroups();
//		for (String s : lGroups) {
//			lbGroups.add(s);
//		}
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			/**
			 * if the user selects an ACL from the TreeViewer, we want to select users and groups
			 * that are granted this acl in the lbGroups and lbUsers ListBoxes
			 */
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				lbGroups.deselectAll();
				lbUsers.deselectAll();
//				if (!sel.isEmpty()) {
//					Tree<ACE> acl = (Tree<ACE>) sel.getFirstElement();
//					ACE right = acl.contents;
//					List<String> grps = CoreHub.acl.groupsForGrant(right);
//					List<Anwender> users = CoreHub.acl.usersForGrant(right);
//					for (String g : grps) {
//						int idx = StringTool.getIndex(lbGroups.getItems(), g);
//						if (idx != -1) {
//							lbGroups.select(idx);
//						}
//					}
//					for (Anwender an : users) {
//						int idx = StringTool.getIndex(lbUsers.getItems(), an.getLabel());
//						if (idx != -1) {
//							lbUsers.select(idx);
//						}
//					}
//				}
				
			}
			
		});
		lbGroups.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent arg0){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if (!sel.isEmpty()) {
					Tree<ACE> acl = (Tree<ACE>) sel.getFirstElement();
					ACE right = acl.contents;
					String[] gsel = lbGroups.getSelection();
					for (String g : lbGroups.getItems()) {
//						CoreHub.acl.revoke(g, right);
					}
					for (String g : gsel) {
						CoreHub.acl.grant(g, right);
					}
				}
			}
			
		});
		lbUsers.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent arg0){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if (!sel.isEmpty()) {
					Tree<ACE> acl = (Tree<ACE>) sel.getFirstElement();
					ACE right = acl.contents;
					int[] uSel = lbUsers.getSelectionIndices();
					for (Anwender an : lUsers) {
//						CoreHub.acl.revoke(an, right);
					}
					for (int i : uSel) {
//						CoreHub.acl.grant(lUsers.get(i), right);
					}
				}
			}
		});
		tv.setSorter(new ViewerSorter() {
			
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				Tree<ACE> t1 = (Tree<ACE>) e1;
				Tree<ACE> t2 = (Tree<ACE>) e2;
				return t1.contents.getLocalizedName().compareToIgnoreCase(
					t2.contents.getLocalizedName());
			}
			
		});
		tv.setInput(this);
		
	}
	
	public void reload(){
		
	}
	
	public void flush(){
		CoreHub.acl.flush();
	}
}
