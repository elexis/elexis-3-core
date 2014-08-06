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
package ch.elexis.scripting;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.rgw.tools.StringTool;

public class ScriptEditor extends TitleAreaDialog {
	String script;
	String title;
	Text text;
	
	public ScriptEditor(Shell shell, String vorgabe, String titel){
		super(shell);
		script = vorgabe;
		title = titel;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		GridData full =
			new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		ret.setLayoutData(full);
		ret.setLayout(new FillLayout());
		text = new Text(ret, SWT.MULTI | SWT.BORDER);
		text.setText(StringTool.unNull(script));
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.ScriptEditor_editScript);
		setMessage(title);
		getShell().setText(Messages.ScriptEditor_ScriptTitle);
	}
	
	@Override
	protected void okPressed(){
		script = text.getText();
		super.okPressed();
	}
	
	public String getScript(){
		return script;
	}
}
