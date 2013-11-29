/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.Kontakt;

/**
 * Dialog to enter / edit XID's
 * 
 * @author gerry
 * 
 */
public class XidEditDialog extends TitleAreaDialog {
	
	public XidEditDialog(Shell parentShell, Kontakt k){
		super(parentShell);
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		// TODO Auto-generated method stub
		return super.createDialogArea(parent);
	}
	
	@Override
	public void create(){
		// TODO Auto-generated method stub
		super.create();
	}
	
}
