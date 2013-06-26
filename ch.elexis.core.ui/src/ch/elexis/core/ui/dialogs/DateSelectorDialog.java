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
	TimeTool date;
	
	public DateSelectorDialog(Shell parent, TimeTool date){
		super(parent);
		this.date = date;
	}
	
	public DateSelectorDialog(Shell parentShell){
		this(parentShell, new TimeTool());
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		dp = new DatePicker(parent, SWT.BORDER);
		dp.setDate(date.getTime());
		getShell().setText(Messages.getString("DateSelectorDialog.enterDate")); //$NON-NLS-1$
		return dp;
	}
	
	public TimeTool getSelectedDate(){
		return new TimeTool(dp.getDate().getTime());
	}
	
}
