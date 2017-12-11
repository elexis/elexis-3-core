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
package ch.elexis.core.ui.contacts.dynamic;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.model.IContact;
import ch.elexis.core.ui.contacts.views.filter.KontaktAnzeigeTypViewerFilter;

public class KontaktRollenSwitcher extends ContributionItem {
	
	private IContact k;
	
	public KontaktRollenSwitcher(){}
	
	public KontaktRollenSwitcher(String id){
		super(id);
	}
	
	@Override
	public void fill(Menu menu, int index){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getActivePage().getSelection();
		IStructuredSelection strucSelection = (IStructuredSelection) selection;
		k = (IContact) strucSelection.getFirstElement();
		
		if (k != null) {
			MenuItem itemMandant;
			MenuItem itemAnwender;
			MenuItem itemPatient;
			switch (k.getContactType()) {
			case PERSON:
				// TODO: Passwort setzen beim Ändern
				itemAnwender = new MenuItem(menu, SWT.CHECK, index);
				itemAnwender.setText("Anwender");
				if (k.isUser())
					itemAnwender.setSelection(true);
				itemAnwender.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						if (!k.isUser()) {
							// TODO Create a new Anwender, set username, pw
							// call preference page to set access rights
							
						} else {
							// Delete the Anwender, delete username and pw
						}
						k.setUser(!k.isUser());
						KontaktAnzeigeTypViewerFilter.refreshViewer();
					}
				});
				
				itemMandant = new MenuItem(menu, SWT.CHECK, index);
				itemMandant.setText("Mandant");
				if (k.isMandator())
					itemMandant.setSelection(true);
				itemMandant.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						if (k.isMandator()) {
							// TODO: User about to remove mandant role, warn him
						}
						
						k.setMandator(!k.isMandator());
						KontaktAnzeigeTypViewerFilter.refreshViewer();
					}
				});
				
				itemPatient = new MenuItem(menu, SWT.CHECK, index);
				itemPatient.setText("Patient");
				if (k.isPatient())
					itemPatient.setSelection(true);
				itemPatient.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						if (k.isPatient()) {
							// TODO User is about to delete patient role, warn him!
						}
						
						k.setPatient(!k.isPatient());
						KontaktAnzeigeTypViewerFilter.refreshViewer();
					}
				});
				break;
			case ORGANIZATION:
				itemMandant = new MenuItem(menu, SWT.CHECK, index);
				itemMandant.setText("Mandant");
				if (k.isMandator())
					itemMandant.setSelection(true);
				itemMandant.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						k.setMandator(!k.isMandator());
						KontaktAnzeigeTypViewerFilter.refreshViewer();
					}
				});
				break;
			default:
				MenuItem item = new MenuItem(menu, SWT.CHECK, index);
				item.setText("Keine Optionen");
				item.setEnabled(false);
				break;
			}
			
		}
		
	}
	
	@Override
	public boolean isDynamic(){
		return true;
	}
}
