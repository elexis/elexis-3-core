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
package ch.elexis.core.ui.contacts.propertyTab;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.model.IContact;
import ch.elexis.core.ui.contacts.controls.UserManagementComposite;

/**
 * This property tab provides the management of User (Anwender) and Mandant respective information.
 */
public class UserManagementTab extends AbstractPropertySection implements IFilter {
	
	private UserManagementComposite umc;
	
	public UserManagementTab(){}
	
	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage){
		super.createControls(parent, aTabbedPropertySheetPage);
		umc = new UserManagementComposite(parent, SWT.None, aTabbedPropertySheetPage);
		aTabbedPropertySheetPage.getWidgetFactory().adapt(umc);
		aTabbedPropertySheetPage.getWidgetFactory().paintBordersFor(umc);
	}
	
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection){
		super.setInput(part, selection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		umc.setContact((ContactBean) input);
	}
	
	@Override
	public boolean select(Object toTest){
		IContact c = (IContact) toTest;
		return (c.isMandator() || c.isUser());
	}
	
}
