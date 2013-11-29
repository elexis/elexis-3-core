/*******************************************************************************
 * Copyright (c) 2006-2010, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    G. Weirich - Adapted for API changes
 * 
 *******************************************************************************/

package ch.elexis.core.ui.laboratory.laborlink;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.laboratory.dialogs.LaborVerordnungDialog;
import ch.elexis.core.ui.laboratory.views.LaborView;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.data.LabItem;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborLink implements IKonsExtension {
	public static final String PROVIDER_ID = "laborlink";
	
	private static final String LABOR_COLOR = "ffc8c8";
	
	IRichTextDisplay textField;
	
	@Override
	public String connect(IRichTextDisplay textField){
		this.textField = textField;
		return PROVIDER_ID;
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		n.background = UiDesk.getColorFromRGB(LABOR_COLOR);
		return true;
	}
	
	public boolean doXRef(String refProvider, String refID){
		// update LaborView and show it
		LaborView laborView =
			(LaborView) Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(UiResourceConstants.LaborView_ID);
		if (laborView != null) {
			ElexisEventDispatcher.getInstance().fire(
				new ElexisEvent(null, LabItem.class, ElexisEvent.EVENT_RELOAD));
			Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.activate(laborView);
		}
		
		return true;
	}
	
	public IAction[] getActions(){
		IAction[] ret = new IAction[1];
		ret[0] = new Action("Labor verordnen") {
			@Override
			public void run(){
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient == null) {
					return;
				}
				TimeTool date = new TimeTool();
				
				LaborVerordnungDialog dialog =
					new LaborVerordnungDialog(UiDesk.getTopShell(), patient, date);
				if (dialog.open() == LaborVerordnungDialog.OK) {
					// insert XRef
					textField.insertXRef(-1, "Labor", PROVIDER_ID, "");
				}
			}
		};
		return ret;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	public void removeXRef(String refProvider, String refID){
		// nothing to do
	}
	
	public void insert(Object o, int pos){
		// TODO Auto-generated method stub
		
	}
}
