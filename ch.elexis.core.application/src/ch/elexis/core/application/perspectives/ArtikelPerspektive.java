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
package ch.elexis.core.application.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.views.KompendiumView;
import ch.elexis.core.ui.views.artikel.ArtikelView;

/**
 * Diese Klasse erzeugt das Anfangslayout für die Funktion "Artikel"
 */
public class ArtikelPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.ArtikelPerspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		IFolderLayout ifr = layout.createFolder("folder", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		ifr.addView(ArtikelView.ID);
		ifr.addView(KompendiumView.ID);
	}
	
}
