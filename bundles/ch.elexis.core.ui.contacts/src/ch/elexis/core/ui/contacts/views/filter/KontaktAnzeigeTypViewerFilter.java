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
package ch.elexis.core.ui.contacts.views.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.model.IContact;

public class KontaktAnzeigeTypViewerFilter extends ViewerFilter {
	
	private static Viewer viewer;
	
	private static boolean showPerson = false;
	private static boolean showOrganisation = false;
	
	private static boolean showPatient = true;
	private static boolean showMandant = false;
	private static boolean showAnwender = false;
	
	private static boolean showDeleted = false;
	
	public KontaktAnzeigeTypViewerFilter(Viewer viewer){
		KontaktAnzeigeTypViewerFilter.viewer = viewer;
		
		PlatformUI.getPreferenceStore().setDefault(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showPatient", true);
		
		showPerson =
			PlatformUI.getPreferenceStore().getBoolean(
				KontaktAnzeigeTypViewerFilter.class.getName() + ".showPerson");
		showPatient =
			PlatformUI.getPreferenceStore().getBoolean(
				KontaktAnzeigeTypViewerFilter.class.getName() + ".showPatient");
		showOrganisation =
			PlatformUI.getPreferenceStore().getBoolean(
				KontaktAnzeigeTypViewerFilter.class.getName() + ".showOrganisation");
		showMandant =
			PlatformUI.getPreferenceStore().getBoolean(
				KontaktAnzeigeTypViewerFilter.class.getName() + ".showMandant");
		showAnwender =
			PlatformUI.getPreferenceStore().getBoolean(
				KontaktAnzeigeTypViewerFilter.class.getName() + ".showAnwender");
		showDeleted =
			PlatformUI.getPreferenceStore().getBoolean(
				KontaktAnzeigeTypViewerFilter.class.getName() + ".showDeleted");
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		IContact contact = (IContact) element;
		if (showDeleted && contact.isDeleted())
			return true;
		
		switch (contact.getContactType()) {
		case ORGANIZATION:
			if (showOrganisation)
				return true;
			break;
		case PERSON:
			if (showPerson) {
				return true;
			} else {
				if (contact.isPatient() && !contact.isDeleted() && showPatient)
					return true;
				if (contact.isUser() && !contact.isDeleted() && showAnwender)
					return true;
				if (contact.isMandator() && !contact.isDeleted() && showMandant)
					return true;
			}
		default:
			// TODO: Administrator is neither Organization nor Person?!
			break;
		}
		
		return false;
	}
	
	public static void refreshViewer(){
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		if (!showAnwender && !showDeleted && !showMandant && !showOrganisation && !showPatient
			&& !showPerson) {
			//
		}
		viewer.getControl().setRedraw(true);
	}
	
	public static boolean isShowPerson(){
		return showPerson;
	}
	
	public static void setShowPerson(boolean showPerson){
		KontaktAnzeigeTypViewerFilter.showPerson = showPerson;
		PlatformUI.getPreferenceStore().setValue(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showPerson", showPerson);
		refreshViewer();
	}
	
	public static boolean isShowOrganisation(){
		return showOrganisation;
	}
	
	public static void setShowOrganisation(boolean showOrganisation){
		KontaktAnzeigeTypViewerFilter.showOrganisation = showOrganisation;
		PlatformUI.getPreferenceStore().setValue(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showOrganisation", showOrganisation);
		refreshViewer();
	}
	
	public static boolean isShowPatient(){
		return showPatient;
	}
	
	public static void setShowPatient(boolean showPatient){
		KontaktAnzeigeTypViewerFilter.showPatient = showPatient;
		PlatformUI.getPreferenceStore().setValue(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showPatient", showPatient);
		refreshViewer();
	}
	
	public static boolean isShowMandant(){
		return showMandant;
	}
	
	public static void setShowMandant(boolean showMandant){
		KontaktAnzeigeTypViewerFilter.showMandant = showMandant;
		PlatformUI.getPreferenceStore().setValue(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showMandant", showMandant);
		refreshViewer();
	}
	
	public static boolean isShowAnwender(){
		return showAnwender;
	}
	
	public static void setShowAnwender(boolean showAnwender){
		KontaktAnzeigeTypViewerFilter.showAnwender = showAnwender;
		PlatformUI.getPreferenceStore().setValue(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showAnwender", showAnwender);
		refreshViewer();
	}
	
	public static boolean isShowDeleted(){
		return showDeleted;
	}
	
	public static void setShowDeleted(boolean showDeleted){
		KontaktAnzeigeTypViewerFilter.showDeleted = showDeleted;
		PlatformUI.getPreferenceStore().setValue(
			KontaktAnzeigeTypViewerFilter.class.getName() + ".showDeleted", showDeleted);
		refreshViewer();
	}
	
}
