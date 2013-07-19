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
package ch.elexis.importers.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class QueryOverwriteDialog extends Dialog {
	
	private String _title;
	private String _message;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public QueryOverwriteDialog(Shell parentShell, final String title, final String message){
		super(parentShell);
		_title = title;
		_message = message;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		Label lblMessage = new Label(container, SWT.NONE);
		lblMessage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		lblMessage.setText(_message);
		
		return container;
	}
	
	@Override
	protected void configureShell(Shell newShell){
		super.configureShell(newShell);
		newShell.setText(_title);
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		Button buttonNo =
			createButton(parent, IDialogConstants.NO_ID, IDialogConstants.OK_LABEL, true);
		buttonNo.setText(Messages.QueryOverwriteDialog_NO);
		Button buttonYesToAll =
			createButton(parent, IDialogConstants.YES_TO_ALL_ID, IDialogConstants.CANCEL_LABEL,
				false);
		buttonYesToAll.setText(Messages.QueryOverwriteDialog_YESTOALL);
		Button buttonYes =
			createButton(parent, IDialogConstants.YES_ID, IDialogConstants.CANCEL_LABEL, false);
		buttonYes.setText(Messages.QueryOverwriteDialog_YES);
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		setReturnCode(buttonId);
		close();
	}
	
}
