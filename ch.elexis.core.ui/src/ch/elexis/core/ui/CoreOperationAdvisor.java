/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.AbstractCoreOperationAdvisor;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.dialogs.ErsterMandantDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.wizards.DBConnectWizard;

public class CoreOperationAdvisor extends AbstractCoreOperationAdvisor {

	@Override
	public void requestDatabaseConnectionConfiguration() {
		WizardDialog wd = new WizardDialog(Hub.getActiveShell(),
				new DBConnectWizard());
		wd.create();
		SWTHelper.center(wd.getShell());
		wd.open();
	}

	@Override
	public void requestInitialMandatorConfiguration() {
		Display d = Display.getDefault();
		new ErsterMandantDialog(d.getActiveShell()).open();
	}

	@Override
	public void adaptForUser() {
		String perspektive = CoreHub.localCfg.get(CoreHub.actUser
				+ GlobalActions.DEFAULTPERSPECTIVECFG, null);
		if (perspektive == null) {
			perspektive = UiResourceConstants.PatientPerspektive_ID;
		}
		try {
			UiDesk.updateFont(Preferences.USR_DEFAULTFONT);
			IWorkbenchWindow win = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			PlatformUI.getWorkbench().showPerspective(perspektive, win);
			ElexisEventDispatcher.getInstance().fire(
					new ElexisEvent(CoreHub.actUser, Anwender.class,
							ElexisEvent.EVENT_USER_CHANGED));
		} catch (Exception ex) {
			MessageEvent.fireLoggedError("Perspektive nicht gefunden",
					"Konnte die eingestellte Startperspektive " + perspektive
							+ " nicht laden.", ex);
		}
	}

	@Override
	public boolean openQuestion(String title, String message) {
		Display d = Display.getDefault();
		return MessageDialog.openQuestion(d.getActiveShell(), title, message);
	}
}
