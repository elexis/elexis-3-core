/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.controls;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IContact;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.views.provider.FaelleContentProvider;
import ch.elexis.core.ui.views.provider.FaelleLabelProvider;
import ch.elexis.data.Fall;

public class FaelleComposite extends Composite {
	
	private TableViewer tableViewerFaelle;
	
	public FaelleComposite(Composite parent, int style, TabbedPropertySheetPage tpsp){
		this(parent, style);
	}
	
	public FaelleComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		tableViewerFaelle = new TableViewer(this, SWT.None);
		tableViewerFaelle.getTable().setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		tableViewerFaelle.getTable().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tableViewerFaelle.setLabelProvider(new FaelleLabelProvider());
		tableViewerFaelle.setContentProvider(new FaelleContentProvider());
		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		tableViewerFaelle.getTable().setMenu(
			menuManager.createContextMenu(tableViewerFaelle.getTable()));
		menuManager.add(GlobalActions.openFallaction);
		menuManager.add(GlobalActions.makeBillAction);
		menuManager.add(GlobalActions.delFallAction);
		menuManager.add(GlobalActions.reopenFallAction);
		
		tableViewerFaelle.addSelectionChangedListener(new FallSelectionChangedToEventDispatcher());
	}
	
	public void setContact(IContact k){
		tableViewerFaelle.setInput(k);
	}
	
	/**
	 * Forwards selections in the contact viewer table to the ElexisEventDispatcher
	 */
	private class FallSelectionChangedToEventDispatcher implements ISelectionChangedListener {
		
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			ISelection selection = event.getSelection();
			if (selection == null)
				return;
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object selectedObject = strucSelection.getFirstElement();
			if (selectedObject == null)
				return;
			if (selectedObject instanceof Fall) {
				Fall f = (Fall) selectedObject;
				ElexisEventDispatcher.fireSelectionEvent(f);
			}
		}
	}
}
