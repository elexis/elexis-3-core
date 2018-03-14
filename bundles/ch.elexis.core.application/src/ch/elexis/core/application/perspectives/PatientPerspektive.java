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

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.compatibility.ElexisFastViewUtil;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.e4.parts.CoverageSelectorPart;
import ch.elexis.core.ui.e4.parts.EncounterHistoryPart;
import ch.elexis.core.ui.views.AUF2;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.KompendiumView;
import ch.elexis.core.ui.views.KonsDetailView;
import ch.elexis.core.ui.views.PatHeuteView;
import ch.elexis.core.ui.views.RezepteView;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.core.ui.views.codesystems.DiagnosenView;
import ch.elexis.core.ui.views.codesystems.LeistungenView;

/**
 * Aufbau des initalen Layouts der "Patient"-Seite
 */
public class PatientPerspektive implements IPerspectiveFactory {
	
	public void createInitialLayout(final IPageLayout layout){
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		IFolderLayout left =
			layout.createFolder("Links.folder", IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout main =
			layout.createFolder("Haupt.Folder", IPageLayout.RIGHT, 0.3f, "Links.folder"); //$NON-NLS-1$
		IFolderLayout leftbottom =
			layout.createFolder("links.unten", IPageLayout.BOTTOM, 0.7f, "Links.folder");
		IFolderLayout right =
			layout.createFolder("Rechts.folder", IPageLayout.RIGHT, 0.7f, "Haupt.Folder");
		
		main.addView(UiResourceConstants.PatientDetailView2_ID);
		left.addView(UiResourceConstants.PatientenListeView_ID);
		leftbottom.addView(CoverageSelectorPart.ID);
		left.addView(PatHeuteView.ID);
		main.addView(KonsDetailView.ID);
		
		main.addView(UiResourceConstants.LaborView_ID);
		main.addView(RezepteView.ID);
		main.addView(AUF2.ID);
		
		right.addView(EncounterHistoryPart.ID);
		
		main.addPlaceholder(FallDetailView.ID);
		main.addPlaceholder(TextView.ID);
		main.addPlaceholder(KompendiumView.ID);

		layout.addPerspectiveShortcut(UiResourceConstants.PatientPerspektive_ID);
		layout.addShowViewShortcut(UiResourceConstants.PatientDetailView2_ID);
		layout.addShowViewShortcut(UiResourceConstants.PatientenListeView_ID);
		// layout.addShowViewShortcut(FallListeView.ID);
		layout.addPerspectiveShortcut(CoverageSelectorPart.ID);
		layout.addShowViewShortcut(EncounterHistoryPart.ID);
		layout.addShowViewShortcut(PatHeuteView.ID);
		layout.addShowViewShortcut(KonsDetailView.ID);
		layout.addShowViewShortcut(RezepteView.ID);
		layout.addShowViewShortcut(FallDetailView.ID);
		
		UiDesk.asyncExec(new Runnable() {
			public void run(){
				
				ElexisFastViewUtil.addToFastView(UiResourceConstants.PatientPerspektive_ID,
					LeistungenView.ID);
				ElexisFastViewUtil.addToFastView(UiResourceConstants.PatientPerspektive_ID,
					DiagnosenView.ID);
				
			}
		});
		
	}
	
}
