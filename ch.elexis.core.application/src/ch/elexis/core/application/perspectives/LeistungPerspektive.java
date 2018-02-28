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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.views.codesystems.CodeDetailView;
import ch.elexis.core.ui.views.codesystems.LeistungenView;

/**
 * Anzeige der Detailviews aller Leistungstypen.
 * 
 * @author gerry
 * 
 */
public class LeistungPerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.LeistungPerspektive"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		layout.addPlaceholder(LeistungenView.ID, IPageLayout.LEFT, 0.3f,
			IPageLayout.ID_EDITOR_AREA);
		layout.addView(CodeDetailView.ID, IPageLayout.LEFT, 0.8f, IPageLayout.ID_EDITOR_AREA);
		layout.addShowViewShortcut(CodeDetailView.ID);
		
	}
	
}
