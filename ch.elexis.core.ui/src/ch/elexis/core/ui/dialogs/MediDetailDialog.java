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

import java.util.Arrays;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;

public class MediDetailDialog extends TitleAreaDialog {
	Prescription prescription;
	private String dosis, intakeOrder, disposalComment;
	private Composite compositeDosage;
	private ArticleDefaultSignature ads;
	private Text txtMorning, txtNoon, txtEvening, txtNight;
	private Text txtIntakeOrder, txtDisposalComment;
	private Artikel article;
	private Button btnReserveMedication;
	
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
		ret.setLayout(new GridLayout(1, false));
		
		compositeDosage = new Composite(ret, SWT.NONE);
		compositeDosage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		GridLayout gl_composite = new GridLayout(8, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		compositeDosage.setLayout(gl_composite);
		
		GridData gdSignature = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdSignature.widthHint = 40;
		
		// morning
		txtMorning = new Text(compositeDosage, SWT.BORDER);
		txtMorning.setTextLimit(6);
		txtMorning.setMessage(Messages.MediDetailDialog_morning);
		txtMorning.setLayoutData(gdSignature);
		new Label(compositeDosage, SWT.NONE).setText("-");
		
		// noon
		txtNoon = new Text(compositeDosage, SWT.BORDER);
		txtNoon.setTextLimit(6);
		txtNoon.setMessage(Messages.MediDetailDialog_lunch);
		txtNoon.setLayoutData(gdSignature);
		new Label(compositeDosage, SWT.NONE).setText("-");
		
		// evening
		txtEvening = new Text(compositeDosage, SWT.BORDER);
		txtEvening.setTextLimit(6);
		txtEvening.setMessage(Messages.MediDetailDialog_evening);
		txtEvening.setLayoutData(gdSignature);
		new Label(compositeDosage, SWT.NONE).setText("-");
		
		// night
		txtNight = new Text(compositeDosage, SWT.BORDER);
		txtNight.setTextLimit(6);
		txtNight.setMessage(Messages.MediDetailDialog_night);
		txtNight.setLayoutData(gdSignature);
		
		btnReserveMedication = new Button(compositeDosage, SWT.CHECK);
		btnReserveMedication.setText(Messages.MediDetailDialog_btnReserveMedication);
		btnReserveMedication.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		
		txtIntakeOrder = new Text(ret, SWT.BORDER);
		txtIntakeOrder.setMessage(Messages.MediDetailDialog_intakeOrder);
		txtIntakeOrder.setTextLimit(254);
		txtIntakeOrder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtDisposalComment = new Text(ret, SWT.BORDER);
		txtDisposalComment.setMessage(Messages.MediDetailDialog_disposalComment);
		txtDisposalComment.setTextLimit(254);
		txtDisposalComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if (prescription != null) {
			initTextFields(prescription.getDosis(), prescription.getBemerkung(),
				prescription.getDisposalComment());
			btnReserveMedication.setSelection(prescription.isReserveMedication());
		} else if (ads != null) {
			initTextFields(ads.getSignatureAsDosisString(), ads.getSignatureComment(), "");
		}
		
		return ret;
	}
	
	private void initTextFields(String dose, String intakeOrder, String disposalComment){
		String[] dosage = getDosageArray(dose);
		txtMorning.setText(dosage[0]);
		txtNoon.setText(dosage[1]);
		txtEvening.setText(dosage[2]);
		txtNight.setText(dosage[3]);
		txtIntakeOrder.setText(intakeOrder == null ? "" : intakeOrder);
		txtDisposalComment.setText(disposalComment == null ? "" : disposalComment);
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
		dosis = getDosage();
		intakeOrder = txtIntakeOrder.getText();
		disposalComment = txtDisposalComment.getText();
		
		if (prescription != null) {
			AcquireLockBlockingUi.aquireAndRun(prescription, new ILockHandler() {
				@Override
				public void lockAcquired(){
					prescription.setDosis(dosis);
					prescription.setBemerkung(intakeOrder);
					prescription.setDisposalComment(disposalComment);
					prescription.setReserveMedication(btnReserveMedication.getSelection());
				}
				
				@Override
				public void lockFailed(){
					// do nothing
					
				}
			});
		}
		
		super.okPressed();
	}
	
	public String getDosis(){
		return dosis;
	}
	
	public String getIntakeOrder(){
		return intakeOrder;
	}
	
	private String getDosage(){
		String[] values = new String[4];
		values[0] = txtMorning.getText().isEmpty() ? "0" : txtMorning.getText();
		values[1] = txtNoon.getText().isEmpty() ? "0" : txtNoon.getText();
		values[2] = txtEvening.getText().isEmpty() ? "0" : txtEvening.getText();
		values[3] = txtNight.getText().isEmpty() ? "0" : txtNight.getText();
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			String string = values[i];
			if (string.length() > 0) {
				if (i > 0) {
					sb.append("-");
				}
				sb.append(string);
			}
		}
		return sb.toString();
	}
	
	public String[] getDosageArray(String dosage){
		String[] retVal = new String[4];
		Arrays.fill(retVal, "");
		if (dosage != null) {
			// Match stuff like '1/2', '7/8'
			if (dosage.matches("^[0-9]/[0-9]$")) {
				retVal[0] = dosage;
			} else if (dosage.matches("[0-9Â½Â¼]+([xX][0-9]+(/[0-9]+)?|)")) { //$NON-NLS-1$
				String[] split = dosage.split("[xX]");
				System.arraycopy(split, 0, retVal, 0, split.length);
			} else if (dosage.indexOf('-') != -1) {
				String[] split = dosage.split("[- ]"); //$NON-NLS-1$
				System.arraycopy(split, 0, retVal, 0, split.length);
			}
		}
		return retVal;
	}
}
