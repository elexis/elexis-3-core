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

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.ui.views.FallDetailBlatt2;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;

public class NeuerFallDialog extends TitleAreaDialog {
	Fall fall;
	Patient pat;
	private FallDetailBlatt2 fdb;
	private final boolean deleteOnCancel;

	public NeuerFallDialog(Shell shell, Fall f) {
		super(shell);
		fall = f;
		if (fall == null) {
			pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
			fall = pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(), Fall.getDefaultCaseLaw());
		}
		deleteOnCancel = true;
	}

	public NeuerFallDialog(Shell shell, Patient pat, boolean deleteOnCancel) {
		super(shell);
		this.pat = pat;
		this.fall = pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(), Fall.getDefaultCaseLaw());
		this.deleteOnCancel = deleteOnCancel;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		fdb = new FallDetailBlatt2(parent);
		fdb.setFall(fall);
		fdb.setUnlocked(true);
		fdb.setLockUpdate(false);
		return fdb;
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.NeuerFallDialog_enterCaseData); // $NON-NLS-1$
		setTitle(Messages.NeuerFallDialog_createNewCase); // $NON-NLS-1$
		getShell().setText(Messages.NeuerFallDialog_newCase); // $NON-NLS-1$
	}

	@Override
	protected void okPressed() {
		if (LocalLockServiceHolder.get().acquireLock(fall).isOk()) {
			fdb.save();
			LocalLockServiceHolder.get().releaseLock(fall);
		}
		ElexisEventDispatcher.update(fall);
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		if (deleteOnCancel) {
			fall.delete();
		}
		ElexisEventDispatcher.reload(Fall.class);
		super.cancelPressed();
	}

	public Fall getFall() {
		return fall;
	}

}
