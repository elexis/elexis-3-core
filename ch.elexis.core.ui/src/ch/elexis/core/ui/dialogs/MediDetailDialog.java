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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Prescription;

public class MediDetailDialog extends TitleAreaDialog {
	Prescription prescription;
	Text dosis;
	Text einnahme;
	private Composite composite;
	private Button btnAssignDefaultSignature;
	private ArticleDefaultSignature ads;
	
	public MediDetailDialog(Shell shell, Prescription prescription){
		super(shell);
		this.prescription = prescription;
		
		this.ads =
			ArticleDefaultSignature.getDefaultsignatureForArticle(prescription.getArtikel());
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		new Label(ret, SWT.NONE).setText(Messages.MediDetailDialog_dosage); //$NON-NLS-1$
		
		composite = new Composite(ret, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		dosis = new Text(composite, SWT.BORDER);
		dosis.setText(prescription.getDosis());
		dosis.setTextLimit(254);
		
		btnAssignDefaultSignature = new Button(composite, SWT.FLAT);
		btnAssignDefaultSignature.setImage(Images.IMG_BOOKMARK_PENCIL.getImage());
		btnAssignDefaultSignature.setText(Messages.MediDetailDialog_lblNewLabel_text);
		btnAssignDefaultSignature.setEnabled(ads!=null);
		
		new Label(ret, SWT.NONE).setText(Messages.MediDetailDialog_prescription); //$NON-NLS-1$
		einnahme = new Text(ret, SWT.MULTI);
		einnahme.setText(prescription.getBemerkung());
		einnahme.setTextLimit(254);
		einnahme.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		btnAssignDefaultSignature.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				dosis.setText(ads.getSignatureAsDosisString());
				einnahme.setText(ads.getSignatureComment());
			}
		});
		
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(prescription.getArtikel().getLabel());
		setMessage(Messages.MediDetailDialog_pleaseEnterPrescription); //$NON-NLS-1$
		getShell().setText(Messages.MediDetailDialog_articleDetail); //$NON-NLS-1$
		
	}
	
	@Override
	protected void okPressed(){
		prescription.setDosis(dosis.getText());
		prescription.set(Prescription.REMARK, einnahme.getText());
		super.okPressed();
	}
	
}
