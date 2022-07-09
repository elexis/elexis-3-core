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
import ch.elexis.core.ui.views.BriefAuswahl;
import ch.elexis.core.ui.views.KonsDetailView;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;

public class BriefePerspektive implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.BriefePerspektive"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);

		IFolderLayout left = layout.createFolder("Links.folder", IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		IFolderLayout main = layout.createFolder("Haupt.Folder", IPageLayout.LEFT, 0.7f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		left.addView(BriefAuswahl.ID);
		main.addView(TextView.ID);
		main.addView(TextTemplateView.ID);

		layout.addPerspectiveShortcut(ID);
		layout.addShowViewShortcut(UiResourceConstants.PatientDetailView2_ID);
		layout.addShowViewShortcut(KonsDetailView.ID);
		layout.addShowViewShortcut(BriefAuswahl.ID);
		layout.addShowViewShortcut(TextView.ID);

	}

}
