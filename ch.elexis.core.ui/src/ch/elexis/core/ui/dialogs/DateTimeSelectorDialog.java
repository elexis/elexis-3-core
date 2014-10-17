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

import java.util.Calendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.rgw.tools.TimeTool;

public class DateTimeSelectorDialog extends Dialog {
	private TimeTool date;
	private DateTime timeSelection;
	private DateTime dateSelection;
	private TimeTool ret;
	
	public DateTimeSelectorDialog(Shell parent, TimeTool date){
		super(parent);
		this.date = date;
	}
	
	public DateTimeSelectorDialog(Shell parentShell){
		this(parentShell, new TimeTool());
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label label = new Label(composite, SWT.NONE);
		label.setText("Zeitpunkt");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		Composite dateComposite = new Composite(composite, SWT.NONE);
		dateComposite.setLayout(new GridLayout(2, true));
		dateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		timeSelection = new DateTime(dateComposite, SWT.TIME);
		timeSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dateSelection = new DateTime(dateComposite, SWT.DATE);
		dateSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		timeSelection.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
			date.get(Calendar.SECOND));
		dateSelection.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
			date.get(Calendar.DAY_OF_MONTH));

		getShell().setText(Messages.DateTimeSelectorDialog_enterDate); //$NON-NLS-1$
		return composite;
	}
	
	@Override
	protected void okPressed(){
		ret = new TimeTool();
		getTime(timeSelection, ret);
		getDate(dateSelection, ret);
		super.okPressed();
	}
	
	public TimeTool getSelectedDate(){

		return ret;
	}
	
	private void getTime(DateTime widget, TimeTool time){
		time.set(Calendar.HOUR_OF_DAY, widget.getHours());
		time.set(Calendar.MINUTE, widget.getMinutes());
		time.set(Calendar.SECOND, widget.getSeconds());
	}
	
	private void getDate(DateTime widget, TimeTool date){
		date.set(Calendar.YEAR, widget.getYear());
		date.set(Calendar.MONTH, widget.getMonth());
		date.set(Calendar.DAY_OF_MONTH, widget.getDay());
	}
}
