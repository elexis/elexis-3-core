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
package ch.elexis.core.ui.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.UiDesk;

import ch.rgw.tools.StringTool;
public class TestDBConnectionGroup extends Group {
	
	private Label lblTestResult;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public TestDBConnectionGroup(Composite parent, int style, final DBConnectWizard connectionWizard){
		super(parent, style);
		setText("Verbindung testen");
		setLayout(new GridLayout(1, false));
		
		final Button btnRestartAfterSwitch = new Button(this, SWT.FLAT | SWT.CHECK);
		btnRestartAfterSwitch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				connectionWizard.setRestartAfterConnectionChange(btnRestartAfterSwitch.getSelection());
			}
		});
		btnRestartAfterSwitch.setSelection(connectionWizard.getRestartAfterConnectionChange());
		btnRestartAfterSwitch.setText("Elexis nach Verbindungswechsel neu starten");
		new Label(this, SWT.NONE);
		
		lblTestResult = new Label(this, SWT.BORDER | SWT.WRAP);
		lblTestResult.setText(StringTool.leer);
		GridData gd_lblTestResult = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_lblTestResult.minimumHeight = 60;
		lblTestResult.setLayoutData(gd_lblTestResult);
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}

	public void setTestResult(boolean error, String text){
		if(error) {
			lblTestResult.setForeground(UiDesk.getColor(UiDesk.COL_RED));
		} else {
			lblTestResult.setForeground(UiDesk.getColor(UiDesk.COL_DARKGREEN));
		}
		lblTestResult.setText(text);
	}

}
