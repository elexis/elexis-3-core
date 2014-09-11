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
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.AbstractCoreOperationAdvisor;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.ErsterMandantDialog;
import ch.elexis.core.ui.dialogs.LoginDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.SqlWithUiRunner;
import ch.elexis.core.ui.wizards.DBConnectWizard;
import ch.elexis.data.Anwender;

public class CoreOperationAdvisor extends AbstractCoreOperationAdvisor {
	
	public String initialPerspectiveString;
	private Logger log = LoggerFactory.getLogger(CoreOperationAdvisor.class);
	
	@Override
	public void requestDatabaseConnectionConfiguration(){
		WizardDialog wd = new WizardDialog(UiDesk.getTopShell(), new DBConnectWizard());
		wd.create();
		SWTHelper.center(wd.getShell());
		wd.open();
	}
	
	@Override
	public void requestInitialMandatorConfiguration(){
		Display d = Display.getDefault();
		new ErsterMandantDialog(d.getActiveShell()).open();
	}
	
	@Override
	public void adaptForUser(){
		if (CoreHub.actUser != null) {
			initialPerspectiveString =
				CoreHub.localCfg.get(CoreHub.actUser + GlobalActions.DEFAULTPERSPECTIVECFG, null);
		}
		
		UiDesk.updateFont(Preferences.USR_DEFAULTFONT);
		
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(CoreHub.actUser, Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
	}
	
	@Override
	public String getInitialPerspective(){
		return (initialPerspectiveString == null) ? UiResourceConstants.PatientPerspektive_ID
				: initialPerspectiveString;
	}
	
	@Override
	public boolean openQuestion(String title, String message){
		Display d = Display.getDefault();
		return MessageDialog.openQuestion(d.getActiveShell(), title, message);
	}
	
	@Override
	public void performLogin(Object shell){
		String username = System.getProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME);
		String password = System.getProperty(ElexisSystemPropertyConstants.LOGIN_PASSWORD);
		if (username != null && password != null) {
			/*
			 * Allow bypassing the login dialog, eg. for automated GUI-tests. Example: when having a
			 * demoDB you may login directly by passing -vmargs -Dch.elexis.username=test
			 * -Dch.elexis.password=test as command line parameters to elexis.
			 */
			log.error("Bypassing LoginDialog with username " + username);
			if (!Anwender.login(username, password)) {
				log.error("Authentication failed. Exiting");
			}
		} else {
			LoginDialog dlg = new LoginDialog((Shell) shell);
			dlg.create();
			dlg.getShell().setText(Messages.LoginDialog_loginHeader);
			dlg.setTitle(Messages.LoginDialog_notLoggedIn);
			dlg.setMessage(Messages.LoginDialog_enterUsernamePass);
			dlg.open();
		}
	}
	
	@Override
	public boolean performDatabaseUpdate(String[] array, String pluginId){
		return new SqlWithUiRunner(array, pluginId).runSql();
	}
}
