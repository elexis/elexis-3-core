/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.DateInput;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class RnFilterDialog extends TitleAreaDialog {
	static final String FROM = Messages.RnFilterDialog_fromDate; //$NON-NLS-1$
	static final String UNTIL = Messages.RnFilterDialog_untilDate; //$NON-NLS-1$
	String[] ret;
	MoneyInput miVon, miBis;
	DateInput diRnVon, diRnBis, diStatVon, diStatBis, diOutVon, diOutBis;
	
	private TimeTool invoiceDateFrom;
	private TimeTool invoiceDateTo;
	private TimeTool invoiceStateDateFrom;
	private TimeTool invoiceStateDateTo;
	private TimeTool invoiceOutputDateFrom;
	private TimeTool invoiceOutputDateTo;
	
	private boolean includeMoneySelector;
	
	public RnFilterDialog(final Shell parentShell){
		this(parentShell, true);
	}
	
	/**
	 * 
	 * @param parentShell
	 * @param includeMoneySelector
	 *            whether to show and populate the money selection
	 * @since 3.2
	 */
	public RnFilterDialog(final Shell parentShell, boolean includeMoneySelector){
		super(parentShell);
		this.includeMoneySelector = includeMoneySelector;
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));
		if (includeMoneySelector) {
			new Label(ret, SWT.NONE).setText(Messages.RnFilterDialog_amount); //$NON-NLS-1$
			miVon = new MoneyInput(ret, FROM);
			miBis = new MoneyInput(ret, UNTIL);
		}
		Label lblBillDate = new Label(ret, SWT.NONE);
		lblBillDate.setText(Messages.RnFilterDialog_billDate); //$NON-NLS-1$
		lblBillDate.setLayoutData(SWTHelper.getFillGridData(2, false, 1, true));
		diRnVon = new DateInput(ret, FROM);
		diRnBis = new DateInput(ret, UNTIL);
		
		Label lblStateDate = new Label(ret, SWT.NONE);
		lblStateDate.setText(Messages.RnFilterDialog_stateDate); //$NON-NLS-1$
		lblStateDate.setLayoutData(SWTHelper.getFillGridData(2, false, 1, true));
		diStatVon = new DateInput(ret, FROM);
		diStatBis = new DateInput(ret, UNTIL);
		
		Label lblOutputDate = new Label(ret, SWT.NONE);
		lblOutputDate.setText(Messages.RnFilterDialog_outputDate); //$NON-NLS-1$
		Label lblOutputDateInfo = new Label(ret, SWT.NONE);
		lblOutputDateInfo.setImage(Images.IMG_ACHTUNG.getImage());
		lblOutputDateInfo.setToolTipText(Messages.RnFilterDialog_outputDateInfo);
		diOutVon = new DateInput(ret, FROM);
		diOutBis = new DateInput(ret, UNTIL);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.RnFilterDialog_billsFilterCaption);
		setMessage(Messages.RnFilterDialog_billsFilterMessage);
		getShell().setText(Messages.RnFilterDialog_billsList);
	}
	
	public TimeTool getInvoiceDateFrom(){
		return invoiceDateFrom;
	}
	
	public TimeTool getInvoiceDateTo(){
		return invoiceDateTo;
	}
	
	public TimeTool getInvoiceStateDateFrom(){
		return invoiceStateDateFrom;
	}
	
	public TimeTool getInvoiceStateDateTo(){
		return invoiceStateDateTo;
	}
	
	public TimeTool getInvoiceOutputDateFrom(){
		return invoiceOutputDateFrom;
	}
	
	public TimeTool getInvoiceOutputDateTo(){
		return invoiceOutputDateTo;
	}
	
	@Override
	protected void okPressed(){
		ArrayList<String> al = new ArrayList<String>();
		
		if (includeMoneySelector) {
			Money mFrom = miVon.getMoney(true);
			Money mUntil = miBis.getMoney(true);
			
			if (mFrom != null) {
				// String sFrom=StringTool.pad(SWT.LEFT, '0', mFrom.getCentsAsString(), 9);
				al.add(PersistentObject.getConnection()
					.translateFlavor("cast(Betrag as SIGNED) >=" + mFrom.getCentsAsString())); //$NON-NLS-1$
			}
			if (mUntil != null) {
				// String sUntil=StringTool.pad(SWT.LEFT, '0', mUntil.getCentsAsString(), 9);
				al.add(PersistentObject.getConnection()
					.translateFlavor("cast(Betrag as SIGNED) <=" + mUntil.getCentsAsString())); //$NON-NLS-1$
			}
		}
		
		invoiceDateFrom = diRnVon.getDate();
		if (invoiceDateFrom != null) {
			al.add("RnDatum >=" + PersistentObject.getConnection() //$NON-NLS-1$
				.wrapFlavored(invoiceDateFrom.toString(TimeTool.DATE_COMPACT)));
		}
		invoiceDateTo = diRnBis.getDate();
		if (invoiceDateTo != null) {
			al.add("RnDatum <=" + PersistentObject.getConnection() //$NON-NLS-1$
				.wrapFlavored(invoiceDateTo.toString(TimeTool.DATE_COMPACT)));
		}
		invoiceStateDateFrom = diStatVon.getDate();
		if (invoiceStateDateFrom != null) {
			al.add("StatusDatum >=" + PersistentObject.getConnection() //$NON-NLS-1$
				.wrapFlavored(invoiceStateDateFrom.toString(TimeTool.DATE_COMPACT)));
		}
		invoiceStateDateTo = diStatBis.getDate();
		if (invoiceStateDateTo != null) {
			al.add("StatusDatum <=" + PersistentObject.getConnection() //$NON-NLS-1$
				.wrapFlavored(invoiceStateDateTo.toString(TimeTool.DATE_COMPACT)));
		}
		invoiceOutputDateFrom = diOutVon.getDate();
		invoiceOutputDateTo = diOutBis.getDate();
		
		if (al.size() > 0) {
			ret = al.toArray(new String[0]);
		} else {
			ret = null;
		}
		
		super.okPressed();
	}
	
}
