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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.extension.AbstractCoreOperationAdvisor;
import ch.elexis.core.data.util.IRunnableWithProgress;
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
		CoreHub.localCfg.flush();
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
	}
	
	@Override
	public String getInitialPerspective(){
		return (initialPerspectiveString == null) ? UiResourceConstants.PatientPerspektive_ID
				: initialPerspectiveString;
	}
	
	@Override
	public void openInformation(String title, String message){
		if (isDisplayAvailable()) {
			InfoDialogRunnable runnable = new InfoDialogRunnable(title, message);
			Display.getDefault().syncExec(runnable);
			return;
		}
		log.error("Could not show info [" + title + "] [" + message + "]");
	}
	
	@Override
	public boolean openQuestion(String title, String message){
		if (isDisplayAvailable()) {
			QuestionDialogRunnable runnable = new QuestionDialogRunnable(title, message);
			Display.getDefault().syncExec(runnable);
			return runnable.getResult();
		}
		log.error("Could not ask question [" + title + "] [" + message + "]");
		return false;
	}
	
	private class QuestionDialogRunnable implements Runnable {
		private String title;
		private String message;
		private boolean result;
		
		public QuestionDialogRunnable(String title, String message){
			this.title = title;
			this.message = message;
		}
		
		@Override
		public void run(){
			result =
				MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title, message);
		}
		
		public boolean getResult(){
			return result;
		}
	}
	
	private class InfoDialogRunnable implements Runnable {
		private String title;
		private String message;
		
		public InfoDialogRunnable(String title, String message){
			this.title = title;
			this.message = message;
		}
		
		@Override
		public void run(){
			
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, message);
		}
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
	
	@Override
	public void showProgress(IRunnableWithProgress irwp, String taskName){
		try {
			if (isDisplayAvailable()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run(){
						ProgressMonitorDialog pmd =
							new ProgressMonitorDialog(Display.getDefault().getActiveShell()) {
								@Override
								protected void configureShell(Shell shell){
									super.configureShell(shell);
									if (taskName != null) {
										shell.setText(taskName);
									}
								}
							};
						org.eclipse.jface.operation.IRunnableWithProgress irpwAdapter =
							new org.eclipse.jface.operation.IRunnableWithProgress() {
								
								@Override
								public void run(IProgressMonitor monitor)
									throws InvocationTargetException, InterruptedException{
									irwp.run(monitor);
								}
							};
						try {
							pmd.run(true, true, irpwAdapter);
						} catch (InvocationTargetException | InterruptedException e) {
							log.error("Execution error", e);
						}
					}
				});
			} else {
				irwp.run(new NullProgressMonitor());
			}
		} catch (InvocationTargetException | InterruptedException e) {
			log.error("Execution error", e);
		}
	}
	
	protected boolean isDisplayAvailable(){
		try {
			Class.forName("org.eclipse.swt.widgets.Display");
		} catch (ClassNotFoundException e) {
			return false;
		} catch (NoClassDefFoundError e) {
			return false;
		}
		if (Display.getDefault() == null)
			return false;
		else
			return true;
	}
}
