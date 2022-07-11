/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.util;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.tiff.common.ui.datepicker.DatePickerCombo;

import ch.rgw.tools.TimeTool;

public class DateInput extends Composite {
	DatePickerCombo dpc;

	public DateInput(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		dpc = new DatePickerCombo(this, SWT.BORDER);
	}

	public DateInput(final Composite parent, final String label) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		new Label(this, SWT.NONE).setText(label);
		dpc = new DatePickerCombo(this, SWT.BORDER);
		dpc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}

	public DateInput(final Composite parent, final String label, final String date) {
		this(parent, label);
		TimeTool tt = new TimeTool(date);
		dpc.setDate(tt.getTime());
	}

	public TimeTool getDate() {
		String t = dpc.getText();
		if (t.length() == 0) {
			Date d = dpc.getDate();
			return d == null ? null : new TimeTool(d.getTime());
		} else {
			if (t.matches("[0-9]{4,4}")) { //$NON-NLS-1$
				t = "01.01." + t; //$NON-NLS-1$
			}
			if (t.matches("[0-9][0-9]")) { //$NON-NLS-1$
				t = "01.01.20" + t; //$NON-NLS-1$
			}
			return new TimeTool(t);
		}
	}

	public void setDate(TimeTool date) {
		dpc.setDate(date.getTime());
	}

	public void addSelectionListener(SelectionListener listener) {
		dpc.addSelectionListener(listener);
	}
}
