/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.controls;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.Patient;

import ch.rgw.tools.StringTool;
public class ClientCustomTextTokenEditDialog extends TitleAreaDialog {
	
	String _token;
	private Label lblTokenlabel;
	private Text txtTokenText;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	private ClientCustomTextTokenEditDialog(Shell parentShell){
		super(parentShell);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ClientCustomTextTokenEditDialog(Shell parentShell, String token){
		super(parentShell);
		_token = token;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle("Edit value ...");
		String name = _token.split("\\.")[1];
		setMessage("Edit " + name + " value");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		lblTokenlabel = new Label(container, SWT.NONE);
		lblTokenlabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTokenlabel.setText(_token.split("\\.")[1]);
		
		txtTokenText = new Text(container, SWT.BORDER);
		txtTokenText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		String[] arr = _token.split("\\.");
		if (arr == null || arr.length < 2) {
			txtTokenText.setText("ERR");
			return area;
		}

		if (arr[0].equalsIgnoreCase("Patient")) {
			Patient pat = ElexisEventDispatcher.getSelectedPatient();
			
			if (arr[1] == null || pat == null) {
				txtTokenText.setText(StringTool.leer);
				return area;
			}

			String result = pat.get(arr[1]);
			txtTokenText.setText((result != null) ? result : StringTool.leer);
		} 
		
		return area;
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed(){
		Patient pat = ElexisEventDispatcher.getSelectedPatient();
		if(pat!=null) {
			pat.set(_token.split("\\.")[1], txtTokenText.getText());
		}
		super.okPressed();
	}
}
