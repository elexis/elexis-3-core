/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package com.tiff.common.ui.datepicker;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.e4.UiDesk;
import ch.rgw.tools.TimeTool;

public class EnhancedDatePickerCombo extends DatePickerCombo {

	private final ExecuteIfValidInterface vi;

	public EnhancedDatePickerCombo(Composite parent, int style, ExecuteIfValidInterface vif) {
		super(parent, style);
		this.vi = vif;

		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				vi.doIt();
				super.widgetSelected(e);
			}

		});

		addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				DatePickerCombo dpc = (DatePickerCombo) e.getSource();
				String text = dpc.getText();
				if (TimeTool.isValidDateTimeString(text, TimeTool.DATE_GER)) {
					setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
					vi.doIt();
				} else {
					setForeground(UiDesk.getColor(UiDesk.COL_RED));
				}
			}
		});

	}

	public static interface ExecuteIfValidInterface {
		public void doIt();
	}
}
