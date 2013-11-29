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
	DateInput diRnVon, diRnBis, diStatVon, diStatBis;
	
	public RnFilterDialog(final Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(3, false));
		new Label(ret, SWT.NONE).setText(Messages.RnFilterDialog_amount); //$NON-NLS-1$
		miVon = new MoneyInput(ret, FROM);
		miBis = new MoneyInput(ret, UNTIL);
		new Label(ret, SWT.NONE).setText(Messages.RnFilterDialog_billDate); //$NON-NLS-1$
		diRnVon = new DateInput(ret, FROM);
		diRnBis = new DateInput(ret, UNTIL);
		new Label(ret, SWT.NONE).setText(Messages.RnFilterDialog_stateDate); //$NON-NLS-1$
		diStatVon = new DateInput(ret, FROM);
		diStatBis = new DateInput(ret, UNTIL);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.RnFilterDialog_billsFilterCaption); //$NON-NLS-1$
		setMessage(Messages.RnFilterDialog_billsFilterMessage); //$NON-NLS-1$
		getShell().setText(Messages.RnFilterDialog_billsList); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		ArrayList<String> al = new ArrayList<String>();
		Money mFrom = miVon.getMoney(true);
		Money mUntil = miBis.getMoney(true);
		
		if (mFrom != null) {
			// String sFrom=StringTool.pad(SWT.LEFT, '0', mFrom.getCentsAsString(), 9);
			al.add(PersistentObject.getConnection().translateFlavor(
				"cast(Betrag as SIGNED) >=" + mFrom.getCentsAsString())); //$NON-NLS-1$
		}
		if (mUntil != null) {
			// String sUntil=StringTool.pad(SWT.LEFT, '0', mUntil.getCentsAsString(), 9);
			al.add(PersistentObject.getConnection().translateFlavor(
				"cast(Betrag as SIGNED) <=" + mUntil.getCentsAsString())); //$NON-NLS-1$
		}
		TimeTool tt = diRnVon.getDate();
		if (tt != null) {
			al.add("RnDatum >=" + PersistentObject.getConnection().wrapFlavored(tt.toString(TimeTool.DATE_COMPACT))); //$NON-NLS-1$
		}
		tt = diRnBis.getDate();
		if (tt != null) {
			al.add("RnDatum <=" + PersistentObject.getConnection().wrapFlavored(tt.toString(TimeTool.DATE_COMPACT))); //$NON-NLS-1$
		}
		tt = diStatVon.getDate();
		if (tt != null) {
			al.add("StatusDatum >=" + PersistentObject.getConnection().wrapFlavored(tt.toString(TimeTool.DATE_COMPACT))); //$NON-NLS-1$
		}
		tt = diStatBis.getDate();
		if (tt != null) {
			al.add("StatusDatum <=" + PersistentObject.getConnection().wrapFlavored(tt.toString(TimeTool.DATE_COMPACT))); //$NON-NLS-1$
		}
		if (al.size() > 0) {
			ret = al.toArray(new String[0]);
		} else {
			ret = null;
		}
		
		super.okPressed();
	}
	
}
