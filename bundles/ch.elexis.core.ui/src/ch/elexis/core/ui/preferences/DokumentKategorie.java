/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;

public class DokumentKategorie extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public DokumentKategorie(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		setDescription(Messages.DokumentKategorie_dokumentKategorien);
		
	}
	
	@Override
	public void createFieldEditors(){
		/*
		 * addField(new Agenda.StringInput( PreferenceConstants.DOC_CATEGORY, "Dokumentkategorien",
		 * getFieldEditorParent() ));
		 */
	}
	
	public void init(IWorkbench workbench){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
}
