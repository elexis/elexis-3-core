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

import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.views.BestellView;
import ch.elexis.core.ui.views.artikel.ArtikelSelektor;

public class BestellPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.bestellperspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		layout.addView(ArtikelSelektor.ID, SWT.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout ifl =
			layout.createFolder("iflRight", SWT.RIGHT, 0.6f, IPageLayout.ID_EDITOR_AREA);
		ifl.addView(BestellView.ID);
	}
	
}
