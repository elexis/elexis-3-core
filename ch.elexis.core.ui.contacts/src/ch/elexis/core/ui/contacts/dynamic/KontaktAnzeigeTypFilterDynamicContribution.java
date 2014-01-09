/*******************************************************************************
 * Copyright (c) 2014 MEDEVIT <office@medevit.at>.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.core.ui.contacts.views.filter.KontaktAnzeigeTypViewerFilter;

public class KontaktAnzeigeTypFilterDynamicContribution extends ContributionItem {
	
	private MenuItem itemShowOrganization;
	private MenuItem itemShowPerson;
	private MenuItem itemShowAnwender;
	private MenuItem itemShowMandant;
	private MenuItem itemShowPatient;
	private MenuItem itemShowDeleted;
	
	public KontaktAnzeigeTypFilterDynamicContribution(){}
	
	public KontaktAnzeigeTypFilterDynamicContribution(String id){
		super(id);
	}
	
	@Override
	public void fill(Menu menu, int index){
		itemShowDeleted = new MenuItem(menu, SWT.CHECK, index);
		itemShowDeleted.setText("Gelöscht");
		itemShowDeleted.setSelection(KontaktAnzeigeTypViewerFilter.isShowDeleted());
		itemShowDeleted.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktAnzeigeTypViewerFilter.setShowDeleted(!KontaktAnzeigeTypViewerFilter
					.isShowDeleted());
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR, index);
		
		itemShowOrganization = new MenuItem(menu, SWT.CHECK, index);
		itemShowOrganization.setText("Organisation");
		itemShowOrganization.setSelection(KontaktAnzeigeTypViewerFilter.isShowOrganisation());
		itemShowOrganization.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktAnzeigeTypViewerFilter.setShowOrganisation(!KontaktAnzeigeTypViewerFilter
					.isShowOrganisation());
			}
		});
		
		itemShowPerson = new MenuItem(menu, SWT.CHECK, index);
		itemShowPerson.setText("Person");
		itemShowPerson.setSelection(KontaktAnzeigeTypViewerFilter.isShowPerson());
		itemShowPerson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktAnzeigeTypViewerFilter.setShowPerson(!KontaktAnzeigeTypViewerFilter
					.isShowPerson());
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR, index);
		
		itemShowAnwender = new MenuItem(menu, SWT.CHECK, index);
		itemShowAnwender.setText("Anwender");
		itemShowAnwender.setSelection(KontaktAnzeigeTypViewerFilter.isShowAnwender());
		itemShowAnwender.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktAnzeigeTypViewerFilter.setShowAnwender(!KontaktAnzeigeTypViewerFilter
					.isShowAnwender());
			}
		});
		
		itemShowMandant = new MenuItem(menu, SWT.CHECK, index);
		itemShowMandant.setText("Mandant");
		itemShowMandant.setSelection(KontaktAnzeigeTypViewerFilter.isShowMandant());
		itemShowMandant.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktAnzeigeTypViewerFilter.setShowMandant(!KontaktAnzeigeTypViewerFilter
					.isShowMandant());
			}
		});
		
		itemShowPatient = new MenuItem(menu, SWT.CHECK, index);
		itemShowPatient.setText("Patient");
		itemShowPatient.setSelection(KontaktAnzeigeTypViewerFilter.isShowPatient());
		itemShowPatient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktAnzeigeTypViewerFilter.setShowPatient(!KontaktAnzeigeTypViewerFilter
					.isShowPatient());
			}
		});
	}
	
	@Override
	public boolean isDynamic(){
		return true;
	}
}
