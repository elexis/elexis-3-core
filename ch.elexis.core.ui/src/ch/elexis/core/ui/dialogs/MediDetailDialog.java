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
import org.eclipse.swt.custom.StackLayout;
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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;

public class MediDetailDialog extends TitleAreaDialog {
	Prescription prescription;
	private String dosis, intakeOrder, disposalComment;
	private Composite stackCompositeDosage, compositeDayTimeDosage, compositeFreeTextDosage;
	private StackLayout stackLayoutDosage;
	private Text txtMorning, txtNoon, txtEvening, txtNight, txtFreeText;
	private Text txtIntakeOrder, txtDisposalComment;
	private Artikel article;
	private Button btnReserveMedication;
	
	private String executedFrom;
	
	private boolean createPrescriptionHistoryEntry;
	
	/**
	 * @wbp.parser.constructor
	 */
	public MediDetailDialog(Shell shell, Prescription prescription){
		this(shell, prescription, false);
	}
	
	/**
	 * Creates optional also a history entry for a prescription if it changed
	 * 
	 * @param shell
	 * @param prescription
	 * @param createPrescriptionHistoryEntry
	 */
	public MediDetailDialog(Shell shell, Prescription prescription,
		boolean createPrescriptionHistoryEntry){
		super(shell);
		this.prescription = prescription;
		this.createPrescriptionHistoryEntry = createPrescriptionHistoryEntry;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(3, false));
		
		stackCompositeDosage = new Composite(ret, SWT.NONE);
		stackCompositeDosage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		stackLayoutDosage = new StackLayout();
		stackCompositeDosage.setLayout(stackLayoutDosage);
		
		compositeDayTimeDosage = new Composite(stackCompositeDosage, SWT.NONE);
		compositeDayTimeDosage
			.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_compositeDayTimeDosage = new GridLayout(7, false);
		gl_compositeDayTimeDosage.marginWidth = 0;
		gl_compositeDayTimeDosage.marginHeight = 0;
		gl_compositeDayTimeDosage.verticalSpacing = 1;
		gl_compositeDayTimeDosage.horizontalSpacing = 0;
		compositeDayTimeDosage.setLayout(gl_compositeDayTimeDosage);
		
		compositeFreeTextDosage = new Composite(stackCompositeDosage, SWT.NONE);
		compositeFreeTextDosage
			.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_compositeFreeTextDosage = new GridLayout(1, false);
		gl_compositeFreeTextDosage.marginWidth = 0;
		gl_compositeFreeTextDosage.marginHeight = 0;
		gl_compositeFreeTextDosage.verticalSpacing = 1;
		gl_compositeFreeTextDosage.horizontalSpacing = 0;
		compositeFreeTextDosage.setLayout(gl_compositeFreeTextDosage);
		
		GridData gdSignature = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdSignature.widthHint = 40;
		
		// morning
		txtMorning = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtMorning.setTextLimit(6);
		txtMorning.setMessage(Messages.MediDetailDialog_morning);
		txtMorning.setLayoutData(gdSignature);
		new Label(compositeDayTimeDosage, SWT.NONE).setText("-");
		
		// noon
		txtNoon = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtNoon.setTextLimit(6);
		txtNoon.setMessage(Messages.MediDetailDialog_lunch);
		txtNoon.setLayoutData(gdSignature);
		new Label(compositeDayTimeDosage, SWT.NONE).setText("-");
		
		// evening
		txtEvening = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtEvening.setTextLimit(6);
		txtEvening.setMessage(Messages.MediDetailDialog_evening);
		txtEvening.setLayoutData(gdSignature);
		new Label(compositeDayTimeDosage, SWT.NONE).setText("-");
		
		// night
		txtNight = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtNight.setTextLimit(6);
		txtNight.setMessage(Messages.MediDetailDialog_night);
		txtNight.setLayoutData(gdSignature);
		
		Button btnDoseSwitch = new Button(ret, SWT.PUSH);
		btnDoseSwitch.setImage(Images.IMG_SYNC.getImage());
		btnDoseSwitch.setToolTipText(Messages.MediDetailDialog_tooltipDosageType);
		btnDoseSwitch.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e){
				if (stackLayoutDosage.topControl == compositeDayTimeDosage) {
					stackLayoutDosage.topControl = compositeFreeTextDosage;
				} else {
					stackLayoutDosage.topControl = compositeDayTimeDosage;
				}
				stackCompositeDosage.layout();
			};
		});
		
		txtFreeText = new Text(compositeFreeTextDosage, SWT.BORDER);
		txtFreeText.setMessage(Messages.MediDetailDialog_freetext);
		GridData gd_txtFreeText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtFreeText.widthHint = 210;
		txtFreeText.setLayoutData(gd_txtFreeText);
		txtFreeText.setTextLimit(255);
		
		btnReserveMedication = new Button(ret, SWT.CHECK);
		btnReserveMedication.setText(Messages.MediDetailDialog_btnReserveMedication);
		btnReserveMedication.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		txtIntakeOrder = new Text(ret, SWT.BORDER);
		txtIntakeOrder.setMessage(Messages.MediDetailDialog_intakeOrder);
		txtIntakeOrder.setTextLimit(254);
		txtIntakeOrder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		txtDisposalComment = new Text(ret, SWT.BORDER);
		txtDisposalComment.setMessage(Messages.MediDetailDialog_disposalComment);
		txtDisposalComment.setTextLimit(254);
		txtDisposalComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		stackLayoutDosage.topControl = compositeDayTimeDosage;
		
		if (prescription != null) {
			initTextFields(prescription.getDosis(), prescription.getBemerkung(),
				prescription.getDisposalComment());
			btnReserveMedication.setSelection(prescription.isReserveMedication());
		}
		stackCompositeDosage.layout();
		
		// show or hide components dependent on caller
		if ("FixMediDisplay".equals(executedFrom)) {
			btnReserveMedication.setVisible(false);
		}
		
		return ret;
	}
	
	private void initTextFields(String dose, String intakeOrder, String disposalComment){
		
		String[] dosage = Prescription.getSignatureAsStringArray(dose);
		if (isFreeText(dosage)) {
			txtFreeText.setText(dosage[0]);
			stackLayoutDosage.topControl = compositeFreeTextDosage;
			
		} else {
			txtMorning.setText(dosage[0]);
			txtNoon.setText(dosage[1]);
			txtEvening.setText(dosage[2]);
			txtNight.setText(dosage[3]);
			stackLayoutDosage.topControl = compositeDayTimeDosage;
		}
		
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
					if (createPrescriptionHistoryEntry) {
						// creates a history entry for a prescription, stops the old one with current date
						Prescription oldPrescription = prescription;
						Prescription newPrescription = new Prescription(oldPrescription);
						if (CoreHub.getLocalLockService().acquireLock(newPrescription).isOk()) {
							newPrescription.setDosis(dosis);
							newPrescription.setBemerkung(intakeOrder);
							newPrescription.setDisposalComment(disposalComment);
							oldPrescription.stop(null);
							oldPrescription
								.setStopReason("Geändert durch " + CoreHub.actUser.getLabel());
							CoreHub.getLocalLockService().releaseLock(newPrescription);
							ElexisEventDispatcher.getInstance().fire(new ElexisEvent(
								newPrescription, Prescription.class, ElexisEvent.EVENT_UPDATE));
						}
						
					} else {
						// no history entry for example recipe
						prescription.setDosis(dosis);
						prescription.setBemerkung(intakeOrder);
						prescription.setDisposalComment(disposalComment);
						if (btnReserveMedication.getSelection()) {
							prescription.setEntryType(EntryType.RESERVE_MEDICATION);
						}
					}
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
		if (stackLayoutDosage.topControl == compositeDayTimeDosage) {
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
		else {
			return txtFreeText.getText();
		}
	}
	
	/**
	 * @deprecated use Prescription.getSignatureAsStringArray
	 * @param dosage
	 * @return
	 */
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
	
	public boolean isFreeText(String[] signatureArray){
		return !signatureArray[0].isEmpty() && signatureArray[1].isEmpty()
			&& signatureArray[2].isEmpty() && signatureArray[3].isEmpty();
	}
	
	public void setExecutedFrom(String executedFrom){
		this.executedFrom = executedFrom;
	}
	
	public String getExecutedFrom(){
		return executedFrom;
	}
}
