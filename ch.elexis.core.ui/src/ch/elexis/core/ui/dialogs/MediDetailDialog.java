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

package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Prescription;

public class MediDetailDialog extends TitleAreaDialog {
	Prescription art;
	Text dosis;
	Text einnahme;
	
	public MediDetailDialog(Shell shell, Prescription a){
		super(shell);
		art = a;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		new Label(ret, SWT.NONE).setText(Messages.MediDetailDialog_dosage); //$NON-NLS-1$
		dosis = new Text(ret, SWT.BORDER);
		GridData gd_dosis = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_dosis.widthHint = 80;
		dosis.setLayoutData(gd_dosis);
		dosis.setText(art.getDosis());
		dosis.setTextLimit(254);
		new Label(ret, SWT.NONE).setText(Messages.MediDetailDialog_prescription); //$NON-NLS-1$
		einnahme = new Text(ret, SWT.MULTI);
		einnahme.setText(art.getBemerkung());
		einnahme.setTextLimit(254);
		einnahme.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(art.getArtikel().getLabel());
		setMessage(Messages.MediDetailDialog_pleaseEnterPrescription); //$NON-NLS-1$
		getShell().setText(Messages.MediDetailDialog_articleDetail); //$NON-NLS-1$
		
	}
	
	@Override
	protected void okPressed(){
		art.setDosis(dosis.getText());
		art.set(Prescription.REMARK, einnahme.getText());
		super.okPressed();
	}
	
}
