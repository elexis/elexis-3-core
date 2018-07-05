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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePicker;

public class DateSelectorDialog extends Dialog {
	DatePicker dp;
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
	
	public DateSelectorDialog(Shell parentShell){
		this(parentShell, new TimeTool());
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		dp = new DatePicker(parent, SWT.BORDER);
		dp.setDate(preSelectedDate.getTime());
		getShell().setText(shellTitle);
		return dp;
	}
	
	public TimeTool getSelectedDate(){
		return new TimeTool(dp.getDate().getTime());
	}
	
}
