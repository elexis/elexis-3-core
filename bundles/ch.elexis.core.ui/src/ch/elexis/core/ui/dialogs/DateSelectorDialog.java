/*******************************************************************************
 * Copyright (c) 2006-2018, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT - provide title selector constructor
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.rgw.tools.TimeTool;

public class DateSelectorDialog extends Dialog {
	CDateTime dp;
	TimeTool preSelectedDate;
	String shellTitle;
	
	public DateSelectorDialog(Shell parent, TimeTool selectedDate, String shellTitle){
		super(parent);
		this.preSelectedDate = (selectedDate == null) ? new TimeTool() : selectedDate;
		this.shellTitle = (shellTitle == null) ? Messages.DateSelectorDialog_enterDate : shellTitle;
	}
	
	public DateSelectorDialog(Shell parent, TimeTool selectedDate){
		this(parent, selectedDate, null);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public DateSelectorDialog(Shell parentShell){
		this(parentShell, new TimeTool());
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.None);
		ret.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ret.setLayout(new GridLayout(1, false));
		dp = new CDateTime(ret, CDT.TAB_FIELDS | CDT.DROP_DOWN | CDT.DATE_SHORT);
		GridData gd_dp = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_dp.widthHint = 100;
		dp.setLayoutData(gd_dp);
		dp.setSelection(preSelectedDate.getTime());
		getShell().setText(shellTitle);
		return ret;
	}
	
	public TimeTool getSelectedDate(){
		return new TimeTool(dp.getSelection());
	}
	
}
