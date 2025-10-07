/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_KG_COVER_SHEET;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_XRAY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.ac.SystemCommandConstants;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.InvoiceServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.constants.UiPreferenceConstants;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.dialogs.NeuerFallDialog;
import ch.elexis.core.ui.dialogs.ResultDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockedAction;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.TemplateDrucker;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.TemplatePrintView;
import ch.elexis.core.ui.wizards.DBConnectWizard;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Diese Klasse definiert alle statischen Actions, die global gelten sollen.
 */
public class GlobalActions {
	// globally used command ids (for key bindings / actions)
	public static final String RENAME_COMMAND = "org.eclipse.ui.edit.rename"; //$NON-NLS-1$
	public static final String DELETE_COMMAND = "org.eclipse.ui.edit.delete"; //$NON-NLS-1$
	public static final String PROPERTIES_COMMAND = "org.eclipse.ui.file.properties"; //$NON-NLS-1$
	public static final String DEFAULTPERSPECTIVECFG = "/default_perspective"; //$NON-NLS-1$

	public static IWorkbenchAction exitAction, newWindowAction, copyAction, cutAction, pasteAction;
	public static IAction loginAction, importAction, aboutAction, helpAction, prefsAction;
	public static IAction connectWizardAction, savePerspectiveAction, savePerspectiveAsAction;
	public static IAction savePerspectiveAsDefaultAction, resetPerspectiveAction, homeAction, fixLayoutAction;
	public static IAction printEtikette, printBlatt, printAdresse, printVersionedEtikette, showBlatt;
	public static IAction printRoeBlatt;
	public static IAction openFallaction, closeFallAction, filterAction, makeBillAction, planeRechnungAction;
	public static RestrictedAction delKonsAction, delFallAction, reopenFallAction;
	public static LockedRestrictedAction<IEncounter> moveBehandlungAction, redateAction;
	public static IAction neuerFallAction;

	public static MenuManager perspectiveMenu, viewMenu;
	public static IContributionItem viewList;
	public IWorkbenchWindow mainWindow;
	public static Action printKontaktEtikette;
	private static IWorkbenchHelpSystem help;
	private static Logger logger;
	private static ICommandService cmdService;

	/**
	 * Open the preferences dialog. This a copy of the same internal eclipse where
	 * we just want to have a large dialog action
	 */
	private class OpenPreferencesAction extends Action implements ActionFactory.IWorkbenchAction {

		/**
		 * The workbench window; or <code>null</code> if this action has been
		 * <code>dispose</code>d.
		 */
		private IWorkbenchWindow workbenchWindow;

		/**
		 * Create a new <code>OpenPreferenceAction</code> This default constructor
		 * allows the the action to be called from the welcome page.
		 */
		public OpenPreferencesAction() {
			this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		}

		/**
		 * Create a new <code>OpenPreferenceAction</code> and initialize it from the
		 * given resource bundle.
		 *
		 * @param window
		 */
		public OpenPreferencesAction(IWorkbenchWindow window) {
			super(WorkbenchMessages.OpenPreferences_text);
			if (window == null) {
				throw new IllegalArgumentException();
			}
			this.workbenchWindow = window;
			// @issue action id not set
			setToolTipText(WorkbenchMessages.OpenPreferences_toolTip);
			window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.OPEN_PREFERENCES_ACTION);
		}

		@Override
		public void run() {
			if (workbenchWindow == null) {
				// action has been dispose
				return;
			}
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, null, null, null);
			dialog.getShell().setSize(1240, 700); // This enlarges the preference page
			dialog.open();
		}

		@Override
		public String getActionDefinitionId() {
			return IWorkbenchCommandConstants.WINDOW_PREFERENCES;
		}

		@Override
		public void dispose() {
			workbenchWindow = null;
		}
	};

	public GlobalActions(final IWorkbenchWindow window) {
		if (Hub.mainActions != null) {
			return;
		}
		logger = LoggerFactory.getLogger(this.getClass());
		cmdService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(ICommandService.class);
		mainWindow = window;
		help = Hub.plugin.getWorkbench().getHelpSystem();
		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setText(Messages.GlobalActions_MenuExit);
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		newWindowAction.setText(Messages.GlobalActions_NewWindow);
		copyAction = ActionFactory.COPY.create(window);
		copyAction.setText(Messages.GlobalActions_Copy);
		cutAction = ActionFactory.CUT.create(window);
		cutAction.setText(Messages.GlobalActions_Cut);
		pasteAction = ActionFactory.PASTE.create(window);
		pasteAction.setText(Messages.GlobalActions_Paste);
		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setText(Messages.GlobalActions_MenuAbout);
		prefsAction = new OpenPreferencesAction(window);
		prefsAction.setText(Messages.GlobalActions_Preferences);
		savePerspectiveAction = new Action(Messages.GlobalActions_SavePerspective) {
			{
				setId("savePerspektive"); //$NON-NLS-1$
				setToolTipText(Messages.GlobalActions_SavePerspectiveToolTip);
				setImageDescriptor(Images.IMG_DISK.getImageDescriptor());
			}

			@Override
			public void run() {
				IWorkbenchPage page = mainWindow.getActivePage();
				if (page != null && page.getPerspective() != null) {
					page.savePerspectiveAs(page.getPerspective());
				}
			}
		};

		helpAction = new Action(Messages.GlobalActions_ac_handbook) {
			{
				setImageDescriptor(Images.IMG_BOOK.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_ac_openhandbook);

			}

			@Override
			public void run() {
				String url = ConfigServiceHolder.getGlobal(UiPreferenceConstants.CFG_HANDBOOK,
						UiPreferenceConstants.DEFAULT_HANDBOOK);
				try {
					Program.launch(url);
				} catch (Exception e) {
					logger.warn("failed to open default browser :" + e); //$NON-NLS-1$
					MessageDialog.openError(mainWindow.getShell(), Messages.Core_Error,
							Messages.GlobalActions_PreferencesHandbook_URL);
					ExHandler.handle(e);
				}
			}
		};
		savePerspectiveAsAction = ActionFactory.SAVE_PERSPECTIVE.create(window);

		// ActionFactory.SAVE_PERSPECTIVE.create(window);
		resetPerspectiveAction = new Action(Messages.GlobalActions_Home) {

			@Override
			public void run() {
				// run access control reset
				ContextServiceHolder.get().sendEvent("info/elexis/ui/accesscontrol/reset",
						ContextServiceHolder.get().getActiveUser().orElse(null));

				EModelService modelService = PlatformUI.getWorkbench().getService(EModelService.class);

				removeModelOfParts(getActivePerspective(modelService), modelService);

				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();

				updateModelOfParts(getActivePerspective(modelService), modelService);

				// update fixedLayout
				boolean value = ConfigServiceHolder.getUser(Preferences.USR_FIX_LAYOUT, false);
				ContextServiceHolder.get().getRootContext().setNamed(Preferences.USR_FIX_LAYOUT, !value);
				ContextServiceHolder.get().getRootContext().setNamed(Preferences.USR_FIX_LAYOUT, value);

				// run access control after perspective reset
				ContextServiceHolder.get().sendEvent("info/elexis/ui/accesscontrol/update",
						getActivePerspective(modelService));
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_REFRESH.getImageDescriptor();
			}
		};

		homeAction = new Action(Messages.GlobalActions_Home) {
			{
				setId("home"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "home"); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_HOME.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_HomeToolTip);
				help.setHelp(this, "ch.elexis.globalactions.homeAction"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				// String
				String perspektive = CoreHub.localCfg.get(CoreHub.getLoggedInContact() + DEFAULTPERSPECTIVECFG, null);
				if (StringTool.isNothing(perspektive)) {
					perspektive = UiResourceConstants.PatientPerspektive_ID;
				}
				try {
					EPartService partService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getService(EPartService.class);
					GlobalActions.addModelOfParts(partService);

					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					PlatformUI.getWorkbench().showPerspective(perspektive, win);
					// Hub.heart.resume(true);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		};
		savePerspectiveAsDefaultAction = new Action(Messages.GlobalActions_saveasstartperspective) {
			{
				setId("start"); //$NON-NLS-1$
				// setActionDefinitionId(Hub.COMMAND_PREFIX+"startPerspective");
			}

			@Override
			public void run() {
				IPerspectiveDescriptor p = mainWindow.getActivePage().getPerspective();
				CoreHub.localCfg.set(CoreHub.getLoggedInContact() + DEFAULTPERSPECTIVECFG, p.getId());
			}

		};
		loginAction = new RestrictedAction(EvACE.of(SystemCommandConstants.LOGIN_UI), Messages.GlobalActions_Login) {
			{
				setId("login"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "login"); //$NON-NLS-1$
			}

			@Override
			public void doRun() {
				try {
					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
					for (IWorkbenchWindow w : wins) {
						if (!w.equals(win)) {
							w.close();
						}
					}

					// make sure env login is not used
					String loginUserName = System.getProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME);
					System.clearProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME);
					boolean performLogin = CoreOperationAdvisorHolder.get().performLogin(win.getShell());
					// reset after login
					if (loginUserName != null) {
						System.setProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME, loginUserName);
					}
					if (!performLogin) {
						exitAction.run();
					}
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		};
		importAction = new Action(Messages.GlobalActions_Import) {
			{
				setId("import"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "import"); //$NON-NLS-1$
			}

			@Override
			public void run() {
				// cnv.open();
				Importer imp = new Importer(mainWindow.getShell(), ExtensionPointConstantsUi.FREMDDATENIMPORT);
				imp.create();
				imp.setMessage(Messages.Core_Choose_Import_Source);
				imp.getShell().setText(Messages.Core_Importer);
				imp.setTitle(Messages.GlobalActions_ImportDlgTitle);
				imp.open();
			}
		};

		connectWizardAction = new RestrictedAction(EvACE.of("ACL_DBCONNECTION_CONFIG"),
				Messages.GlobalActions_Connection) {
			{
				setId("connectWizard"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "connectWizard"); //$NON-NLS-1$
			}

			@Override
			public void doRun() {
				WizardDialog wd = new WizardDialog(mainWindow.getShell(), new DBConnectWizard());
				wd.open();
			}
		};

		printKontaktEtikette = new Action(Messages.Print_AddressLabel) {
			{
				setToolTipText(Messages.GlobalActions_PrintContactLabelToolTip);
				setImageDescriptor(Images.IMG_ADRESSETIKETTE.getImageDescriptor());
			}

			@Override
			public void run() {
				Command cmd = cmdService.getCommand("ch.elexis.core.ui.commands.printContactLabel"); //$NON-NLS-1$

				try {
					cmd.executeWithChecks(new ExecutionEvent());
				} catch (Exception e) {
					ExHandler.handle(e);
					logger.error("Failed to execute command ch.elexis.core.ui.commands.printContactLabel", e); //$NON-NLS-1$
				}
			}
		};

		printAdresse = new Action(Messages.Print_AddressLabel) {
			{
				setImageDescriptor(Images.IMG_ADRESSETIKETTE.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_PrintAddressLabelToolTip);
			}

			@Override
			public void run() {
				Command cmd = cmdService.getCommand("ch.elexis.core.ui.commands.printAddressLabel"); //$NON-NLS-1$

				try {
					cmd.executeWithChecks(new ExecutionEvent());
				} catch (Exception e) {
					ExHandler.handle(e);
					logger.error("Failed to execute command ch.elexis.core.ui.commands.printAddressLabel", e); //$NON-NLS-1$
				}
			}
		};

		printVersionedEtikette = new Action(Messages.Core_Print_versioned_Label) {
			{
				setToolTipText(Messages.GlobalActions_PrintVersionedLabelToolTip);
				setImageDescriptor(Images.IMG_VERSIONEDETIKETTE.getImageDescriptor());
			}

			@Override
			public void run() {
				Command cmd = cmdService.getCommand("ch.elexis.core.ui.commands.printVersionedLabel"); //$NON-NLS-1$

				try {
					cmd.executeWithChecks(new ExecutionEvent());
				} catch (Exception e) {
					ExHandler.handle(e);
					logger.error("Failed to execute command ch.elexis.core.ui.commands.printVersionedLabel", e); //$NON-NLS-1$
				}
			}
		};

		printEtikette = new Action(Messages.Core_Print_Patient_Label) {
			{
				setImageDescriptor(Images.IMG_PATIENTETIKETTE.getImageDescriptor());
				setToolTipText(Messages.Core_Print_Patient_Label);
			}

			@Override
			public void run() {
				Command cmd = cmdService.getCommand("ch.elexis.core.ui.commands.printPatientLabel"); //$NON-NLS-1$

				try {
					cmd.executeWithChecks(new ExecutionEvent());
				} catch (Exception e) {
					ExHandler.handle(e);
					logger.error("Failed to execute command ch.elexis.core.ui.commands.printPatientLabel", e); //$NON-NLS-1$
				}
			}
		};

		printBlatt = new Action(Messages.GlobalActions_PrintEMR) {
			@Override
			public void run() {
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				String printer = CoreHub.localCfg.get("Drucker/Einzelblatt/Name", null); //$NON-NLS-1$
				String tray = CoreHub.localCfg.get("Drucker/Einzelblatt/Schacht", null); //$NON-NLS-1$

				new TemplateDrucker(TT_KG_COVER_SHEET, printer, tray).doPrint(actPatient); // $NON-NLS-1$
			}
		};
		showBlatt = new Action(Messages.GlobalActions_ShowEMR) {
			@Override
			public void run() {
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				try {
					TemplatePrintView tpw = (TemplatePrintView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(TemplatePrintView.ID);
					tpw.doShow(actPatient, TT_KG_COVER_SHEET);
				} catch (PartInitException e) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Fehler",
							"Konnte View nicht öffnen");
					LoggerFactory.getLogger(getClass()).error("Error showing " + TemplatePrintView.ID, e); //$NON-NLS-1$
				}
			}
		};
		printRoeBlatt = new Action(Messages.GlobalActions_PrintXRay) {
			@Override
			public void run() {
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				String printer = CoreHub.localCfg.get("Drucker/A4/Name", null); //$NON-NLS-1$
				String tray = CoreHub.localCfg.get("Drucker/A4/Schacht", null); //$NON-NLS-1$

				new TemplateDrucker(TT_XRAY, printer, tray).doPrint(actPatient); // $NON-NLS-1$
			}
		};

		fixLayoutAction = new Action(Messages.GlobalActions_LockPerspectives, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.GlobalActions_LockPerspectivesToolTip);
				boolean state = ConfigServiceHolder.getUser(Preferences.USR_FIX_LAYOUT, false);
				setChecked(state);
				ContextServiceHolder.get().getRootContext().setNamed(Preferences.USR_FIX_LAYOUT, state);
			}

			@Override
			public void run() {
				// store the current value in the user's configuration
				boolean state = fixLayoutAction.isChecked();
				ConfigServiceHolder.setUser(Preferences.USR_FIX_LAYOUT, state);
				// update a named variable to trigger update of the ui
				ContextServiceHolder.get().getRootContext().setNamed(Preferences.USR_FIX_LAYOUT, state);
			}
		};
		makeBillAction = new Action(Messages.GlobalActions_MakeBill) {
			@Override
			public void run() {
				ICoverage actFall = ContextServiceHolder.get().getActiveCoverage().orElse(null);
				IMandator mnd = ContextServiceHolder.get().getActiveMandator().orElse(null);
				if (actFall != null && mnd != null) {
					String rsId = mnd.getBiller().getId();
					List<IEncounter> bhdl = actFall.getEncounters();
					List<IEncounter> lBehdl = new ArrayList<>();
					for (IEncounter b : bhdl) {
						IInvoice rn = b.getInvoice();
						if (rn == null) {
							if (b.getMandator().getBiller().getId().equals(rsId)) {
								lBehdl.add(b);
							}
						}
					}
					Map<Integer, List<IEncounter>> sortedByYears = BillingUtil.getSortedEncountersByYear(lBehdl);
					if (!BillingUtil.canBillYears(new ArrayList<>(sortedByYears.keySet()))) {
						StringJoiner sj = new StringJoiner(", "); //$NON-NLS-1$
						sortedByYears.keySet().forEach(i -> sj.add(Integer.toString(i)));
						if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Rechnung Validierung",
								"Die Leistungen sind aus Jahren die nicht kombinierbar sind.\n\nWollen Sie separate Rechnungen für die Jahre "
										+ sj.toString() + " erstellen?")) {
							// bill each year separately
							for (Integer year : sortedByYears.keySet()) {
								Result<IInvoice> res = InvoiceServiceHolder.get().invoice(sortedByYears.get(year));
								if (!res.isOK()) {
									ErrorDialog.openError(mainWindow.getShell(), Messages.Core_Error,
											Messages.GlobalActions_BillErrorMessage,
											ResultAdapter.getResultAsStatus(res));
								}
							}
						}
					} else {
						Result<IInvoice> res = InvoiceServiceHolder.get().invoice(lBehdl);
						if (!res.isOK()) {
							ErrorDialog.openError(mainWindow.getShell(), Messages.Core_Error,
									Messages.GlobalActions_BillErrorMessage, ResultAdapter.getResultAsStatus(res));
						}
					}
				}
			}
		};
		moveBehandlungAction = new LockedRestrictedAction<IEncounter>(EvACE.of(IEncounter.class, Right.UPDATE),
				Messages.GlobalActions_AssignCase) {
			@Override
			public IEncounter getTargetedObject() {
				return ContextServiceHolder.get().getTyped(IEncounter.class).orElse(null);
			}

			@Override
			public void doRun(IEncounter element) {
				// TODO do we need to lock the fall?
				SelectFallDialog dlg = new SelectFallDialog(mainWindow.getShell());
				if (dlg.open() == Dialog.OK) {
					Fall f = dlg.result;
					if (f != null) {
						ICoverage coverage = NoPoUtil.loadAsIdentifiable(f, ICoverage.class).get();
						Result<IEncounter> result = EncounterServiceHolder.get().transferToCoverage(element, coverage,
								false);
						if (!result.isOK()) {
							SWTHelper.alert("Warnung", result.toString());
						}
						ContextServiceHolder.get().setActiveCoverage(coverage);
					}
				}
			}
		};
		redateAction = new LockedRestrictedAction<IEncounter>(EvACE.of(IEncounter.class, Right.UPDATE),
				Messages.GlobalActions_Redate) {

			@Override
			public IEncounter getTargetedObject() {
				return ContextServiceHolder.get().getTyped(IEncounter.class).orElse(null);
			}

			@Override
			public void doRun(IEncounter element) {
				DateSelectorDialog dlg = new DateSelectorDialog(mainWindow.getShell(), new TimeTool(element.getDate()),
						null);
				if (dlg.open() == Dialog.OK) {
					TimeTool date = dlg.getSelectedDate();
					Result<IEncounter> result = EncounterServiceHolder.get().setEncounterDate(element,
							date.toLocalDate());
					if (!result.isOK()) {
						SWTHelper.alert("Warnung", ResultDialog.getResultMessage(result));
					}
					// notify listeners about change
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, element);
				}
			}
		};
		delFallAction = new LockedRestrictedAction<Fall>(EvACE.of(ICoverage.class, Right.DELETE),
				Messages.GlobalActions_DeleteCase) {
			@Override
			public void doRun(Fall element) {
				if ((element.delete(false) == false)) {
					SWTHelper.alert(Messages.GlobalActions_CouldntDeleteCaseMessage,
							Messages.GlobalActions_CouldntDeleteCaseExplanation);
				}
				ElexisEventDispatcher.reload(Fall.class);
			}

			@Override
			public Fall getTargetedObject() {
				return (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			}
		};
		delKonsAction = new LockedRestrictedAction<Konsultation>(EvACE.of(IEncounter.class, Right.DELETE),
				Messages.GlobalActions_DeleteKons) {

			@Override
			public void doRun(Konsultation element) {
				if (element.delete(false) == false) {
					SWTHelper.alert(Messages.GlobalActions_CouldntDeleteKons,
							Messages.GlobalActions_CouldntDeleteKonsExplanation + Messages.GlobalActions_97);
				}
				ElexisEventDispatcher.clearSelection(Konsultation.class);
				ElexisEventDispatcher.fireSelectionEvent(element.getFall());
			}

			@Override
			public Konsultation getTargetedObject() {
				return (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			}
		};
		openFallaction = new Action(Messages.Core_Edit_Case) {

			@Override
			public void run() {
				try {
					Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(FallDetailView.ID);
					// getViewSite().getPage().showView(FallDetailView.ID);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}

		};
		closeFallAction = new LockedAction<Fall>(Messages.GlobalActions_CloseCase) {

			@Override
			public Fall getTargetedObject() {
				return (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			}

			@Override
			public void doRun(Fall fall) {
				if (hasUnbilledConsultations(fall)) {
					Display display = Display.getDefault();
					Shell shell = new Shell(display);
					MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
					dialog.setText(Messages.GlobalActions_CloseCaseDialogTitel);
					dialog.setMessage(
							((String) Messages.GlobalActions_CloseCaseDialog).replace("{0}", fall.getLabel()));
					int response = dialog.open();
					if (response == SWT.NO) {
						return;
					}
				}
				DateSelectorDialog dsd = new DateSelectorDialog(UiDesk.getTopShell(), null,
						Messages.GlobalActions_CloseCase_SelectCloseDate);
				int retVal = dsd.open();
				if (Dialog.OK == retVal) {
					TimeTool endDate = dsd.getSelectedDate();
					fall.setEndDatum(new TimeTool(endDate.getTime()).toString(TimeTool.DATE_GER));
					ElexisEventDispatcher.getInstance()
							.fire(new ElexisEvent(fall, Fall.class, ElexisEvent.EVENT_UPDATE));
				}
			}
		};
		reopenFallAction = new LockedRestrictedAction<Fall>(EvACE.of(ICoverage.class, Right.DELETE).and(Right.EXECUTE),
				Messages.GlobalActions_ReopenCase) {
			@Override
			public void doRun(Fall element) {
				element.setEndDatum(StringConstants.EMPTY);
				ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(element, Fall.class, ElexisEvent.EVENT_UPDATE));
			}

			@Override
			public Fall getTargetedObject() {
				return (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			}
		};
		neuerFallAction = new Action(Messages.Core_New_Case) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.Core_Create_new_case);
			}

			@Override
			public void run() {
				Patient pat = ElexisEventDispatcher.getSelectedPatient();
				if (pat != null) {
					NeuerFallDialog nfd = new NeuerFallDialog(mainWindow.getShell(), null);
					if (nfd.open() == Dialog.OK) {

					}
				}
			}
		};
		planeRechnungAction = new Action(Messages.GlobalActions_plaBill) {
			@Override
			public void run() {

			}
		};
	}

	public boolean hasUnbilledConsultations(Fall fall) {
		Konsultation[] konsultationen = fall.getBehandlungen(false);
		for (Konsultation kons : konsultationen) {
			Rechnung rechnung = kons.getRechnung();
			if (rechnung == null || rechnung.getInvoiceState() == InvoiceState.CANCELLED) {
				return true;
			}
		}
		return false;
	}

	protected void printPatient(final Patient patient) {
		PrinterData pd = getPrinterData("Etiketten"); //$NON-NLS-1$
		if (pd != null) {
			// 25.01.2010 patch tschaller: page orientation of printer
			// driver is not handled correctly (we always get porttrait
			// even when the printer settings have landscape stored)
			Integer iOrientation = -1;
			String sOrientation = CoreHub.localCfg.get("Drucker/Etiketten/Ausrichtung", null); //$NON-NLS-1$
			try {
				iOrientation = Integer.parseInt(sOrientation);
			} catch (NumberFormatException ex) {
			}
			if (iOrientation != -1)
				pd.orientation = iOrientation;
			Printer prn = new Printer(pd);
			if (prn.startJob(Messages.GlobalActions_PrintLabelJobName) == true) {
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				gc.drawString(Messages.GlobalActions_PatientIDLabelText + patient.getPatCode(), 0, 0);
				FontMetrics fmt = gc.getFontMetrics();
				y += fmt.getHeight();
				String pers = patient.getPersonalia();
				gc.drawString(pers, 0, y);
				y += fmt.getHeight();
				gc.drawString(patient.getAnschrift().getEtikette(false, false), 0, y);
				y += fmt.getHeight();
				StringBuilder tel = new StringBuilder();
				tel.append(Messages.GlobalActions_PhoneHomeLabelText).append(patient.get("Telefon1")) //$NON-NLS-1$ //$NON-NLS-2$
						.append(Messages.GlobalActions_PhoneWorkLabelText).append(patient.get("Telefon2")) //$NON-NLS-1$ //$NON-NLS-2$
						.append(Messages.GlobalActions_PhoneMobileLabelText).append(patient.get("Natel")); //$NON-NLS-1$ //$NON-NLS-2$
				gc.drawString(tel.toString(), 0, y);
				gc.dispose();
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog.openError(mainWindow.getShell(), Messages.GlobalActions_PrinterErrorTitle,
						Messages.GlobalActions_PrinterErrorMessage);

			}
		}
	}

	protected void printPatientAuftragsnummer(final Patient patient) {
		PrinterData pd = getPrinterData("Etiketten"); //$NON-NLS-1$
		if (pd != null) {
			// 25.01.2010 patch tschaller: page orientation of printer
			// driver is not handled correctly (we always get porttrait
			// even when the printer settings have landscape stored)
			Integer iOrientation = -1;
			String sOrientation = CoreHub.localCfg.get("Drucker/Etiketten/Ausrichtung", null); //$NON-NLS-1$
			try {
				iOrientation = Integer.parseInt(sOrientation);
			} catch (NumberFormatException ex) {
			}
			if (iOrientation != -1)
				pd.orientation = iOrientation;
			Printer prn = new Printer(pd);
			if (prn.startJob(Messages.GlobalActions_PrintLabelJobName) == true) {
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				String pid = StringTool.addModulo10(patient.getPatCode()) + "-" //$NON-NLS-1$
						+ new TimeTool().toString(TimeTool.TIME_COMPACT);
				gc.drawString(Messages.Order_ID + ": " + pid, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
				FontMetrics fmt = gc.getFontMetrics();
				y += fmt.getHeight();
				String pers = patient.getPersonalia();
				gc.drawString(pers, 0, y);
				y += fmt.getHeight();
				gc.drawString(patient.getAnschrift().getEtikette(false, false), 0, y);
				y += fmt.getHeight();
				StringBuilder tel = new StringBuilder();
				tel.append(Messages.GlobalActions_PhoneHomeLabelText).append(patient.get("Telefon1")) //$NON-NLS-1$
						.append(Messages.GlobalActions_PhoneWorkLabelText).append(patient.get("Telefon2")) //$NON-NLS-1$
						.append(Messages.GlobalActions_PhoneMobileLabelText).append(patient.get("Natel")); //$NON-NLS-1$
				gc.drawString(tel.toString(), 0, y);
				gc.dispose();
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog.openError(mainWindow.getShell(), Messages.GlobalActions_PrinterErrorTitle,
						Messages.GlobalActions_PrinterErrorMessage);
			}
		}
	}

	protected void printAdr(final Kontakt k) {
		// 25.01.2010 patch tschaller: there was always the printer selection
		// dialog. With printEtikette it wasn't so I copied the hardcoded string
		// from there
		// PrinterData pd =
		// getPrinterData(Messages.getString("GlobalActions.printersticker"));
		PrinterData pd = getPrinterData("Etiketten"); //$NON-NLS-1$
		if (pd != null) {
			// 25.01.2010 patch tschaller: page orientation of printer driver is
			// not handled correctly (we always get porttrait even when the
			// printer settings have landscape stored)
			Integer iOrientation = -1;
			String sOrientation = CoreHub.localCfg.get("Drucker/Etiketten/Ausrichtung", null); //$NON-NLS-1$
			try {
				iOrientation = Integer.parseInt(sOrientation);
			} catch (NumberFormatException ex) {
			}
			if (iOrientation != -1)
				pd.orientation = iOrientation;
			Printer prn = new Printer(pd);
			if (prn.startJob("Etikette drucken") == true) { //$NON-NLS-1$
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				FontMetrics fmt = gc.getFontMetrics();
				String pers = k.getPostAnschrift(true);
				String[] lines = pers.split(StringUtils.LF);
				for (String line : lines) {
					gc.drawString(line, 0, y);
					y += fmt.getHeight();
				}
				gc.dispose();
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog.openError(mainWindow.getShell(), Messages.GlobalActions_PrinterErrorTitle,
						Messages.GlobalActions_PrinterErrorMessage);

			}

		}
	}

	/**
	 * Return a PrinterData object according to the given type (e. g. "Etiketten")
	 * and the user settings. Shows a printer selection dialog if required.
	 *
	 * @param type the printer type according to the printer settings
	 * @return a PrinterData object describing the selected printer
	 */
	private PrinterData getPrinterData(final String type) {
		String cfgPrefix = "Drucker/" + type + "/"; //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$

		PrinterData pd = null;
		String printer = CoreHub.localCfg.get(cfgPrefix + "Name", null); //$NON-NLS-1$
		String driver = CoreHub.localCfg.get(cfgPrefix + "Driver", null); //$NON-NLS-1$
		boolean choose = CoreHub.localCfg.get(cfgPrefix + "Choose", false); //$NON-NLS-1$
		if (choose || StringTool.isNothing(printer) || StringTool.isNothing(driver)) {
			Shell shell = UiDesk.getTopShell();
			PrintDialog pdlg = new PrintDialog(shell);
			pd = pdlg.open();
		} else {
			pd = new PrinterData(driver, printer);
		}

		return pd;
	}

	/**
	 * Return true if direct printing on defined printer. If false, the user has to
	 * choose the printer and print himself
	 */
	private boolean isDirectPrint() {
		return !CoreHub.localCfg.get("Drucker/Etiketten/Choose", true);
	}

	/**
	 * Creates an ActionHandler for the given IAction and registers it to the Site's
	 * HandlerService, i. e. binds the action to the command so that key bindings
	 * get activated. You need to set the action's actionDefinitionId to the command
	 * id.
	 *
	 * @param action the action to activate. The action's actionDefinitionId must
	 *               have been set to the command's id (using
	 *               <code>setActionDefinitionId()</code>)
	 * @param part   the view this action should be registered for
	 */
	public static void registerActionHandler(final ViewPart part, final IAction action) {
		String commandId = action.getActionDefinitionId();
		if (!StringTool.isNothing(commandId)) {
			IHandlerService handlerService = part.getSite().getService(IHandlerService.class);
			IHandler handler = new ActionHandler(action);
			handlerService.activateHandler(commandId, handler);
		}
	}

	public static void updateModelOfParts(MPerspective activePerspective, EModelService modelService) {
		// perspective with actionsets want visible part toolbars
		if (activePerspective.getTags().stream().filter(t -> t.toLowerCase().contains("actionset")).findFirst()
				.isPresent()) {
			List<MPart> mParts = modelService.findElements(getActivePerspective(modelService), null, MPart.class);
			for (MPart mPart : mParts) {
				if (mPart.getToolbar() != null) {
					mPart.getToolbar().setVisible(true);
				}
			}
		}
	}

	public static void addModelOfParts(EPartService partService) {
		for (MPart mPart : partService.getParts()) {
			if (!mPart.isToBeRendered() && mPart.getTags().contains("GlobalActions:deleted")) {
				partService.showPart(mPart, PartState.CREATE);
				mPart.getTags().remove("GlobalActions:deleted");
			}
		}
	}

	public static void removeModelOfParts(MPerspective mPerspective, EModelService modelService) {
		List<MPart> mParts = modelService.findElements(mPerspective, null, MPart.class);
		for (MPart mPart : mParts) {
			if (mPart.getWidget() instanceof Composite) {
				try {
					mPart.getTags().add("GlobalActions:deleted");
					modelService.deleteModelElement(mPart);
				} catch (Exception e) {
					// ignore keep on resetting
				}
			}
		}
	}

	public static MPerspective getActivePerspective(EModelService modelService) {
		MTrimmedWindow mWindow = getActiveWindow(modelService);
		if (mWindow != null) {
			return modelService.getActivePerspective(mWindow);
		}
		return null;
	}

	private static MTrimmedWindow getActiveWindow(EModelService modelService) {
		MApplication mApplication = PlatformUI.getWorkbench().getService(MApplication.class);

		MTrimmedWindow mWindow = (MTrimmedWindow) modelService.find("IDEWindow", mApplication); //$NON-NLS-1$
		if (mWindow == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				mWindow = (MTrimmedWindow) windows.get(0);
			}
		}
		return mWindow;
	}
}
