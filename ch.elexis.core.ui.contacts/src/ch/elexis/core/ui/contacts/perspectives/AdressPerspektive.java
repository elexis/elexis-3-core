/*******************************************************************************
 * Copyright (c) 2006-2017, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT <office@medevit.at> - e4 port compatibility
 *******************************************************************************/
package ch.elexis.core.ui.contacts.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.contacts.views.KontaktDetailView;
import ch.elexis.core.ui.contacts.views.KontakteView;

/**
 * Diese Perspektive erzeugt das Startlayout für den Auswahlknopf "Adressen" Funktion: Verknüpfung
 * vong Anschriften und Kontakten zu Adressen
 */
public class AdressPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.AdressPerspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		IFolderLayout oben =
			layout.createFolder("oben", IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout details =
			layout.createFolder("details", IPageLayout.TOP, 1.0f, IPageLayout.ID_EDITOR_AREA);
		oben.addView(KontakteView.ID);
		details.addView(KontaktDetailView.ID);
	}
	
}
