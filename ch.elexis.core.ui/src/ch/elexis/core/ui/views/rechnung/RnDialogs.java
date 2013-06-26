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

package ch.elexis.core.ui.views.rechnung;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.Rechnung;
import ch.elexis.core.data.RnStatus;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.icons.ImageSize;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class RnDialogs {
	public static final int ERR_STORNO = 1;
	private static final String RECHNUNG_IST_STORNIERT = "Rechnung ist storniert";
	
	public static class GebuehrHinzuDialog extends TitleAreaDialog {
		Rechnung rn;
		DatePickerCombo dp;
		Text amount;
		Text bemerkung;
		
		public GebuehrHinzuDialog(Shell shell, Rechnung r) throws ElexisException{
			super(shell);
			if (r.getStatus() == RnStatus.STORNIERT) {
				throw new ElexisException(getClass(), RECHNUNG_IST_STORNIERT, ERR_STORNO);
			}
			rn = r;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			new Label(ret, SWT.NONE).setText(Messages.getString("RnDialogs.date")); //$NON-NLS-1$
			dp = new DatePickerCombo(ret, SWT.NONE);
			dp.setDate(new Date());
			new Label(ret, SWT.NONE).setText(Messages.getString("RnDialogs.amount")); //$NON-NLS-1$
			// nf=NumberFormat.getCurrencyInstance();
			amount = new Text(ret, SWT.BORDER);
			// amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.getString("RnDialogs.remark")); //$NON-NLS-1$
			bemerkung = new Text(ret, SWT.MULTI | SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			amount.setFocus();
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.getString("RnDialogs.invoice") + rn.getNr()); //$NON-NLS-1$
			getShell().setText(Messages.getString("RnDialogs.addExpense")); //$NON-NLS-1$
			setMessage(Messages.getString("RnDialogs.enterAmount")); //$NON-NLS-1$
			setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		}
		
		@Override
		protected void okPressed(){
			
			// Number num=df.parse(amount.getText());
			Money ret = MoneyInput.getFromTextField(amount);
			if (ret != null) {
				ret = ret.multiply(-1.0);
				rn.addZahlung(ret, bemerkung.getText(), new TimeTool(dp.getDate().getTime()));
				super.okPressed();
			} else {
				ErrorDialog
					.openError(
						getShell(),
						Messages.getString("RnDialogs.amountInvalid"), Messages.getString("RnDialogs.invalidFormat"), //$NON-NLS-1$ //$NON-NLS-2$
						new Status(1, "ch.elexis", 1, "CurrencyFormat", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		}
	}
	
	public static class BuchungHinzuDialog extends TitleAreaDialog {
		
		Rechnung rn;
		DatePickerCombo dp;
		Text amount, bemerkung;
		
		public BuchungHinzuDialog(Shell shell, Rechnung r) throws ElexisException{
			super(shell);
			if (r.getStatus() == RnStatus.STORNIERT) {
				throw new ElexisException(getClass(), RECHNUNG_IST_STORNIERT, ERR_STORNO);
			}
			rn = r;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.getString("RnDialogs.date")); //$NON-NLS-1$
			dp = new DatePickerCombo(ret, SWT.NONE);
			dp.setDate(new Date());
			new Label(ret, SWT.NONE).setText(Messages.getString("RnDialogs.amount")); //$NON-NLS-1$
			// nf=NumberFormat.getCurrencyInstance();
			amount = new Text(ret, SWT.BORDER);
			// amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.getString("RnDialogs.remark")); //$NON-NLS-1$
			bemerkung = new Text(ret, SWT.MULTI | SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setFocus();
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.getString("RnDialogs.invoice") + rn.getNr()); //$NON-NLS-1$
			getShell().setText(Messages.getString("RnDialogs.addTransaction")); //$NON-NLS-1$
			setMessage(Messages.getString("RnDialogs.enterAmount")); //$NON-NLS-1$
			setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		}
		
		@Override
		protected void okPressed(){
			// Number num=df.parse(amount.getText());
			Money ret = MoneyInput.getFromTextField(amount);
			if (ret != null) {
				rn.addZahlung(ret, bemerkung.getText(), new TimeTool(dp.getDate().getTime()));
				super.okPressed();
			} else {
				ErrorDialog
					.openError(
						getShell(),
						Messages.getString("RnDialogs.amountInvalid"), Messages.getString("RnDialogs.invalidFormat"), //$NON-NLS-1$ //$NON-NLS-2$
						new Status(1, "ch.elexis", 1, "CurrencyFormat", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		}
		
	}
	
	public static class StatusAendernDialog extends TitleAreaDialog {
		Rechnung rn;
		Combo cbStates;
		
		// RnStatus[] states=RnStatus.Text;
		
		public StatusAendernDialog(Shell shell, Rechnung r){
			super(shell);
			rn = r;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cbStates = new Combo(ret, SWT.READ_ONLY);
			cbStates.setItems(RnStatus.getStatusTexts());
			cbStates.setVisibleItemCount(RnStatus.getStatusTexts().length);
			cbStates.select(rn.getStatus());
			cbStates.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.WRAP).setText(Messages
				.getString("RnDialogs.warningDontChangeManually")); //$NON-NLS-1$
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.getString("RnDialogs.invoiceNumber") + rn.getNr()); //$NON-NLS-1$
			setTitle(Messages.getString("RnDialogs.modifyInvoiceState")); //$NON-NLS-1$
			
			setMessage(rn.getFall().getPatient().getLabel()
				+ Messages.getString("RnDialogs.pleaseNewState")); //$NON-NLS-1$
		}
		
		@Override
		protected void okPressed(){
			int idx = cbStates.getSelectionIndex();
			if (idx != -1) {
				rn.setStatus(idx);
			}
			super.okPressed();
		}
		
	}
	
	public static class StornoDialog extends TitleAreaDialog {
		Rechnung rn;
		Button bReactivate;
		List<Button> exporters = new ArrayList<Button>();
		private List<IRnOutputter> lo;
		
		public StornoDialog(Shell shell, Rechnung r){
			super(shell);
			rn = r;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(Composite parent){
			lo = Extensions.getClasses("ch.elexis.RechnungsManager", "outputter"); //$NON-NLS-1$ //$NON-NLS-2$
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			Label lbLocal = new Label(ret, SWT.NONE);
			lbLocal.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			lbLocal.setText(Messages.getString("RnDialogs.stornoOnlyLocal")); //$NON-NLS-1$
			for (IRnOutputter rno : lo) {
				if (rno.canStorno(null) && hasTrace(rno.getDescription())) {
					Button cbStorno = new Button(ret, SWT.CHECK);
					cbStorno.setData(rno);
					cbStorno.setText(rno.getDescription());
					cbStorno.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					cbStorno.setSelection(true);
					exporters.add(cbStorno);
				}
			}
			if (exporters.size() > 0) {
				lbLocal.setText(Messages.getString("RnDialogs.stornoPropagate")); //$NON-NLS-1$
			}
			new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(
				1, false, 1, false));
			bReactivate = new Button(ret, SWT.CHECK);
			bReactivate.setText(Messages.getString("RnDialogs.reactivateConsultations")); //$NON-NLS-1$
			bReactivate.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			bReactivate.setSelection(true);
			/*
			 * bYes=new Button(ret,SWT.RADIO); bNo=new Button(ret,SWT.RADIO);
			 * bYes.setText(Messages.getString("RnDialogs.yes")); //$NON-NLS-1$
			 * bNo.setText(Messages.getString("RnDialogs.no")); //$NON-NLS-1$
			 */
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			return ret;
		}
		
		private boolean hasTrace(String msg){
			List<String> msgs = rn.getTrace(Rechnung.OUTPUT);
			for (String m : msgs) {
				if (m.indexOf(msg) > -1) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.getString("RnDialogs.invoice") + rn.getNr()); //$NON-NLS-1$
			setTitle(Messages.getString("RnDialogs.reallyCancel")); //$NON-NLS-1$
			//setMessage(Messages.getString("RnDialogs.reactivateConsultations")); //$NON-NLS-1$
		}
		
		@Override
		protected void okPressed(){
			rn.storno(bReactivate.getSelection());
			for (Button exporter : exporters) {
				if (exporter.getSelection()) {
					IRnOutputter iro = (IRnOutputter) exporter.getData();
					if (iro != null) {
						iro.doOutput(IRnOutputter.TYPE.STORNO, Arrays.asList(new Rechnung[] {
							rn
						}), new Properties());
					}
				}
			}
			super.okPressed();
		}
		
	}
}
