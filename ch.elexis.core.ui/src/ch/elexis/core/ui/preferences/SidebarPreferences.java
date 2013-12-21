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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class SidebarPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	BooleanFieldEditor sb;
	
	public SidebarPreferences(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription(Messages.SidebarPreferences_DefinitisonStarterBar);
		// noDefaultAndApplyButton();
	}
	
	public SidebarPreferences(String title, int style){
		super(title, style);
	}
	
	@Override
	protected void createFieldEditors(){
		sb =
			new BooleanFieldEditor(Preferences.SHOWSIDEBAR,
				Messages.SidebarPreferences_ShowStartBar, getFieldEditorParent());
		
		addField(sb);
		addField(new BooleanFieldEditor(Preferences.SHOWPERSPECTIVESELECTOR,
			Messages.SidebarPreferences_SchowPerspectives, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.SHOWTOOLBARITEMS,
			Messages.SidebarPreferences_PerspectivesInToolbar, getFieldEditorParent()));
		addField(new Perspektivenliste(Preferences.SIDEBAR,
			Messages.SidebarPreferences_Perspectives, getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	/*
	 * @Override public boolean performOk() { IWorkbenchPage
	 * page=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); try { Starter
	 * starter=(Starter)page.showView(Starter.ID); if(sb.getBooleanValue()==false){
	 * page.hideView(starter); } } catch (PartInitException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } return super.performOk(); }
	 */
}

class Perspektivenliste extends ListEditor {
	public Perspektivenliste(String name, String input, Composite parent){
		super(name, input, parent);
	}
	
	@Override
	protected String createList(String[] items){
		return StringTool.join(items, ","); //$NON-NLS-1$
	}
	
	@Override
	protected String getNewInputObject(){
		PerspektivenAuswahl pa = new PerspektivenAuswahl(getShell());
		if (pa.open() == Dialog.OK) {
			return pa.selection;
		}
		return null;
	}
	
	@Override
	protected String[] parseString(String stringList){
		return stringList.split(","); //$NON-NLS-1$
	}
	
}

class PerspektivenAuswahl extends Dialog {
	String selection;
	private List list;
	
	protected PerspektivenAuswahl(Shell parentShell){
		super(parentShell);
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		list = new List(parent, SWT.BORDER | SWT.SINGLE);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		IExtensionRegistry exr = Platform.getExtensionRegistry();
		IExtensionPoint exp = exr.getExtensionPoint(ExtensionPointConstantsUi.SIDEBAR);
		if (exp != null) {
			IExtension[] extensions = exp.getExtensions();
			for (IExtension ex : extensions) {
				IConfigurationElement[] elems = ex.getConfigurationElements();
				for (IConfigurationElement el : elems) {
					String name = el.getAttribute("name"); //$NON-NLS-1$
					String ID = el.getAttribute("ID"); //$NON-NLS-1$
					list.add(name + ":" + ID); //$NON-NLS-1$
				}
			}
		}
		
		return list;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(Messages.SidebarPreferences_AvailablePerspectives);
	}
	
	@Override
	protected void okPressed(){
		selection = StringTool.join(list.getSelection(), ","); //$NON-NLS-1$
		super.okPressed();
		
	};
}
