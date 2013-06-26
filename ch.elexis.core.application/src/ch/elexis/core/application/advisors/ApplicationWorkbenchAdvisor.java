/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    N. Giger - Bypass Login Dialog for development environments
 *    
 *******************************************************************************/

package ch.elexis.core.application.advisors;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Reminder;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.Messages;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.dialogs.LoginDialog;
import ch.elexis.core.ui.perspectives.PatientPerspektive;
import ch.rgw.tools.ExHandler;

/**
 * Dies ist eine Eclipse-spezifische Klasse Wichtigste Funktion ist das Festlegen der initialen
 * Perspektive In eventloopException können spezifische Verarbeitungen für nicht abgefangene
 * Exceptions definiert werden (Hier einfach Ausgabe). In eventLoopIdle können Arbeiten eingetragen
 * werden, die immer dann zu eredigen sind, wenn das Programm nichts weiter zu tun hat.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	
	private static final String PERSPECTIVE_ID = PatientPerspektive.ID;
	private Logger log = LoggerFactory.getLogger(ApplicationWorkbenchAdvisor.class.getName());
	
	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
		final IWorkbenchWindowConfigurer configurer){
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.
	 * IWorkbenchConfigurer)
	 */
	@Override
	public void initialize(final IWorkbenchConfigurer configurer){
		Hub.pin.initializeDisplayPreferences(UiDesk.getDisplay());
		configurer.setSaveAndRestore(true);
		super.initialize(configurer);
	}
	
	@Override
	public void preStartup() {
		System.out.println("Reaching preStartup()");
		super.preStartup();
	}
	
	@Override
	public IAdaptable getDefaultPageInput() {
		// TODO Auto-generated method stub
		return super.getDefaultPageInput();
	}
	
	@Override
	public void postStartup(){
		Shell shell = getWorkbenchConfigurer().getWorkbench().getActiveWorkbenchWindow().getShell();
		String username = System.getProperty("ch.elexis.username");
		String password = System.getProperty("ch.elexis.password");
		if (username != null && password != null) {
			log.error("Bypassing LoginDialog with username " + username);
			if (!Anwender.login(username, password)) {
				log.error("Authentication failed. Exiting");
			}
		} else {
			LoginDialog dlg = new LoginDialog(shell);
			dlg.create();
			dlg.getShell().setText(Messages.ApplicationWorkbenchAdvisor_7);
			dlg.setTitle(Messages.ApplicationWorkbenchAdvisor_8);
			dlg.setMessage(Messages.ApplicationWorkbenchAdvisor_9);
			dlg.open();
		}
		
		// check if there is a valid user
		if ((CoreHub.actUser == null) || !CoreHub.actUser.isValid()) {
			// no valid user, exit (don't consider this as an error)
			log.warn("Exit because no valid user logged-in"); //$NON-NLS-1$
			PersistentObject.disconnect();
			System.exit(0);
		}
		
		List<Reminder> reminderList = Reminder.findToShowOnStartup(CoreHub.actUser);
		if (reminderList.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Reminder reminder : reminderList) {
				sb.append(reminder.getMessage()).append("\n\n"); //$NON-NLS-1$		
			}
			
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell(),
				ch.elexis.core.ui.views.Messages.getString("ReminderView.importantRemindersOnLogin"),
				sb.toString());
		}
		
	}
	
	@Override
	public String getInitialWindowPerspectiveId(){
		return PERSPECTIVE_ID;
	}
	
	@Override
	public void eventLoopException(final Throwable exception){
		log.error(Messages.ApplicationWorkbenchAdvisor_10 + exception.getMessage());
		exception.printStackTrace();
		ExHandler.handle(exception);
		super.eventLoopException(exception);
	}
	
	@Override
	public boolean preShutdown(){
		GlobalActions.fixLayoutAction.setChecked(false);
		return super.preShutdown();
	}
	
	@Override
	public void postShutdown(){
		Hub.postShutdown();
		super.postShutdown();
	}
}
