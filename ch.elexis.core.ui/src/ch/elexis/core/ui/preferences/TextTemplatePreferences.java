/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;

public class TextTemplatePreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public static final String SUFFIX_FOR_THIS_STATION =
		Messages.TextTemplatePreferences_suffixForStation;
	public static final String BRANCH = "document_templates/"; //$NON-NLS-1$
	public static final String SUFFIX_STATION = BRANCH + "suffix_station"; //$NON-NLS-1$
	
	public TextTemplatePreferences(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
	}
	
	@Override
	protected void createFieldEditors(){
		Label expl = new Label(getFieldEditorParent(), SWT.WRAP);
		expl.setText(Messages.TextTemplatePreferences_ExplanationLine1
			+ Messages.TextTemplatePreferences_ExplanationLine2
			+ Messages.TextTemplatePreferences_ExplanationLine3);
		expl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		addField(new StringFieldEditor(SUFFIX_STATION, SUFFIX_FOR_THIS_STATION,
			getFieldEditorParent()));
		/*
		 * IExtensionRegistry exr = Platform.getExtensionRegistry(); IExtensionPoint exp =
		 * exr.getExtensionPoint("ch.elexis.documentTemplates"); if (exp != null) { IExtension[]
		 * extensions = exp.getExtensions(); for (IExtension ex : extensions) {
		 * IConfigurationElement[] elems = ex.getConfigurationElements(); for (IConfigurationElement
		 * el : elems) { String n=el.getAttribute("name"); addField(new StringFieldEditor(BRANCH+n,
		 * n, getFieldEditorParent())); } }
		 * 
		 * }
		 */
	}
	
	@Override
	protected void performApply(){
		CoreHub.localCfg.flush();
	}
	
	@Override
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
}
