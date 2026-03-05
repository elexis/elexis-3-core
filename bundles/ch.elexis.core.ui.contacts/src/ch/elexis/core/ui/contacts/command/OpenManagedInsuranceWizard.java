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
package ch.elexis.core.ui.contacts.command;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.contacts.wizard.ManagedInsuranceWizard;

public class OpenManagedInsuranceWizard extends AbstractHandler {

	private ManagedInsuranceWizard wizard;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShellChecked(event),
				getManagedInsuranceWizard(HandlerUtil.getActiveShell(event))) {
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				super.createButtonsForButtonBar(parent);
				getButton(IDialogConstants.CANCEL_ID).setText("Beenden");
			}
		};
		dialog.open();
		return null;
	}

	private IWizard getManagedInsuranceWizard(Shell activeshell) {

		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(activeshell);
		try {
			progressDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(
							"Organisationen und Versicherungen werden geladen.",
							IProgressMonitor.UNKNOWN);
					wizard = new ManagedInsuranceWizard();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			MessageDialog.openError(activeshell, "Organisationen und Versicherungen",
					"Organisationen und Versicherungen konnte nicht gestartet werden.");
			LoggerFactory.getLogger(getClass()).error("Error open managed insurance wizard", e);
		}
		return wizard;
	}
}
