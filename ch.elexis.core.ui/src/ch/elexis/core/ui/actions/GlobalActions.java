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
import static ch.elexis.admin.AccessControlDefaults.AC_SHOWPERSPECTIVE;
import static ch.elexis.admin.AccessControlDefaults.AC_SHOWVIEW;

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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.ACE;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.dialogs.EtiketteDruckenDialog;
import ch.elexis.core.ui.dialogs.LoginDialog;
import ch.elexis.core.ui.dialogs.NeuerFallDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.TemplateDrucker;
import ch.elexis.core.ui.views.FallDetailView;
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
	public static IAction loginAction, importAction, testAction, aboutAction, helpAction,
			prefsAction;
	public static IAction connectWizardAction, changeMandantAction, savePerspectiveAction,
			savePerspectiveAsAction;
	public static IAction savePerspectiveAsDefaultAction, resetPerspectiveAction, homeAction,
			fixLayoutAction;
	public static IAction printEtikette, printBlatt, printAdresse, printVersionedEtikette;
	public static IAction printRoeBlatt;
	public static IAction delFallAction, delKonsAction, openFallaction, filterAction,
			reopenFallAction, makeBillAction, planeRechnungAction;
	public static IAction moveBehandlungAction, redateAction, neueKonsAction, neuerFallAction;
	
	public static MenuManager perspectiveMenu, viewMenu;
	public static IContributionItem perspectiveList, viewList;
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
		// helpAction=ActionFactory.HELP_CONTENTS.create(window);
		// helpAction.setText(Messages.getString("GlobalActions.HelpIndex")); //$NON-NLS-1$
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
					mainWindow.getActivePage().savePerspective();
				}
			};
		
		helpAction = new Action(Messages.GlobalActions_ac_handbook) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_BOOK.getImageDescriptor());
					setToolTipText(Messages.GlobalActions_ac_openhandbook); //$NON-NLS-1$
					
				}
				
				@Override
				public void run(){
					File book =
						new File(Platform.getInstallLocation().getURL().getPath() + "elexis.pdf"); //$NON-NLS-1$
					Program proggie = Program.findProgram(".pdf"); //$NON-NLS-1$
					if (proggie != null) {
						logger
							.info("will open handbook: " + book.toString() + " using: " + proggie);
						proggie.execute(book.toString());
					} else {
						logger.info("will launch handbook: " + book.toString());
						if (Program.launch(book.toString()) == false) {
							try {
								logger.info("will exec handbook: " + book.toString());
								Runtime.getRuntime().exec(book.toString());
							} catch (Exception e) {
								ExHandler.handle(e);
							}
						}
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
					setActionDefinitionId(Hub.COMMAND_PREFIX + "login");} //$NON-NLS-1$
				
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
						ch.elexis.data.Anwender.logoff();
						
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
					setActionDefinitionId(Hub.COMMAND_PREFIX + "import");} //$NON-NLS-1$
				
				@Override
				public void run(){
					// cnv.open();
					Importer imp =
						new Importer(mainWindow.getShell(),
							ExtensionPointConstantsUi.FREMDDATENIMPORT);
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
					WizardDialog wd =
						new WizardDialog(mainWindow.getShell(), new DBConnectWizard());
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
						new EtiketteDruckenDialog(mainWindow.getShell(), kontakt, "AdressEtikette");
					dlg.setTitle(Messages.GlobalActions_PrintContactLabel);
					dlg.setMessage(Messages.GlobalActions_PrintContactLabelToolTip);
					if (isDirectPrint()) {
						dlg.setBlockOnOpen(false);
						dlg.open();
						if (dlg.doPrint()) {
							dlg.close();
						} else {
							SWTHelper
								.alert("Fehler beim Drucken",
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
						new EtiketteDruckenDialog(mainWindow.getShell(), actPatient,
							"AdressEtikette");
					dlg.setTitle(Messages.GlobalActions_PrintAddressLabel);
					dlg.setMessage(Messages.GlobalActions_PrintAddressLabelToolTip);
					if (isDirectPrint()) {
						dlg.setBlockOnOpen(false);
						dlg.open();
						if (dlg.doPrint()) {
							dlg.close();
						} else {
							SWTHelper
								.alert("Fehler beim Drucken",
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
					EtiketteDruckenDialog dlg =
						new EtiketteDruckenDialog(mainWindow.getShell(), actPatient,
							"PatientEtiketteAuftrag");
					dlg.setTitle(Messages.GlobalActions_PrintVersionedLabel);
					dlg.setMessage(Messages.GlobalActions_PrintVersionedLabelToolTip);
					if (isDirectPrint()) {
						dlg.setBlockOnOpen(false);
						dlg.open();
						if (dlg.doPrint()) {
							dlg.close();
						} else {
							SWTHelper
								.alert("Fehler beim Drucken",
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
						new EtiketteDruckenDialog(mainWindow.getShell(), actPatient,
							"PatientEtikette");
					dlg.setTitle(Messages.GlobalActions_PrintLabel);
					dlg.setMessage(Messages.GlobalActions_PrintLabelToolTip);
					if (isDirectPrint()) {
						dlg.setBlockOnOpen(false);
						dlg.open();
						if (dlg.doPrint()) {
							dlg.close();
						} else {
							SWTHelper
								.alert("Fehler beim Drucken",
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
					
					new TemplateDrucker("KG-Deckblatt", printer, tray).doPrint(actPatient); //$NON-NLS-1$
				}
			};
		printRoeBlatt = new Action(Messages.GlobalActions_PrintXRay) { //$NON-NLS-1$
				@Override
				public void run(){
					Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
					String printer = CoreHub.localCfg.get("Drucker/A4/Name", null); //$NON-NLS-1$
					String tray = CoreHub.localCfg.get("Drucker/A4/Schacht", null); //$NON-NLS-1$
					
					new TemplateDrucker("Roentgen-Blatt", printer, tray).doPrint(actPatient); //$NON-NLS-1$
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
					Mandant mnd = CoreHub.actMandant;
					if (actFall != null && mnd != null) {
						String rsId = mnd.getRechnungssteller().getId();
						Konsultation[] bhdl = actFall.getBehandlungen(false);
						ArrayList<Konsultation> lBehdl = new ArrayList<Konsultation>(bhdl.length);
						for (Konsultation b : bhdl) {
							Rechnung rn = b.getRechnung();
							if (rn == null) {
								if (b.getMandant().getRechnungssteller().getId().equals(rsId)) {
									lBehdl.add(b);
								}
							}
						}
						Result<Rechnung> res = Rechnung.build(lBehdl);
						if (!res.isOK()) {
							ErrorDialog.openError(mainWindow.getShell(),
								Messages.GlobalActions_Error, Messages //$NON-NLS-1$
								.GlobalActions_BillErrorMessage, ResultAdapter //$NON-NLS-1$
									.getResultAsStatus(res));
							// Rechnung rn=(Rechnung)res.get();
							// rn.storno(true);
							// rn.delete();
							
						}
					}
					// setFall(actFall,null);
				}
			};
		moveBehandlungAction = new Action(Messages.GlobalActions_AssignCase) { //$NON-NLS-1$
				@Override
				public void run(){
					// Object[] s=behandlViewer.getSelection();
					Konsultation k =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					if (k == null) {
						MessageDialog.openInformation(mainWindow.getShell(),
							Messages.GlobalActions_NoKonsSelected,
							Messages.GlobalActions_NoKonsSelectedMessage); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					
					SelectFallDialog dlg = new SelectFallDialog(mainWindow.getShell());
					if (dlg.open() == Dialog.OK) {
						Fall f = dlg.result;
						if (f != null) {
							k.setFall(f);
							ElexisEventDispatcher.fireSelectionEvent(f);
						}
					}
				}
			};
		redateAction = new Action(Messages.GlobalActions_Redate) { //$NON-NLS-1$
				@Override
				public void run(){
					Konsultation k =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					if (k == null) {
						MessageDialog.openInformation(mainWindow.getShell(),
							Messages.GlobalActions_NoKonsSelected,
							Messages.GlobalActions_NoKonsSelectedMessage); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					
					DateSelectorDialog dlg = new DateSelectorDialog(mainWindow.getShell());
					if (dlg.open() == Dialog.OK) {
						TimeTool date = dlg.getSelectedDate();
						k.setDatum(date.toString(TimeTool.DATE_GER), false);
						
						// notify listeners about change
						ElexisEventDispatcher.getInstance().fire(
							new ElexisEvent(k, k.getClass(), ElexisEvent.EVENT_UPDATE));
						
						ElexisEventDispatcher.fireSelectionEvent(k);
					}
				}
			};
		delFallAction = new Action(Messages.GlobalActions_DeleteCase) { //$NON-NLS-1$
				@Override
				public void run(){
					Fall actFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
					if ((actFall != null) && (actFall.delete(false) == false)) {
						SWTHelper.alert(Messages.GlobalActions_CouldntDeleteCaseMessage, //$NON-NLS-1$
							Messages.GlobalActions_CouldntDeleteCaseExplanation + //$NON-NLS-1$
								Messages.GlobalActions_93); //$NON-NLS-1$
					}
					ElexisEventDispatcher.reload(Fall.class);
				}
			};
		delKonsAction = new Action(Messages.GlobalActions_DeleteKons) { //$NON-NLS-1$
				@Override
				public void run(){
					Konsultation k =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					if ((k != null) && (k.delete(false) == false)) {
						SWTHelper.alert(Messages.GlobalActions_CouldntDeleteKons, //$NON-NLS-1$
							Messages.GlobalActions_CouldntDeleteKonsExplanation + //$NON-NLS-1$
								Messages.GlobalActions_97); //$NON-NLS-1$
					}
					ElexisEventDispatcher.clearSelection(Konsultation.class);
					if (k != null) {
						ElexisEventDispatcher.fireSelectionEvent(k.getFall());
					}
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
		reopenFallAction = new Action(Messages.GlobalActions_ReopenCase) { //$NON-NLS-1$
				@Override
				public void run(){
					Fall actFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
					if (actFall != null) {
						actFall.setEndDatum(""); //$NON-NLS-1$
					}
				}
			};
		neueKonsAction = new Action(Messages.GlobalActions_NewKons) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
					setToolTipText(Messages.GlobalActions_NewKonsToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					Konsultation.neueKons(null);
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
		//PrinterData pd = getPrinterData(Messages.getString("GlobalActions.printersticker")); //$NON-NLS-1$
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
		if (CoreHub.acl.request(AC_SHOWPERSPECTIVE) == true) {
			perspectiveList.setVisible(true);
		} else {
			perspectiveList.setVisible(false);
		}
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
			IHandlerService handlerService =
				(IHandlerService) part.getSite().getService(IHandlerService.class);
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
