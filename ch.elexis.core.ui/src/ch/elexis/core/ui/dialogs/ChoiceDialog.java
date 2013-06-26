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
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.util.SWTHelper;

public class ChoiceDialog extends Dialog {
	String title;
	String message;
	String[] choices;
	Button[] buttons;
	int result = -1;
	
	public ChoiceDialog(Shell shell, String title, String message, String[] choices){
		super(shell);
		this.title = title;
		this.choices = choices;
		this.message = message;
		buttons = new Button[choices.length];
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		Label msg = new Label(ret, SWT.NONE);
		msg.setText(message);
		for (int i = 0; i < choices.length; i++) {
			buttons[i] = new Button(ret, SWT.RADIO);
			buttons[i].setText(choices[i]);
			buttons[i].setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		}
		return ret;
	}
	
	@Override
	protected void okPressed(){
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].getSelection()) {
				result = i;
				break;
			}
		}
		super.okPressed();
	}
	
	public int getResult(){
		return result;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(title);
	}
	
}
