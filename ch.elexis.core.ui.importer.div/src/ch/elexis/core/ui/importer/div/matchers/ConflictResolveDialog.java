/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.importer.div.matchers;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.Kontakt;

public class ConflictResolveDialog extends TitleAreaDialog {
	private Kontakt res;
	private Kontakt mine;
	
	public ConflictResolveDialog(Shell shell, Kontakt k){
		super(shell);
		mine = k;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		Label lTell = new Label(ret, SWT.WRAP);
		lTell.setText(resolve1);
		
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.ConflictResolveDialog_ImportCaption);
		setMessage(mine.getLabel());
	}
	
	@Override
	protected void okPressed(){
		// TODO Auto-generated method stub
		super.okPressed();
	}
	
	final static String resolve1 = Messages.ConflictResolveDialog_CannotDecideAutomatically
		+ Messages.ConflictResolveDialog_whethercontainedinDatavase
		+ Messages.ConflictResolveDialog_Pleaseselectbelow
		+ Messages.ConflictResolveDialog_OrCreateNew;
	
	public Kontakt getResult(){
		return res;
	}
}
