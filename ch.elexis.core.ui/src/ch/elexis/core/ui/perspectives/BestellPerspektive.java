/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.perspectives;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.views.BestellView;
import ch.elexis.core.ui.views.artikel.ArtikelSelektor;

public class BestellPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.bestellperspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		layout.addView(ArtikelSelektor.ID, SWT.LEFT, 0.4f, editorArea);
		IFolderLayout ifl =
			layout.createFolder("iflRight", SWT.RIGHT, 0.6f, layout.getEditorArea()); //$NON-NLS-1$
		ifl.addView(BestellView.ID);
	}
	
}
