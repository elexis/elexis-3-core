/*******************************************************************************
 * Copyright (c) 2019 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.internal;

import java.lang.reflect.InvocationTargetException;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.util.IRunnableWithProgress;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.ILoginContributor;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.ErsterMandantDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.SqlWithUiRunner;
import ch.elexis.core.ui.wizards.DBConnectWizard;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.data.Anwender;

@Component
public class CoreOperationAdvisor implements ICoreOperationAdvisor {

	@Reference(target = "(id=login.envvars)")
	private ILoginContributor loginEnv;

	@Reference(target = "(id=login.dialog)")
	private ILoginContributor loginDialog;

	public String initialPerspectiveString;
	private Logger log = LoggerFactory.getLogger(CoreOperationAdvisor.class);

	@Override
	public void requestDatabaseConnectionConfiguration() {
		WizardDialog wd = new WizardDialog(UiDesk.getTopShell(), new DBConnectWizard());
		wd.create();
		SWTHelper.center(wd.getShell());
		wd.open();
		CoreHub.localCfg.flush();
	}

	@Override
	public void requestInitialMandatorConfiguration() {
		Display d = Display.getDefault();
		new ErsterMandantDialog(d.getActiveShell()).open();
	}

	@Override
	public void adaptForUser() {
		if (CoreHub.getLoggedInContact() != null) {
			initialPerspectiveString = CoreHub.localCfg
					.get(CoreHub.getLoggedInContact() + GlobalActions.DEFAULTPERSPECTIVECFG, null);
			boolean fixLayoutChecked = ConfigServiceHolder.getUser(Preferences.USR_FIX_LAYOUT,
					Preferences.USR_FIX_LAYOUT_DEFAULT);
			if (GlobalActions.fixLayoutAction != null) {
				GlobalActions.fixLayoutAction.setChecked(fixLayoutChecked);
			}
		} else {
			if (GlobalActions.fixLayoutAction != null) {
				GlobalActions.fixLayoutAction.setChecked(Preferences.USR_FIX_LAYOUT_DEFAULT);
			}
		}
	}

	@Override
	public String getInitialPerspective() {
		return (initialPerspectiveString == null) ? UiResourceConstants.PatientPerspektive_ID
				: initialPerspectiveString;
	}

	@Override
	public void openInformation(String title, String message) {
		if (isDisplayAvailable() && !CoreUtil.isTestMode()) {
			InfoDialogRunnable runnable = new InfoDialogRunnable(title, message);
			Display.getDefault().syncExec(runnable);
			// dispose possibly created display if workbench is not running
			if (!PlatformUI.isWorkbenchRunning()) {
				Display.getDefault().dispose();
			}
			return;
		}
		log.error("Could not show info [" + title + "] [" + message + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public boolean openQuestion(String title, String message) {
		if (isDisplayAvailable() && !CoreUtil.isTestMode()) {
			QuestionDialogRunnable runnable = new QuestionDialogRunnable(title, message);
			Display.getDefault().syncExec(runnable);
			// dispose possibly created display if workbench is not running
			if (!PlatformUI.isWorkbenchRunning()) {
				Display.getDefault().dispose();
			}
			return runnable.getResult();
		}
		log.error("Could not ask question [" + title + "] [" + message + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return false;
	}

	private class QuestionDialogRunnable implements Runnable {
		private String title;
		private String message;
		private boolean result;

		public QuestionDialogRunnable(String title, String message) {
			this.title = title;
			this.message = message;
		}

		@Override
		public void run() {
			result = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title, message);
		}

		public boolean getResult() {
			return result;
		}
	}

	private class InfoDialogRunnable implements Runnable {
		private String title;
		private String message;

		public InfoDialogRunnable(String title, String message) {
			this.title = title;
			this.message = message;
		}

		@Override
		public void run() {

			MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, message);
		}
	}

	@Override
	public boolean performLogin(Object shell) {

		CoreHub.logoffAnwender();

		// try login env first then show dialog
		IUser user = null;
		try {
			user = loginEnv.performLogin(shell);
		} catch (LoginException le) {
			log.warn("Unable to login with loginService [{}]: {} - skipping", loginEnv.getClass().getName(), //$NON-NLS-1$
					le.getMessage(), le);
		}
		if (user == null) {
			try {
				user = loginDialog.performLogin(shell);
			} catch (LoginException le) {
				log.warn("Unable to login with loginService [{}]: {} - skipping", loginDialog.getClass().getName(), //$NON-NLS-1$
						le.getMessage(), le);
			}
		}

		if (user != null && user.isActive()) {
			// set user in system
			ContextServiceHolder.get().setActiveUser(user);
			ElexisEventDispatcher.getInstance().fire(
					new ElexisEvent(CoreHub.getLoggedInContact(), Anwender.class, ElexisEvent.EVENT_USER_CHANGED));

			CoreOperationAdvisorHolder.get().adaptForUser();
			CoreHub.getLoggedInContact().setInitialMandator();
			CoreHub.heart.resume(true);

			return true;
		}

		return false;
	}

	@Override
	public boolean performDatabaseUpdate(String[] array, String pluginId) {
		return new SqlWithUiRunner(array, pluginId).runSql();
	}

	@Override
	public void showProgress(IRunnableWithProgress irwp, String taskName) {
		try {
			if (isDisplayAvailable()) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						ProgressMonitorDialog pmd = new ProgressMonitorDialog(Display.getDefault().getActiveShell()) {
							@Override
							protected void configureShell(Shell shell) {
								super.configureShell(shell);
								if (taskName != null) {
									shell.setText(taskName);
								}
							}
						};
						org.eclipse.jface.operation.IRunnableWithProgress irpwAdapter = new org.eclipse.jface.operation.IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException, InterruptedException {
								irwp.run(monitor);
							}
						};
						try {
							pmd.run(true, true, irpwAdapter);
						} catch (InvocationTargetException | InterruptedException e) {
							log.error("Execution error", e); //$NON-NLS-1$
						}
					}
				});
			} else {
				irwp.run(new NullProgressMonitor());
			}
		} catch (InvocationTargetException | InterruptedException e) {
			log.error("Execution error", e); //$NON-NLS-1$
		}
	}

	protected boolean isDisplayAvailable() {
		try {
			Class.forName("org.eclipse.swt.widgets.Display"); //$NON-NLS-1$
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
