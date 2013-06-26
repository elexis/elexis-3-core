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
package ch.elexis.core.ui.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.ScannerEvents;

public class ScannerPref extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String ID = "ch.elexis.preferences.ScannerPrefs"; //$NON-NLS-1$
	
	private static class TextScannerListener implements Listener {
		final Text textfield;
		
		public TextScannerListener(Text textfield){
			super();
			this.textfield = textfield;
		}
		
		public void handleEvent(Event event){
			if (textfield.isFocusControl()) {
				String str = textfield.getText();
				if (str != null && str.length() > 0) {
					str += "; "; //$NON-NLS-1$
				}
				if (event.character > 31) {
					str += event.character;
				}
				str += "(" + event.keyCode + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				textfield.setText(str);
			}
		}
	}
	
	TextScannerListener txtScannerListener;
	Button backupDefaultButton;
	
	SettingsPreferenceStore prefs = new SettingsPreferenceStore(CoreHub.globalCfg);
	
	public ScannerPref(){
		super(GRID);
		
		prefs.setDefault(Preferences.SCANNER_PREFIX_CODE, 0); //$NON-NLS-1$
		prefs.setDefault(Preferences.SCANNER_POSTFIX_CODE, 123456789); //$NON-NLS-1$
		prefs.setDefault(Preferences.BARCODE_LENGTH, 13); //$NON-NLS-1$
		
		setPreferenceStore(prefs);
		setDescription(Messages.ScannerPref_SettingsForScanner);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new IntegerFieldEditor(Preferences.SCANNER_PREFIX_CODE,
			Messages.ScannerPref_ScannerPrefix, getFieldEditorParent(), 10));
		
		addField(new IntegerFieldEditor(Preferences.SCANNER_POSTFIX_CODE,
			Messages.ScannerPref_ScannerPostfix, getFieldEditorParent(), 10));
		
		addField(new IntegerFieldEditor(Preferences.BARCODE_LENGTH,
			Messages.ScannerPref_Barcodelength, getFieldEditorParent(), 50));
	}
	
	@Override
	protected Control createContents(Composite parent){
		super.createContents(parent);
		
		GridLayout noMarginLayout = new GridLayout(3, false);
		noMarginLayout.marginLeft = 0;
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(noMarginLayout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblTest = new Label(comp, SWT.NONE);
		lblTest.setText(Messages.ScannerPref_test);
		lblTest.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		final Text txtTest = new Text(comp, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		txtTest.setLayoutData(new GridData(GridData.FILL_BOTH));
		txtScannerListener = new TextScannerListener(txtTest);
		getShell().getDisplay().addFilter(SWT.KeyDown, txtScannerListener);
		
		backupDefaultButton = getShell().getDefaultButton();
		final Button hiddenBtn = new Button(parent, SWT.PUSH);
		hiddenBtn.setVisible(false);
		txtTest.addFocusListener(new FocusListener() {
			
			public void focusGained(FocusEvent e){
				getShell().setDefaultButton(hiddenBtn);
			}
			
			public void focusLost(FocusEvent e){
				getShell().setDefaultButton(backupDefaultButton);
			}
		});
		
		Button btnClear = new Button(comp, SWT.PUSH);
		btnClear.setText(Messages.ScannerPref_clear);
		btnClear.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				txtTest.setText(""); //$NON-NLS-1$
				txtTest.setFocus();
			}
			
		});
		
		Button btnSteuerblatt = new Button(parent, SWT.PUSH);
		btnSteuerblatt.setText(Messages.ScannerPref_printSheet);
		btnSteuerblatt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				MessageDialog.openInformation(getShell(), Messages.ScannerPref_printSettingsSheet,
					Messages.ScannerPref_NotImplemented);
			}
		});
		
		return parent;
	}
	
	@Override
	public boolean performOk(){
		prefs.flush();
		boolean ok = super.performOk();
		ScannerEvents.getInstance().reloadCodes();
		return ok;
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		if (txtScannerListener != null) {
			getShell().getDisplay().removeFilter(SWT.KeyDown, txtScannerListener);
		}
		super.dispose();
	}
	
}
