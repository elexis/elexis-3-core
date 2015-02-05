/*******************************************************************************
 * Copyright (c) 2005-2015, G. Weirich and Elexis
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
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;

public class MediDetailDialog extends TitleAreaDialog {
	Prescription prescription;
	String dosis;
	String einnahme;
	private Composite composite;
	private ArticleDefaultSignature ads;
	private Text txtDosis;
	private Text txtEinnahme;
	private Artikel article;
	
	/**
	 * @wbp.parser.constructor
	 */
	public MediDetailDialog(Shell shell, Prescription prescription){
		super(shell);
		this.prescription = prescription;
		
		this.ads =
			ArticleDefaultSignature.getDefaultsignatureForArticle(prescription.getArtikel());	
	}
	
	/**
	 * 
	 * @since 3.1.0
	 */
	public MediDetailDialog(Shell shell, Artikel article){
		super(shell);
		this.prescription = null;
		this.article = article;
		
		this.ads =
			ArticleDefaultSignature.getDefaultsignatureForArticle(article);	
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		new Label(ret, SWT.NONE).setText(Messages.MediDetailDialog_dosage); //$NON-NLS-1$
		
		composite = new Composite(ret, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		txtDosis = new Text(composite, SWT.BORDER);
		GridData gd_txtDosis = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtDosis.widthHint = 80;
		txtDosis.setLayoutData(gd_txtDosis);

		txtDosis.setTextLimit(254);
		
		new Label(ret, SWT.NONE).setText(Messages.MediDetailDialog_prescription);
		txtEinnahme = new Text(ret, SWT.MULTI);

		txtEinnahme.setTextLimit(254);
		txtEinnahme.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		if(prescription!=null) {
			txtDosis.setText(prescription.getDosis());
			txtEinnahme.setText(prescription.getBemerkung());
		} else if(ads!=null) {
			txtDosis.setText(ads.getSignatureAsDosisString());
			txtEinnahme.setText(ads.getSignatureComment());
		} 
		
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		if(prescription!=null) {
			setTitle(prescription.getArtikel().getLabel());
		} else if (article!=null) {
			setTitle(article.getLabel());
		}
		

		setMessage(Messages.MediDetailDialog_pleaseEnterPrescription);
		getShell().setText(Messages.MediDetailDialog_articleDetail);
	}
	
	@Override
	protected void okPressed(){
		dosis = txtDosis.getText();
		einnahme = txtEinnahme.getText();
		
		if(prescription!=null) {
			prescription.setDosis(dosis);
			prescription.set(Prescription.FLD_REMARK, einnahme);
		}
		
		super.okPressed();
	}
	
	public String getDosis(){
		return dosis;
	}
	
	public String getEinnahme(){
		return einnahme;
	}
	
}
