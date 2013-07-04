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
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Patientenblatt2;
import ch.rgw.tools.StringTool;

public class BezugsKontaktAuswahl extends Dialog {
	Combo cbType;
	String result = ""; //$NON-NLS-1$
	
	public BezugsKontaktAuswahl(){
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(ch.elexis.core.ui.views.Messages.Patientenblatt2_kindOfRelation); //$NON-NLS-1$
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		new Label(ret, SWT.NONE).setText(ch.elexis.core.ui.views.Messages.Patientenblatt2_pleaseEnterKindOfRelationship); //$NON-NLS-1$
		cbType = new Combo(ret, SWT.NONE);
		cbType.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		String bez = CoreHub.globalCfg.get(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN, ""); //$NON-NLS-1$
		cbType.setItems(bez.split(Patientenblatt2.SPLITTER));
		return ret;
	}
	
	@Override
	protected void okPressed(){
		result = cbType.getText();
		String[] items = cbType.getItems();
		String nitem = cbType.getText();
		String res = ""; //$NON-NLS-1$
		if (StringTool.getIndex(items, nitem) == -1) {
			res = nitem + Patientenblatt2.SPLITTER;
		}
		CoreHub.globalCfg.set(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN,
			res + StringTool.join(items, Patientenblatt2.SPLITTER));
		super.okPressed();
	}
	
	public String getResult(){
		return result;
	}
	
}
