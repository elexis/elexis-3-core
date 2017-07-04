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

import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.KonsDetailView;
import ch.elexis.core.ui.views.PatHeuteView;
import ch.elexis.core.ui.views.rechnung.KonsZumVerrechnenView;
import ch.elexis.core.ui.views.rechnung.RechnungsListeView;
import ch.elexis.core.ui.views.rechnung.RnDetailView;

/**
 * Perspektive für Abrechnungen.
 * 
 * @author gerry
 * 
 */
public class AbrechnungsPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.AbrechnungPerspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		IFolderLayout fld = layout.createFolder("AbrechnungsFolder", IPageLayout.LEFT, 0.6f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout frd = layout.createFolder("Detailfolder", IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA);
		fld.addView(PatHeuteView.ID);
		fld.addView(KonsZumVerrechnenView.ID);
		fld.addView(RechnungsListeView.ID);
		frd.addView(RnDetailView.ID);
		frd.addView(KonsDetailView.ID);
		frd.addPlaceholder(FallDetailView.ID);
		frd.addPlaceholder(UiResourceConstants.PatientDetailView2_ID);
		layout.addShowViewShortcut(PatHeuteView.ID);
		layout.addShowViewShortcut(KonsZumVerrechnenView.ID);
		layout.addShowViewShortcut(RnDetailView.ID);
		layout.addShowViewShortcut(KonsDetailView.ID);
		layout.addShowViewShortcut(RechnungsListeView.ID);
	}
	
}
