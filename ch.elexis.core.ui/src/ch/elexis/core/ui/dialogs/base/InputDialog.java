/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.dialogs.base;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends org.eclipse.jface.dialogs.InputDialog {
	private final int style;
	
	private final static int HEIGHT_MULTILINE = 100;
	
	/**
	 * Extends the existing jface InputDialog with custom style functionality
	 * 
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogMessage
	 * @param initialValue
	 * @param validator
	 * @param style
	 */
	public InputDialog(Shell parentShell, String dialogTitle, String dialogMessage,
		String initialValue, IInputValidator validator, int style){
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		this.style = style;
	}
	
	@Override
	protected int getInputTextStyle(){
		return style;
	}
	

	@Override
	protected Control createDialogArea(Composite parent){
		Control c = super.createDialogArea(parent);
		
		// multi line
		if ((getInputTextStyle() & SWT.MULTI) != 0) {
			Text text = getText();
			if (text != null) {
				Object layoutData = text.getLayoutData();
				if (layoutData instanceof GridData) {
					((GridData) layoutData).heightHint = HEIGHT_MULTILINE;
				}
			}
		}
		
		return c;
	}
}
