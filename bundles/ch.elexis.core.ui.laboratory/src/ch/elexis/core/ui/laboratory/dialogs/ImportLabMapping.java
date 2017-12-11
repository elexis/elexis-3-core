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
package ch.elexis.core.ui.laboratory.dialogs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.LabItem;
import ch.elexis.data.LabMapping;

public class ImportLabMapping extends TitleAreaDialog {
	
	private Text filePath;
	private Button selectFilePath;
	
	public ImportLabMapping(Shell parentShell, LabItem act){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		getShell().setText(Messages.ImportLabMapping_shellTitle);
		setTitle(Messages.ImportLabMapping_title);
		setMessage(Messages.ImportLabMapping_message);
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new GridLayout(2, false));
		
		filePath = new Text(ret, SWT.BORDER);
		filePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		selectFilePath = new Button(ret, SWT.PUSH);
		selectFilePath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		selectFilePath.setText(Messages.ImportLabMapping_selectFile); //$NON-NLS-1$
		selectFilePath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				FileDialog fileDialog = new FileDialog(getShell());
				String selected = fileDialog.open();
				filePath.setText(selected);
			}
		});
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath.getText());
		} catch (FileNotFoundException fe) {
			setErrorMessage(String.format(Messages.ImportLabMapping_errorNotFound,
				filePath.getText()));
			return;
		}
		try {
			LabMapping.importMappingFromCsv(fis);
		} catch (IOException ioe) {
			setErrorMessage(String.format(Messages.ImportLabMapping_errorProblems,
				filePath.getText()));
			MessageDialog.openWarning(getShell(), Messages.ImportLabMapping_titleProblemDialog,
				ioe.getMessage());
		}
		
		super.okPressed();
	}
}
