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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.ui.contacts.controls.StammDatenComposite;

public class StammdatenTab extends AbstractPropertySection {
	
	/**
	 * we need to use {@link ContactBean} as it fullfills both {@link IContact} and {@link IPerson}
	 */
	private ContactBean contact;
	private StammDatenComposite sdc;
	
	public StammdatenTab(){}
	
	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage){
		super.createControls(parent, aTabbedPropertySheetPage);
		sdc = new StammDatenComposite(parent, SWT.None, aTabbedPropertySheetPage);
		aTabbedPropertySheetPage.getWidgetFactory().adapt(sdc);
		aTabbedPropertySheetPage.getWidgetFactory().paintBordersFor(sdc);
	}
	
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection){
		super.setInput(part, selection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		contact = (ContactBean) input;
		sdc.setContact(contact);
	}
	
}
