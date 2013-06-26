/*******************************************************************************
 * Copyright (c) 2006-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - Ticket 1378
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.SWTHelper;

public class SelectFallDialog extends TitleAreaDialog {
	Fall[] faelle;
	public Fall result;
	List list;
	private UpdateFallListListener updateFallListener =
		new UpdateFallListListener(Fall.class, 0xff);
	
	public SelectFallDialog(Shell shell){
		super(shell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.None);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		GridLayout gl_ret = new GridLayout(1, false);
		gl_ret.marginWidth = 0;
		gl_ret.marginHeight = 0;
		ret.setLayout(gl_ret);
		
		list = new List(ret, SWT.BORDER);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		reloadFaelleList();
		
		ToolBarManager tbManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		tbManager.add(GlobalActions.neuerFallAction);
		tbManager.createControl(ret);
		
		ElexisEventDispatcher.getInstance().addListeners(updateFallListener);
		
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.getString("SelectFallDialog.selectFall")); //$NON-NLS-1$
		setMessage(Messages.getString("SelectFallDialog.pleaseSelectCase")); //$NON-NLS-1$
		getShell().setText(Messages.getString("SelectFallDialog.Cases")); //$NON-NLS-1$
	}
	
	@Override
	public void okPressed(){
		int sel = list.getSelectionIndex();
		if (sel == -1) {
			result = null;
		} else {
			result = faelle[sel];
		}
		
		ElexisEventDispatcher.getInstance().removeListeners(updateFallListener);
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed(){
		ElexisEventDispatcher.getInstance().removeListeners(updateFallListener);
		super.cancelPressed();
	}
	
	private void reloadFaelleList(){
		list.removeAll();
		faelle = ElexisEventDispatcher.getSelectedPatient().getFaelle();
		for (Fall f : faelle) {
			list.add(f.getLabel());
		}
	}
	
	private class UpdateFallListListener extends ElexisUiEventListenerImpl {
		
		UpdateFallListListener(final Class<?> clazz, int mode){
			super(clazz, mode);
		}
		
		@Override
		public void runInUi(ElexisEvent ev){
			reloadFaelleList();
		}
	}
	
}