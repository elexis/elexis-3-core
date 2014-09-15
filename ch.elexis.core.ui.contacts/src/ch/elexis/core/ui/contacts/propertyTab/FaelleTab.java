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
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import ch.elexis.core.model.IContact;
import ch.elexis.core.types.ContactType;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.contacts.controls.FaelleComposite;

public class FaelleTab extends AbstractPropertySection implements IFilter {

	private FaelleComposite fc;
	private SubActionBars subActionBars;
	
	public FaelleTab() {}
	
	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		fc = new FaelleComposite(parent, SWT.None, aTabbedPropertySheetPage);
		aTabbedPropertySheetPage.getWidgetFactory().adapt(fc);
		aTabbedPropertySheetPage.getWidgetFactory().paintBordersFor(fc);
		subActionBars = new SubActionBars(aTabbedPropertySheetPage.getSite()
				.getActionBars());
		
		// COMPAT
		subActionBars.getToolBarManager().add(GlobalActions.neuerFallAction);
	}
	
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		fc.setContact((IContact) input);
	}

	@Override
	public void aboutToBeShown() {
		super.aboutToBeShown();
		subActionBars.activate();
		subActionBars.updateActionBars();
	}

	@Override
	public void aboutToBeHidden() {
		super.aboutToBeHidden();
		subActionBars.deactivate();
		subActionBars.updateActionBars();
	}

	@Override
	public boolean select(Object toTest) {
		IContact c = (IContact) toTest;
		return (c.getContactType() == ContactType.PERSON);
	}

}
