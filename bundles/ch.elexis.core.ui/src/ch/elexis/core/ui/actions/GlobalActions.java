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

import static ch.elexis.admin.AccessControlDefaults.AC_ABOUT;
import static ch.elexis.admin.AccessControlDefaults.AC_CHANGEMANDANT;
import static ch.elexis.admin.AccessControlDefaults.AC_CONNECT;
import static ch.elexis.admin.AccessControlDefaults.AC_EXIT;
import static ch.elexis.admin.AccessControlDefaults.AC_HELP;
import static ch.elexis.admin.AccessControlDefaults.AC_IMORT;
import static ch.elexis.admin.AccessControlDefaults.AC_LOGIN;
import static ch.elexis.admin.AccessControlDefaults.AC_NEWWINDOW;
import static ch.elexis.admin.AccessControlDefaults.AC_PREFS;
import static ch.elexis.admin.AccessControlDefaults.AC_SHOWVIEW;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_ADDRESS_LABEL;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_KG_COVER_SHEET;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_PATIENT_LABEL;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_PATIENT_LABEL_ORDER;
import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_XRAY;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.constants.UiPreferenceConstants;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.dialogs.EtiketteDruckenDialog;
import ch.elexis.core.ui.dialogs.LoginDialog;
import ch.elexis.core.ui.dialogs.NeuerFallDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockedAction;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.TemplateDrucker;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.TemplatePrintView;
import ch.elexis.core.ui.wizards.DBConnectWizard;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
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
	public static IAction connectWizardAction, changeMandantAction, savePerspectiveAction,
			savePerspectiveAsAction;
	public static IAction savePerspectiveAsDefaultAction, resetPerspectiveAction, homeAction,
			fixLayoutAction;
	public static IAction printEtikette, printBlatt, printAdresse, printVersionedEtikette,
			showBlatt;
	public static IAction printRoeBlatt;
	public static IAction openFallaction, closeFallAction, filterAction, makeBillAction, planeRechnungAction;
	public static RestrictedAction delKonsAction, delFallAction, reopenFallAction, neueKonsAction;
	public static LockedAction<Konsultation> moveBehandlungAction, redateAction;
	public static IAction neuerFallAction;
	
	public static MenuManager perspectiveMenu, viewMenu;
	public static IContributionItem viewList;
	public IWorkbenchWindow mainWindow;
	public static Action printKontaktEtikette;
	private static IWorkbenchHelpSystem help;
	private static Logger logger;
	
	public GlobalActions(final IWorkbenchWindow window){
		if (Hub.mainActions != null) {
			return;
		}
		logger = LoggerFactory.getLogger(this.getClass());
		mainWindow = window;
		help = Hub.plugin.getWorkbench().getHelpSystem();
		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setText(Messages.GlobalActions_MenuExit); //$NON-NLS-1$
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		newWindowAction.setText(Messages.GlobalActions_NewWindow); //$NON-NLS-1$
		copyAction = ActionFactory.COPY.create(window);
		copyAction.setText(Messages.GlobalActions_Copy); //$NON-NLS-1$
		cutAction = ActionFactory.CUT.create(window);
		cutAction.setText(Messages.GlobalActions_Cut); //$NON-NLS-1$
		pasteAction = ActionFactory.PASTE.create(window);
		pasteAction.setText(Messages.GlobalActions_Paste); //$NON-NLS-1$
		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setText(Messages.GlobalActions_MenuAbout); //$NON-NLS-1$
		prefsAction = ActionFactory.PREFERENCES.create(window);
		prefsAction.setText(Messages.GlobalActions_Preferences); //$NON-NLS-1$
		savePerspectiveAction = new Action(Messages.GlobalActions_SavePerspective) { //$NON-NLS-1$
			{
				setId("savePerspektive"); //$NON-NLS-1$
				// setActionDefinitionId(Hub.COMMAND_PREFIX+"savePerspektive"); //$NON-NLS-1$
				setToolTipText(Messages.GlobalActions_SavePerspectiveToolTip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_DISK.getImageDescriptor()); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				IWorkbenchPage page = mainWindow.getActivePage();
				if (page != null && page.getPerspective() != null) {
					page.savePerspectiveAs(page.getPerspective());
				}
			}
		};
		
		helpAction = new Action(Messages.GlobalActions_ac_handbook) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_BOOK.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_ac_openhandbook); //$NON-NLS-1$
				
			}
			
			@Override
			public void run(){
				Desktop desktop = Desktop.getDesktop();
				String url = CoreHub.globalCfg.get(UiPreferenceConstants.CFG_HANDBOOK,
					UiPreferenceConstants.DEFAULT_HANDBOOK);
				if (Desktop.isDesktopSupported()) {
					try {
						desktop.browse(new java.net.URI(url));
					} catch (Exception e) {
						logger.warn("failed to open default browser :" + e);
						MessageDialog.openError(mainWindow.getShell(), Messages.GlobalActions_Error,
							Messages.GlobalActions_PreferencesHandbook_URL);
						ExHandler.handle(e);
					}
				} else {
					logger.warn("isDesktopSupported was false.");
				}
			}
		};
		savePerspectiveAsAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
		
		// ActionFactory.SAVE_PERSPECTIVE.create(window);
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		resetPerspectiveAction.setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
		
		homeAction = new Action(Messages.GlobalActions_Home) { //$NON-NLS-1$
			{
				setId("home"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "home"); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_HOME.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_HomeToolTip); //$NON-NLS-1$
				help.setHelp(this, "ch.elexis.globalactions.homeAction"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				// String
				// perspektive=CoreHub.actUser.getInfoString("StartPerspektive");
				String perspektive =
					CoreHub.localCfg.get(CoreHub.actUser + DEFAULTPERSPECTIVECFG, null);
				if (StringTool.isNothing(perspektive)) {
					perspektive = UiResourceConstants.PatientPerspektive_ID;
				}
				try {
					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					PlatformUI.getWorkbench().showPerspective(perspektive, win);
					// Hub.heart.resume(true);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		};
		savePerspectiveAsDefaultAction = new Action(Messages.GlobalActions_saveasstartperspective) { //$NON-NLS-1$
			{
				setId("start"); //$NON-NLS-1$
				// setActionDefinitionId(Hub.COMMAND_PREFIX+"startPerspective");
			}
			
			@Override
			public void run(){
				IPerspectiveDescriptor p = mainWindow.getActivePage().getPerspective();
				CoreHub.localCfg.set(CoreHub.actUser + DEFAULTPERSPECTIVECFG, p.getId());
				// CoreHub.actUser.setInfoElement("StartPerspektive",p.getId());
			}
			
		};
		loginAction = new Action(Messages.GlobalActions_Login) { //$NON-NLS-1$
			{
				setId("login"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "login"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				try {
					IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
					for (IWorkbenchWindow w : wins) {
						if (!w.equals(win)) {
							w.close();
						}
					}
					CoreHub.logoffAnwender();
					
					LoginDialog dlg = new LoginDialog(win.getShell());
					dlg.create();
					dlg.setTitle(Messages.GlobalActions_LoginDialogTitle); //$NON-NLS-1$
					dlg.setMessage(Messages.GlobalActions_LoginDialogMessage); //$NON-NLS-1$
					// dlg.getButton(IDialogConstants.CANCEL_ID).setText("Beenden");
					dlg.getShell().setText(Messages.GlobalActions_LoginDialogShelltext); //$NON-NLS-1$
					if (dlg.open() == Dialog.CANCEL) {
						exitAction.run();
					}
					adaptForUser();
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
				System.out.println("login"); //$NON-NLS-1$
			}
		};
		importAction = new Action(Messages.GlobalActions_Import) { //$NON-NLS-1$
			{
				setId("import"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "import"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				// cnv.open();
				Importer imp =
					new Importer(mainWindow.getShell(), ExtensionPointConstantsUi.FREMDDATENIMPORT);
				imp.create();
				imp.setMessage(Messages.GlobalActions_ImportDlgMessage); //$NON-NLS-1$
				imp.getShell().setText(Messages.GlobalActions_ImportDlgShelltext); //$NON-NLS-1$
				imp.setTitle(Messages.GlobalActions_ImportDlgTitle); //$NON-NLS-1$
				imp.open();
			}
		};
		
		connectWizardAction = new Action(Messages.GlobalActions_Connection) { //$NON-NLS-1$
			{
				setId("connectWizard"); //$NON-NLS-1$
				setActionDefinitionId(Hub.COMMAND_PREFIX + "connectWizard"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				WizardDialog wd = new WizardDialog(mainWindow.getShell(), new DBConnectWizard());
				wd.open();
			}
			
		};
		
		changeMandantAction = new Action(Messages.GlobalActions_Mandator) { //$NON-NLS-1$
			{
				setId("changeMandant"); //$NON-NLS-1$
				// setActionDefinitionId(Hub.COMMAND_PREFIX+"changeMandant"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				ChangeMandantDialog cmd = new ChangeMandantDialog();
				if (cmd.open() == org.eclipse.jface.dialogs.Dialog.OK) {
					Mandant n = cmd.result;
					if (n != null) {
						Hub.setMandant(n);
					}
				}
			}
		};
		printKontaktEtikette = new Action(Messages.GlobalActions_PrintContactLabel) { //$NON-NLS-1$
			{
				setToolTipText(Messages.GlobalActions_PrintContactLabelToolTip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_ADRESSETIKETTE.getImageDescriptor());
			}
			
			@Override
			public void run(){
				Kontakt kontakt = (Kontakt) ElexisEventDispatcher.getSelected(Kontakt.class);
				if (kontakt == null) {
					SWTHelper.showInfo("Kein Kontakt ausgewählt",
						"Bitte wählen Sie vor dem Drucken einen Kontakt!");
					return;
				}
				EtiketteDruckenDialog dlg =
					new EtiketteDruckenDialog(mainWindow.getShell(), kontakt, TT_ADDRESS_LABEL);
				dlg.setTitle(Messages.GlobalActions_PrintContactLabel);
				dlg.setMessage(Messages.GlobalActions_PrintContactLabelToolTip);
				if (isDirectPrint()) {
					dlg.setBlockOnOpen(false);
					dlg.open();
					if (dlg.doPrint()) {
						dlg.close();
					} else {
						SWTHelper.alert("Fehler beim Drucken",
							"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
					}
				} else {
					dlg.setBlockOnOpen(true);
					dlg.open();
				}
			}
		};
		
		printAdresse = new Action(Messages.GlobalActions_PrintAddressLabel) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_ADRESSETIKETTE.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_PrintAddressLabelToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				if (actPatient == null) {
					SWTHelper.showInfo("Kein Patient ausgewählt",
						"Bitte wählen Sie vor dem Drucken einen Patient!");
					return;
				}
				
				EtiketteDruckenDialog dlg =
					new EtiketteDruckenDialog(mainWindow.getShell(), actPatient, TT_ADDRESS_LABEL);
				dlg.setTitle(Messages.GlobalActions_PrintAddressLabel);
				dlg.setMessage(Messages.GlobalActions_PrintAddressLabelToolTip);
				if (isDirectPrint()) {
					dlg.setBlockOnOpen(false);
					dlg.open();
					if (dlg.doPrint()) {
						dlg.close();
					} else {
						SWTHelper.alert("Fehler beim Drucken",
							"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
					}
				} else {
					dlg.setBlockOnOpen(true);
					dlg.open();
				}
			}
		};
		
		printVersionedEtikette = new Action(Messages.GlobalActions_PrintVersionedLabel) { //$NON-NLS-1$
			{
				setToolTipText(Messages.GlobalActions_PrintVersionedLabelToolTip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_VERSIONEDETIKETTE.getImageDescriptor());
			}
			
			@Override
			public void run(){
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				if (actPatient == null) {
					SWTHelper.showInfo("Kein Patient ausgewählt",
						"Bitte wählen Sie vor dem Drucken einen Patient!");
					return;
				}
				EtiketteDruckenDialog dlg = new EtiketteDruckenDialog(mainWindow.getShell(),
					actPatient, TT_PATIENT_LABEL_ORDER);
				dlg.setTitle(Messages.GlobalActions_PrintVersionedLabel);
				dlg.setMessage(Messages.GlobalActions_PrintVersionedLabelToolTip);
				if (isDirectPrint()) {
					dlg.setBlockOnOpen(false);
					dlg.open();
					if (dlg.doPrint()) {
						dlg.close();
					} else {
						SWTHelper.alert("Fehler beim Drucken",
							"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
					}
				} else {
					dlg.setBlockOnOpen(true);
					dlg.open();
				}
			}
		};
		
		printEtikette = new Action(Messages.GlobalActions_PrintLabel) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_PATIENTETIKETTE.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_PrintLabelToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				if (actPatient == null) {
					SWTHelper.showInfo("Kein Patient ausgewählt",
						"Bitte wählen Sie vor dem Drucken einen Patient!");
					return;
				}
				EtiketteDruckenDialog dlg =
					new EtiketteDruckenDialog(mainWindow.getShell(), actPatient, TT_PATIENT_LABEL);
				dlg.setTitle(Messages.GlobalActions_PrintLabel);
				dlg.setMessage(Messages.GlobalActions_PrintLabelToolTip);
				if (isDirectPrint()) {
					dlg.setBlockOnOpen(false);
					dlg.open();
					if (dlg.doPrint()) {
						dlg.close();
					} else {
						SWTHelper.alert("Fehler beim Drucken",
							"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
					}
				} else {
					dlg.setBlockOnOpen(true);
					dlg.open();
				}
			}
		};
		
		printBlatt = new Action(Messages.GlobalActions_PrintEMR) { //$NON-NLS-1$
			@Override
			public void run(){
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				String printer = CoreHub.localCfg.get("Drucker/Einzelblatt/Name", null); //$NON-NLS-1$
				String tray = CoreHub.localCfg.get("Drucker/Einzelblatt/Schacht", null); //$NON-NLS-1$
				
				new TemplateDrucker(TT_KG_COVER_SHEET, printer, tray).doPrint(actPatient); //$NON-NLS-1$
			}
		};
		showBlatt = new Action(Messages.GlobalActions_ShowEMR) { //$NON-NLS-1$
			@Override
			public void run(){
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				try {
					TemplatePrintView tpw = (TemplatePrintView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().showView(TemplatePrintView.ID);
					tpw.doShow(actPatient, TT_KG_COVER_SHEET);
				} catch (PartInitException e) {
					MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Fehler",
						"Konnte View nicht öffnen");
					LoggerFactory.getLogger(getClass())
						.error("Error showing " + TemplatePrintView.ID, e);
				}
			}
		};
		printRoeBlatt = new Action(Messages.GlobalActions_PrintXRay) { //$NON-NLS-1$
			@Override
			public void run(){
				Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				String printer = CoreHub.localCfg.get("Drucker/A4/Name", null); //$NON-NLS-1$
				String tray = CoreHub.localCfg.get("Drucker/A4/Schacht", null); //$NON-NLS-1$
				
				new TemplateDrucker(TT_XRAY, printer, tray).doPrint(actPatient); //$NON-NLS-1$
			}
		};
		
		fixLayoutAction = new Action(Messages.GlobalActions_LockPerspectives, Action.AS_CHECK_BOX) { //$NON-NLS-1$
			{
				setToolTipText(Messages.GlobalActions_LockPerspectivesToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				// store the current value in the user's configuration
				CoreHub.userCfg.set(Preferences.USR_FIX_LAYOUT, fixLayoutAction.isChecked());
			}
		};
		makeBillAction = new Action(Messages.GlobalActions_MakeBill) { //$NON-NLS-1$
			@Override
			public void run(){
				Fall actFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
				Mandant mnd = ElexisEventDispatcher.getSelectedMandator();
				if (actFall != null && mnd != null) {
					String rsId = mnd.getRechnungssteller().getId();
					Konsultation[] bhdl = actFall.getBehandlungen(false);
					List<Konsultation> lBehdl = new ArrayList<Konsultation>(bhdl.length);
					for (Konsultation b : bhdl) {
						Rechnung rn = b.getRechnung();
						if (rn == null) {
							if (b.getMandant().getRechnungssteller().getId().equals(rsId)) {
								lBehdl.add(b);
							}
						}
					}
					lBehdl = BillingUtil.getKonsultationsFromSameYear(lBehdl);
					Result<Rechnung> res = Rechnung.build(lBehdl);
					if (!res.isOK()) {
						ErrorDialog.openError(mainWindow.getShell(), Messages.GlobalActions_Error,
							Messages //$NON-NLS-1$
									.GlobalActions_BillErrorMessage,
							ResultAdapter //$NON-NLS-1$
								.getResultAsStatus(res));
						// Rechnung rn=(Rechnung)res.get();
						// rn.storno(true);
						// rn.delete();
						
					}
				}
				// setFall(actFall,null);
			}
		};
		moveBehandlungAction = new LockedAction<Konsultation>(Messages.GlobalActions_AssignCase) {
			@Override
			public Konsultation getTargetedObject(){
				return (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			}
			
			@Override
			public void doRun(Konsultation element){
				// TODO do we need to lock the fall?
				SelectFallDialog dlg = new SelectFallDialog(mainWindow.getShell());
				if (dlg.open() == Dialog.OK) {
					Fall f = dlg.result;
					if (f != null) {
						element.setFall(f);
						ElexisEventDispatcher.fireSelectionEvent(f);
					}
				}
			}
		};
		redateAction = new LockedAction<Konsultation>(Messages.GlobalActions_Redate) {
			
			@Override
			public Konsultation getTargetedObject(){
				return (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			}
			
			@Override
			public void doRun(Konsultation element){
				DateSelectorDialog dlg = new DateSelectorDialog(mainWindow.getShell());
				if (dlg.open() == Dialog.OK) {
					TimeTool date = dlg.getSelectedDate();
					element.setDatum(date.toString(TimeTool.DATE_GER), false);
					
					// notify listeners about change
					ElexisEventDispatcher.getInstance().fire(
						new ElexisEvent(element, Konsultation.class, ElexisEvent.EVENT_UPDATE));
					
					ElexisEventDispatcher.fireSelectionEvent(element);
				}
			}
		};
		delFallAction = new LockedRestrictedAction<Fall>(AccessControlDefaults.DELETE_CASE,
			Messages.GlobalActions_DeleteCase) {
			@Override
			public void doRun(Fall element){
				if ((element.delete(false) == false)) {
					SWTHelper.alert(Messages.GlobalActions_CouldntDeleteCaseMessage,
						Messages.GlobalActions_CouldntDeleteCaseExplanation);
				}
				ElexisEventDispatcher.reload(Fall.class);
			}
			
			@Override
			public Fall getTargetedObject(){
				return (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			}
		};
		delKonsAction = new LockedRestrictedAction<Konsultation>(AccessControlDefaults.KONS_DELETE,
			Messages.GlobalActions_DeleteKons) {
			
			@Override
			public void doRun(Konsultation element){
				if (element.delete(false) == false) {
					SWTHelper.alert(Messages.GlobalActions_CouldntDeleteKons, //$NON-NLS-1$
						Messages.GlobalActions_CouldntDeleteKonsExplanation + //$NON-NLS-1$
					Messages.GlobalActions_97); //$NON-NLS-1$
				}
				ElexisEventDispatcher.clearSelection(Konsultation.class);
				ElexisEventDispatcher.fireSelectionEvent(element.getFall());
			}
			
			@Override
			public Konsultation getTargetedObject(){
				return (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			}
		};
		openFallaction = new Action(Messages.GlobalActions_EditCase) { //$NON-NLS-1$
			
			@Override
			public void run(){
				try {
					Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(FallDetailView.ID);
					// getViewSite().getPage().showView(FallDetailView.ID);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
			
		};
		closeFallAction = new LockedAction<Fall>(Messages.GlobalActions_CloseCase) {
			
			@Override
			public Fall getTargetedObject(){
				return (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			}
			
			@Override
			public void doRun(Fall fall){
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
		reopenFallAction = new LockedRestrictedAction<Fall>(AccessControlDefaults.CASE_REOPEN,
			Messages.GlobalActions_ReopenCase) {
			@Override
			public void doRun(Fall element){
				element.setEndDatum(StringConstants.EMPTY);
			}
			
			@Override
			public Fall getTargetedObject(){
				return (Fall) ElexisEventDispatcher.getSelected(Fall.class);
			}
		};
		neueKonsAction = new RestrictedAction(AccessControlDefaults.KONS_CREATE,
			Messages.GlobalActions_NewKons) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_NewKonsToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void doRun(){
				Konsultation.neueKons(null);
				IPersistentObject kons = ElexisEventDispatcher.getSelected(Konsultation.class);
				if (kons != null && kons.exists()) {
					CoreHub.getLocalLockService().acquireLock(kons);
					CoreHub.getLocalLockService().releaseLock(kons);
				}
			}
		};
		neuerFallAction = new Action(Messages.GlobalActions_NewCase) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.GlobalActions_NewCaseToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				Patient pat = ElexisEventDispatcher.getSelectedPatient();
				if (pat != null) {
					NeuerFallDialog nfd = new NeuerFallDialog(mainWindow.getShell(), null);
					if (nfd.open() == Dialog.OK) {
					
					}
				}
			}
		};
		planeRechnungAction = new Action(Messages.GlobalActions_plaBill) { //$NON-NLS-1$
			public void run(){
			
			}
		};
	}
	
	protected void printPatient(final Patient patient){
		PrinterData pd = getPrinterData("Etiketten"); //$NON-NLS-1$
		if (pd != null) {
			// 25.01.2010 patch tschaller: page orientation of printer
			// driver is not handled correctly (we always get porttrait
			// even when the printer settings have landscape stored)
			Integer iOrientation = -1;
			String sOrientation = CoreHub.localCfg.get("Drucker/Etiketten/Ausrichtung", null); //$NON-NLS-1$
			try {
				iOrientation = Integer.parseInt(sOrientation);
			} catch (NumberFormatException ex) {}
			if (iOrientation != -1)
				pd.orientation = iOrientation;
			Printer prn = new Printer(pd);
			if (prn.startJob(Messages.GlobalActions_PrintLabelJobName) == true) { //$NON-NLS-1$
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				gc.drawString(Messages.GlobalActions_PatientIDLabelText + patient.getPatCode(), 0,
					0); //$NON-NLS-1$
				FontMetrics fmt = gc.getFontMetrics();
				y += fmt.getHeight();
				String pers = patient.getPersonalia();
				gc.drawString(pers, 0, y);
				y += fmt.getHeight();
				gc.drawString(patient.getAnschrift().getEtikette(false, false), 0, y);
				y += fmt.getHeight();
				StringBuilder tel = new StringBuilder();
				tel.append(Messages.GlobalActions_PhoneHomeLabelText)
					.append(patient.get("Telefon1")) //$NON-NLS-1$ //$NON-NLS-2$
					.append(Messages.GlobalActions_PhoneWorkLabelText)
					.append(patient.get("Telefon2")) //$NON-NLS-1$ //$NON-NLS-2$
					.append(Messages.GlobalActions_PhoneMobileLabelText)
					.append(patient.get("Natel")); //$NON-NLS-1$ //$NON-NLS-2$
				gc.drawString(tel.toString(), 0, y);
				gc.dispose();
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog.openError(mainWindow.getShell(),
					Messages.GlobalActions_PrinterErrorTitle,
					Messages.GlobalActions_PrinterErrorMessage); //$NON-NLS-1$ //$NON-NLS-2$
				
			}
		}
	}
	
	protected void printPatientAuftragsnummer(final Patient patient){
		PrinterData pd = getPrinterData("Etiketten"); //$NON-NLS-1$
		if (pd != null) {
			// 25.01.2010 patch tschaller: page orientation of printer
			// driver is not handled correctly (we always get porttrait
			// even when the printer settings have landscape stored)
			Integer iOrientation = -1;
			String sOrientation = CoreHub.localCfg.get("Drucker/Etiketten/Ausrichtung", null); //$NON-NLS-1$
			try {
				iOrientation = Integer.parseInt(sOrientation);
			} catch (NumberFormatException ex) {}
			if (iOrientation != -1)
				pd.orientation = iOrientation;
			Printer prn = new Printer(pd);
			if (prn.startJob(Messages.GlobalActions_PrintLabelJobName) == true) { //$NON-NLS-1$
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				String pid = StringTool.addModulo10(patient.getPatCode()) + "-" //$NON-NLS-1$
					+ new TimeTool().toString(TimeTool.TIME_COMPACT);
				gc.drawString(Messages.GlobalActions_OrderID + ": " + pid, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
				FontMetrics fmt = gc.getFontMetrics();
				y += fmt.getHeight();
				String pers = patient.getPersonalia();
				gc.drawString(pers, 0, y);
				y += fmt.getHeight();
				gc.drawString(patient.getAnschrift().getEtikette(false, false), 0, y);
				y += fmt.getHeight();
				StringBuilder tel = new StringBuilder();
				tel.append(Messages.GlobalActions_PhoneHomeLabelText)
					.append(patient.get("Telefon1")) //$NON-NLS-1$ //$NON-NLS-2$
					.append(Messages.GlobalActions_PhoneWorkLabelText)
					.append(patient.get("Telefon2")) //$NON-NLS-1$ //$NON-NLS-2$
					.append(Messages.GlobalActions_PhoneMobileLabelText)
					.append(patient.get("Natel")); //$NON-NLS-1$ //$NON-NLS-2$
				gc.drawString(tel.toString(), 0, y);
				gc.dispose();
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog.openError(mainWindow.getShell(),
					Messages.GlobalActions_PrinterErrorTitle,
					Messages.GlobalActions_PrinterErrorMessage); //$NON-NLS-1$ //$NON-NLS-2$
				
			}
		}
	}
	
	protected void printAdr(final Kontakt k){
		// 25.01.2010 patch tschaller: there was always the printer selection
		// dialog. With printEtikette it wasn't so I copied the hardcoded string
		// from there
		// PrinterData pd =
		// getPrinterData(Messages.getString("GlobalActions.printersticker"));
		// //$NON-NLS-1$
		PrinterData pd = getPrinterData("Etiketten"); //$NON-NLS-1$
		if (pd != null) {
			// 25.01.2010 patch tschaller: page orientation of printer driver is
			// not handled correctly (we always get porttrait even when the
			// printer settings have landscape stored)
			Integer iOrientation = -1;
			String sOrientation = CoreHub.localCfg.get("Drucker/Etiketten/Ausrichtung", null); //$NON-NLS-1$
			try {
				iOrientation = Integer.parseInt(sOrientation);
			} catch (NumberFormatException ex) {}
			if (iOrientation != -1)
				pd.orientation = iOrientation;
			Printer prn = new Printer(pd);
			if (prn.startJob("Etikette drucken") == true) { //$NON-NLS-1$
				GC gc = new GC(prn);
				int y = 0;
				prn.startPage();
				FontMetrics fmt = gc.getFontMetrics();
				String pers = k.getPostAnschrift(true);
				String[] lines = pers.split("\n"); //$NON-NLS-1$
				for (String line : lines) {
					gc.drawString(line, 0, y);
					y += fmt.getHeight();
				}
				gc.dispose();
				prn.endPage();
				prn.endJob();
				prn.dispose();
			} else {
				MessageDialog.openError(mainWindow.getShell(),
					Messages.GlobalActions_PrinterErrorTitle,
					Messages.GlobalActions_PrinterErrorMessage); //$NON-NLS-1$ //$NON-NLS-2$
				
			}
			
		}
	}
	
	/**
	 * Return a PrinterData object according to the given type (e. g. "Etiketten") and the user
	 * settings. Shows a printer selection dialog if required.
	 * 
	 * @param type
	 *            the printer type according to the printer settings
	 * @return a PrinterData object describing the selected printer
	 */
	private PrinterData getPrinterData(final String type){
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
	 * Return true if direct printing on defined printer. If false, the user has to choose the
	 * printer and print himself
	 */
	private boolean isDirectPrint(){
		return !CoreHub.localCfg.get("Drucker/Etiketten/Choose", true);
	}
	
	/**
	 * Verfügbarkeit der einzelnen Menuepunkte an den angemeldeten Anwender anpassen
	 * Menueeinstellungen wiederherstellen
	 */
	public void adaptForUser(){
		setMenuForUser(AC_EXIT, exitAction);
		// setMenuForUser(AC_UPDATE,updateAction); //$NON-NLS-1$
		setMenuForUser(AC_NEWWINDOW, newWindowAction);
		setMenuForUser(AC_LOGIN, loginAction);
		setMenuForUser(AC_IMORT, importAction);
		setMenuForUser(AC_ABOUT, aboutAction);
		setMenuForUser(AC_HELP, helpAction);
		setMenuForUser(AC_PREFS, prefsAction);
		setMenuForUser(AC_CHANGEMANDANT, changeMandantAction);
		// setMenuForUser("importTarmedAction",importTarmedAction);
		setMenuForUser(AC_CONNECT, connectWizardAction);
		if (CoreHub.acl.request(AC_SHOWVIEW) == true) {
			viewList.setVisible(true);
		} else {
			viewList.setVisible(false);
		}
		
		// restore menue settings
		if (CoreHub.actUser != null) {
			boolean fixLayoutChecked =
				CoreHub.userCfg.get(Preferences.USR_FIX_LAYOUT, Preferences.USR_FIX_LAYOUT_DEFAULT);
			fixLayoutAction.setChecked(fixLayoutChecked);
			// System.err.println("fixLayoutAction: set to " +
			// fixLayoutChecked);
		} else {
			fixLayoutAction.setChecked(Preferences.USR_FIX_LAYOUT_DEFAULT);
			// System.err.println("fixLayoutAction: reset to false");
		}
	}
	
	private void setMenuForUser(final ACE ace, final IAction action){
		if (CoreHub.acl.request(ace) == true) {
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
		
	}
	
	/**
	 * Creates an ActionHandler for the given IAction and registers it to the Site's HandlerService,
	 * i. e. binds the action to the command so that key bindings get activated. You need to set the
	 * action's actionDefinitionId to the command id.
	 * 
	 * @param action
	 *            the action to activate. The action's actionDefinitionId must have been set to the
	 *            command's id (using <code>setActionDefinitionId()</code>)
	 * @param part
	 *            the view this action should be registered for
	 */
	public static void registerActionHandler(final ViewPart part, final IAction action){
		String commandId = action.getActionDefinitionId();
		if (!StringTool.isNothing(commandId)) {
			IHandlerService handlerService = part.getSite().getService(IHandlerService.class);
			IHandler handler = new ActionHandler(action);
			handlerService.activateHandler(commandId, handler);
		}
	}
	
	class ChangeMandantDialog extends TitleAreaDialog {
		List<Mandant> lMandant;
		org.eclipse.swt.widgets.List lbMandant;
		Mandant result;
		
		ChangeMandantDialog(){
			super(mainWindow.getShell());
		}
		
		@Override
		public Control createDialogArea(final Composite parent){
			lbMandant = new org.eclipse.swt.widgets.List(parent, SWT.BORDER | SWT.SINGLE);
			lbMandant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
			lMandant = qbe.execute();
			for (PersistentObject m : lMandant) {
				lbMandant.add(m.getLabel());
			}
			return lbMandant;
		}
		
		@Override
		protected void okPressed(){
			int idx = lbMandant.getSelectionIndex();
			if (idx > -1) {
				result = lMandant.get(idx);
			}
			super.okPressed();
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.GlobalActions_ChangeMandator); //$NON-NLS-1$
			setMessage(Messages.GlobalActions_ChangeMandatorMessage); //$NON-NLS-1$
		}
		
	};
}
