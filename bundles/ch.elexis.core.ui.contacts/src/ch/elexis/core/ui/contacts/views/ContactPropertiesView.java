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
package ch.elexis.core.ui.contacts.views;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.PropertySheet;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class ContactPropertiesView extends PropertySheet {
	
	public static final String ID = "ch.elexis.core.ui.contacts.views.ContactPropertiesView";
	
	public ContactPropertiesView(){}
	
	private ISelectionProvider isp = new ProxySelectionProvider();
	
	@Override
	public void createPartControl(Composite parent){
		super.createPartControl(parent);
		getSite().setSelectionProvider(isp);
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection sel){
		super.selectionChanged(part, sel);
		isp.setSelection(sel);
	}
	
	@Override
	protected boolean isImportant(IWorkbenchPart part){
		// Only listen to selections made in the ContactSelectorView
		if (part.getSite().getId().equals(ContactSelectorView.ID)) {
			if (isPinned()) {
				return false;
			} else {
				return true;
			}
		}
		
		return false;
	}
	
	// To declaratively provide commands in the toolbar of this view the
	// command has to be able to access the current selection by means of the
	// HandlerUtil. It is only capable to do so, if the view serves a 
	// SelectionProvider.
	private class ProxySelectionProvider implements ISelectionProvider {
		ISelection current;
		
		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener){}
		
		@Override
		public ISelection getSelection(){
			return current;
		}
		
		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener){}
		
		@Override
		public void setSelection(ISelection selection){
			current = selection;
		}
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
