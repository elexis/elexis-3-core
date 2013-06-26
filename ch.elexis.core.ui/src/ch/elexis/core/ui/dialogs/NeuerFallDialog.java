/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
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

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.views.FallDetailBlatt2;

public class NeuerFallDialog extends TitleAreaDialog {
	Fall fall;
	Patient pat;
	
	public NeuerFallDialog(Shell shell, Fall f){
		super(shell);
		fall = f;
		if (fall == null) {
			pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
			fall =
				pat.neuerFall(
					Messages.getString("NeuerFallDialog.0"), Messages.getString("NeuerFallDialog.1"), Messages.getString("NeuerFallDialog.2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ElexisEventDispatcher.fireSelectionEvent(fall);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		FallDetailBlatt2 fdb = new FallDetailBlatt2(parent);
		return fdb;
	}
	
	@Override
	public void create(){
		super.create();
		setMessage(Messages.getString("NeuerFallDialog.enterCaseData")); //$NON-NLS-1$
		setTitle(Messages.getString("NeuerFallDialog.createNewCase")); //$NON-NLS-1$
		getShell().setText(Messages.getString("NeuerFallDialog.newCase")); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		ElexisEventDispatcher.reload(Fall.class);
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed(){
		fall.delete();
		ElexisEventDispatcher.reload(Fall.class);
		super.cancelPressed();
	}
	
}
