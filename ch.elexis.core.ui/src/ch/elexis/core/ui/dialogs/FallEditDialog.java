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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.views.FallDetailBlatt2;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;

public class FallEditDialog extends TitleAreaDialog {
	Fall fall;
	Patient pat;
	private FallDetailBlatt2 fdb;
	
	public FallEditDialog(Shell shell, Fall f){
		super(shell);
		fall = f;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		fdb = new FallDetailBlatt2(parent);
		fdb.setFall(fall);
		fdb.setUnlocked(true);
		fdb.setLockUpdate(false);
		return fdb;
	}
	
	@Override
	public void create(){
		super.create();
		setMessage(Messages.FallEditDialog_enterCaseData); //$NON-NLS-1$
		setTitle(Messages.FallEditDialog_editCase); //$NON-NLS-1$
		getShell().setText(Messages.FallEditDialog_editCase); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		if (CoreHub.getLocalLockService().acquireLock(fall).isOk()) {
			fdb.save();
			CoreHub.getLocalLockService().releaseLock(fall);
		}
		ElexisEventDispatcher.reload(Fall.class);
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed(){
		super.cancelPressed();
	}
	
	public Fall getFall(){
		return fall;
	}
}
