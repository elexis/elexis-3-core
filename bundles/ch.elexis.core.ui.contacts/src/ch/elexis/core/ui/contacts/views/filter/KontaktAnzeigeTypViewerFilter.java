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

	public KontaktAnzeigeTypViewerFilter(Viewer viewer) {
		KontaktAnzeigeTypViewerFilter.viewer = viewer;

		PlatformUI.getPreferenceStore().setDefault(KontaktAnzeigeTypViewerFilter.class.getName() + ".showPatient", //$NON-NLS-1$
				true);

		showPerson = PlatformUI.getPreferenceStore()
				.getBoolean(KontaktAnzeigeTypViewerFilter.class.getName() + ".showPerson"); //$NON-NLS-1$
		showPatient = PlatformUI.getPreferenceStore()
				.getBoolean(KontaktAnzeigeTypViewerFilter.class.getName() + ".showPatient"); //$NON-NLS-1$
		showOrganisation = PlatformUI.getPreferenceStore()
				.getBoolean(KontaktAnzeigeTypViewerFilter.class.getName() + ".showOrganisation"); //$NON-NLS-1$
		showMandant = PlatformUI.getPreferenceStore()
				.getBoolean(KontaktAnzeigeTypViewerFilter.class.getName() + ".showMandant"); //$NON-NLS-1$
		showAnwender = PlatformUI.getPreferenceStore()
				.getBoolean(KontaktAnzeigeTypViewerFilter.class.getName() + ".showAnwender"); //$NON-NLS-1$
		showDeleted = PlatformUI.getPreferenceStore()
				.getBoolean(KontaktAnzeigeTypViewerFilter.class.getName() + ".showDeleted"); //$NON-NLS-1$
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IContact contact = (IContact) element;
		if (showDeleted && contact.isDeleted())
			return true;

		if (contact.isOrganization()) {
			if (showOrganisation) {
				return true;
			}
		}

		if (contact.isPerson()) {
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
		}

		// TODO: Administrator is neither Organization nor Person?!

		return false;
	}

	public static void refreshViewer() {
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		if (!showAnwender && !showDeleted && !showMandant && !showOrganisation && !showPatient && !showPerson) {
			//
		}
		viewer.getControl().setRedraw(true);
	}

	public static boolean isShowPerson() {
		return showPerson;
	}

	public static void setShowPerson(boolean showPerson) {
		KontaktAnzeigeTypViewerFilter.showPerson = showPerson;
		PlatformUI.getPreferenceStore().setValue(KontaktAnzeigeTypViewerFilter.class.getName() + ".showPerson", //$NON-NLS-1$
				showPerson);
		refreshViewer();
	}

	public static boolean isShowOrganisation() {
		return showOrganisation;
	}

	public static void setShowOrganisation(boolean showOrganisation) {
		KontaktAnzeigeTypViewerFilter.showOrganisation = showOrganisation;
		PlatformUI.getPreferenceStore().setValue(KontaktAnzeigeTypViewerFilter.class.getName() + ".showOrganisation", //$NON-NLS-1$
				showOrganisation);
		refreshViewer();
	}

	public static boolean isShowPatient() {
		return showPatient;
	}

	public static void setShowPatient(boolean showPatient) {
		KontaktAnzeigeTypViewerFilter.showPatient = showPatient;
		PlatformUI.getPreferenceStore().setValue(KontaktAnzeigeTypViewerFilter.class.getName() + ".showPatient", //$NON-NLS-1$
				showPatient);
		refreshViewer();
	}

	public static boolean isShowMandant() {
		return showMandant;
	}

	public static void setShowMandant(boolean showMandant) {
		KontaktAnzeigeTypViewerFilter.showMandant = showMandant;
		PlatformUI.getPreferenceStore().setValue(KontaktAnzeigeTypViewerFilter.class.getName() + ".showMandant", //$NON-NLS-1$
				showMandant);
		refreshViewer();
	}

	public static boolean isShowAnwender() {
		return showAnwender;
	}

	public static void setShowAnwender(boolean showAnwender) {
		KontaktAnzeigeTypViewerFilter.showAnwender = showAnwender;
		PlatformUI.getPreferenceStore().setValue(KontaktAnzeigeTypViewerFilter.class.getName() + ".showAnwender", //$NON-NLS-1$
				showAnwender);
		refreshViewer();
	}

	public static boolean isShowDeleted() {
		return showDeleted;
	}

	public static void setShowDeleted(boolean showDeleted) {
		KontaktAnzeigeTypViewerFilter.showDeleted = showDeleted;
		PlatformUI.getPreferenceStore().setValue(KontaktAnzeigeTypViewerFilter.class.getName() + ".showDeleted", //$NON-NLS-1$
				showDeleted);
		refreshViewer();
	}

}
