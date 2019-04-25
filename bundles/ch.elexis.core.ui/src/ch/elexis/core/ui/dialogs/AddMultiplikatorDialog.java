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

import java.time.format.DateTimeFormatter;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class AddMultiplikatorDialog extends TitleAreaDialog {
	CDateTime dpc;
	Text multi;
	TimeTool begindate;
	String mult;
	
	public AddMultiplikatorDialog(final Shell shell){
		super(shell);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		Label l1 = new Label(ret, SWT.NONE);
		l1.setText(Messages.AccountView_date);
		dpc = new CDateTime(ret, CDT.HORIZONTAL | CDT.DATE_SHORT | CDT.DROP_DOWN | SWT.BORDER | CDT.TAB_FIELDS);
		String value = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("01.01.y"));
		dpc.setSelection(new TimeTool(value).getTime());
		dpc.setToolTipText(Messages.MultiplikatorEditor_PleaseEnterBeginDate);
		Label label = new Label(ret, SWT.NONE);
		label.setText(Messages.Leistungscodes_multiplierLabel);
		multi = new Text(ret, SWT.BORDER);
		multi.setTextLimit(6);
		multi.setToolTipText(Messages.MultiplikatorEditor_NewMultipilcator);
		multi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.MultiplikatorEditor_BeginDate); //$NON-NLS-1$
		setMessage(Messages.MultiplikatorEditor_PleaseEnterBeginDate); //$NON-NLS-1$
		getShell().setText(Messages.MultiplikatorEditor_NewMultipilcator); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		begindate = new TimeTool(dpc.getSelection());
		mult = multi.getText();
		try {
			if (mult.isEmpty() ||  Float.parseFloat(mult) <= 0.0) {
				SWTHelper.showError(Messages.MultiplikatorEditor_ErrorTitle,
						Messages.MultiplikatorEditor_ErrorMessage);
				super.cancelPressed();
			} else {
				super.okPressed();
			}
		}  catch (Exception ex) {
			SWTHelper.showError(Messages.MultiplikatorEditor_ErrorTitle,
					Messages.MultiplikatorEditor_ErrorMessage);
			super.cancelPressed();
		}
	}
	
	public TimeTool getBegindate(){
		return begindate;
	}
	
	public String getMult(){
		return mult;
	}
}
