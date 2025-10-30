/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * Portions (c) 2012, Joerg M. Sigle (js, jsigle)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - Declarative access to the contextMenu
 *******************************************************************************/

package ch.elexis.core.ui.contacts.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.Command;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.format.AddressFormatUtil;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.contacts.command.StickerFilterCommand;
import ch.elexis.core.ui.contacts.dialogs.PatientErfassenDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.tools.StringTool;

/**
 * Display of Patients
 *
 * @author gerry
 *
 */
public class PatientenListeView extends ViewPart implements IActivationListener, HeartListener {
	private CommonViewer cv;
	private ViewerConfigurer vc;
	private ViewMenus menus;
	private RestrictedAction newPatAction;
	// private IAction filterAction;
	private IAction copySelectedPatInfosToClipboardAction, copySelectedAddressesToClipboardAction;
	private String[] currentUserFields;
	// PatListFilterBox plfb;
	PatListeContentProvider plcp;
	DefaultControlFieldProvider dcfp;
	Composite parent;

	private boolean created = false;

	private IUser actUser;

	@Inject
	private IContextService contextService;

	@Inject
	void activeUser(@Optional IUser user) {
		actUser = user;
		if (created) {
			Display.getDefault().asyncExec(() -> {
				userChanged(user);
			});
		}
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IPatient.class.equals(clazz)) {
			if (created) {
				Display.getDefault().asyncExec(() -> {
					reload();
				});
			}
		}
	}

	@Override
	public void dispose() {
		plcp.stopListening();
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}

	/**
	 * retrieve the patient that is currently selected in the list
	 *
	 * @return the selected patient or null if none was selected
	 */
	public IPatient getSelectedPatient() {
		Object[] sel = cv.getSelection();
		if (sel != null) {
			return (IPatient) sel[0];
		}
		return null;
	}

	/**
	 * Refresh the contents of the list.
	 */
	public void reload() {
		plcp.invalidate();
		cv.notify(CommonViewer.Message.update);
	}

	@Override
	public void createPartControl(final Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		this.parent = parent;
		this.parent.setLayout(layout);

		cv = new CommonViewer();

		collectUserFields();
		plcp = new PatListeContentProvider(cv, currentUserFields, this);
		makeActions();

		dcfp = new DefaultControlFieldProvider(cv, currentUserFields) {
			@Override
			public void setQuery(IQuery<?> query) {
				for (int i = 0; i < dbFields.length; i++) {
					if (!lastFiltered[i].equals(StringTool.leer)) {
						if ("dob".equals(dbFields[i])) { //$NON-NLS-1$
							query.and(dbFields[i], COMPARATOR.LIKE, NoPoUtil.getElexisDateSearchString(lastFiltered[i]),
									true);
						} else if ("code".equals(dbFields[i])) {
							query.and(dbFields[i], COMPARATOR.EQUALS, lastFiltered[i]);
						} else {
							query.and(dbFields[i], COMPARATOR.LIKE, lastFiltered[i] + "%", true); //$NON-NLS-1$
						}
					}
				}
			}
		};
		updateFocusField();

		vc = new ViewerConfigurer(plcp, new PatLabelProvider(), dcfp, new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.SINGLE, cv));
		cv.create(vc, parent, SWT.NONE, getViewSite());
		// let user select patient by pressing ENTER in the control fields
		cv.getConfigurer().getControlFieldProvider().addChangeListener(new ControlFieldSelectionListener());
		cv.getViewerWidget().getControl().setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));

		plcp.startListening();
		GlobalEventDispatcher.addActivationListener(this, this);

		populateViewMenu();

		StructuredViewer viewer = cv.getViewerWidget();
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				PropertyDialogAction pdAction = new PropertyDialogAction(new SameShellProvider(parent),
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite()
								.getSelectionProvider());

				if (pdAction.isApplicableForSelection())
					pdAction.run();
			}
		});
		getSite().registerContextMenu(menus.getContextMenu(), viewer);
		getSite().setSelectionProvider(viewer);

		// start with filter disabled
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand(StickerFilterCommand.CMD_ID);
		if (command != null) {
			command.getState(StickerFilterCommand.STATE_ID).setValue(false);
		}

		// // ****DoubleClick Version Marlovits -> öffnet bei DoubleClick die
		// Patienten-Detail-Ansicht
		// cv.addDoubleClickListener(new DoubleClickListener() {
		// @Override
		// public void doubleClicked(PersistentObject obj, CommonViewer cv){
		// try {
		// PatientDetailView2 pdv =
		// (PatientDetailView2)
		// getSite().getPage().showView(PatientDetailView2.ID);
		// } catch (PartInitException e) {
		// ExHandler.handle(e);
		// }
		// }
		// });
		created = true;
	}

	private void updateFocusField() {
		String ff = ConfigServiceHolder.getUser(Preferences.USR_PATLIST_FOCUSFIELD, null);
		if (ff != null) {
			dcfp.setFocusField(ff);
		}
	}

	private void collectUserFields() {
		ArrayList<String> fields = new ArrayList<>();
		if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWPATNR, false)) {
			fields.add("code" + Query.EQUALS + Messages.PatientenListeView_PatientNr); // $NON-NLS-1$ //$NON-NLS-1$
		}
		if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWNAME, true)) {
			fields.add("description1" + Query.EQUALS + Messages.Core_Name); // $NON-NLS-1$ //$NON-NLS-1$
		}
		if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWFIRSTNAME, true)) {
			fields.add("description2" + Query.EQUALS + Messages.Core_Firstname); // $NON-NLS-1$ //$NON-NLS-1$
		}
		if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWDOB, true)) {
			fields.add("dob" + Query.EQUALS + Messages.Core_Enter_Birthdate); // $NON-NLS-1$ //$NON-NLS-1$
		}
		currentUserFields = fields.toArray(new String[fields.size()]);
	}

	private void populateViewMenu() {
		menus = new ViewMenus(getViewSite());

		menus.createToolbar(newPatAction); // TODO filterAction ?

		menus.createToolbar(copySelectedPatInfosToClipboardAction);
		menus.createToolbar(copySelectedAddressesToClipboardAction);

		PatientMenuPopulator pmp = new PatientMenuPopulator(this, cv.getViewerWidget());
		menus.createControlContextMenu(cv.getViewerWidget().getControl(), pmp);
		menus.getContextMenu().addMenuListener(pmp);

		menus.createMenu(newPatAction); // TODO filterAction ?
		menus.createMenu(copySelectedPatInfosToClipboardAction);
		menus.createMenu(copySelectedAddressesToClipboardAction);
	}

	public PatListeContentProvider getContentProvider() {
		return plcp;
	}

	@Override
	public void setFocus() {
		vc.getControlFieldProvider().setFocus();
	}

	class PatLabelProvider extends DefaultLabelProvider implements ITableColorProvider {

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			if (element instanceof IPatient) {
				IPatient pat = (IPatient) element;

				if (contextService.getActiveMandator().isPresent()) {
					if (!Reminder
							.findRemindersDueFor(Patient.load(pat.getId()),
									Anwender.load(contextService.getActiveMandator().get().getId()), false).isEmpty()) {
						return Images.IMG_AUSRUFEZ.getImage();
					}
				}

				ISticker sticker = StickerServiceHolder.get().getSticker(pat).orElse(null);
				if (sticker != null && sticker.getImage() != null) {
					return CoreUiUtil.getImageAsIcon(sticker.getImage());
				} else {
					if (pat.getGender().equals(Gender.MALE)) {
						return Images.IMG_MANN.getImage();
					} else {
						return Images.IMG_FRAU.getImage();
					}
				}
			} else {
				return super.getColumnImage(element, columnIndex);
			}
		}

		@Override
		public Color getBackground(final Object element, final int columnIndex) {
			if (element instanceof IPatient) {
				IPatient pat = (IPatient) element;
				ISticker et = StickerServiceHolder.get().getSticker(pat).orElse(null);
				if (et != null && et.getBackground() != null) {
					return CoreUiUtil.getColorForString(et.getBackground());
				}
			}
			return null;
		}

		@Override
		public Color getForeground(final Object element, final int columnIndex) {
			if (element instanceof IPatient) {
				IPatient pat = (IPatient) element;
				ISticker et = StickerServiceHolder.get().getSticker(pat).orElse(null);
				if (et != null && et.getForeground() != null) {
					return CoreUiUtil.getColorForString(et.getForeground());
				}
			}

			return null;
		}

	}

	public void reset() {
		vc.getControlFieldProvider().clearValues();
	}

	private void makeActions() {

		newPatAction = new RestrictedAction(EvACE.of(IPatient.class, Right.CREATE),
				Messages.PatientenListeView_NewPatientAction) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.PatientenListeView_NewPationtToolTip);
			}

			@Override
			public void doRun() {
				HashMap<String, String> ctlFields = new HashMap<>();
				String[] fx = vc.getControlFieldProvider().getValues();
				int i = 0;
				if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWPATNR, false)) {
					if (i < fx.length) {
						ctlFields.put(Patient.FLD_PATID, fx[i++]);
					}
				}
				if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWNAME, true)) {
					if (i < fx.length) {
						ctlFields.put(Patient.FLD_NAME, fx[i++]);
					}
				}
				if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWFIRSTNAME, true)) {
					if (i < fx.length) {
						ctlFields.put(Patient.FLD_FIRSTNAME, fx[i++]);
					}
				}
				if (ConfigServiceHolder.getUser(Preferences.USR_PATLIST_SHOWDOB, true)) {
					if (i < fx.length) {
						ctlFields.put(Patient.FLD_DOB, fx[i++]);
					}
				}

				PatientErfassenDialog ped = new PatientErfassenDialog(getViewSite().getShell(), ctlFields);
				if (ped.open() == Dialog.OK) {
					plcp.temporaryAddObject(ped.getResult());
					IPatient pat = ped.getResult();
					for (int j = 0; j < currentUserFields.length; j++) {
						String current = currentUserFields[j];
						if (current.startsWith(Patient.FLD_PATID)) {
							dcfp.setValue(j, pat.getPatientNr());
						} else if (current.startsWith(Patient.FLD_NAME) && pat.getLastName() != null) {
							dcfp.setValue(j, pat.getLastName());
						} else if (current.startsWith(Patient.FLD_FIRSTNAME) && pat.getFirstName() != null) {
							dcfp.setValue(j, pat.getFirstName());
						}
					}
					plcp.syncRefresh();
					TableViewer tv = (TableViewer) cv.getViewerWidget();
					tv.setSelection(new StructuredSelection(pat), true);
					ContextServiceHolder.get().setActivePatient(pat);
				}
			}
		};

		/*
		 * Copy selected PatientInfos to the clipboard, so it/they can be easily pasted
		 * into a letter for printing. An action with identical / similar code has also
		 * been added above, and to KontakteView.java. Detailed comments regarding field
		 * access, and output including used newline/cr characters are maintained only
		 * there.
		 */
		copySelectedPatInfosToClipboardAction = new Action(
				Messages.PatientenListeView_copySelectedPatInfosToClipboard) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
				setToolTipText(Messages.PatientenListeView_copySelectedPatInfosToClipboard); // $NON-NLS-1$
			}

			@Override
			public void run() {

				StringJoiner sj = new StringJoiner(System.lineSeparator());
				Object[] sel = cv.getSelection();
				if (sel != null && sel.length > 0) {
					for (int i = 0; i < sel.length; i++) {
						StringBuilder sb = new StringBuilder();
						IPatient selectedPatient = (IPatient) sel[i];
						if (selectedPatient.isPerson()) {
							sb.append(AddressFormatUtil.getSingleLine(selectedPatient));
						} else {
							sb.append(
									"Fehler: Bei diesem Patienten ist das Flag \"Person\" nicht gesetzt! Bitte korrigieren!\n");
						}
						sj.add(sb.toString());
					}

					Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					Transfer[] transfers = new Transfer[] { textTransfer };
					Object[] data = new Object[] { sj.toString() };
					clipboard.setContents(data, transfers);
					clipboard.dispose();

				}
			};
		};

		/*
		 * Copy selected address(es) to the clipboard, so it/they can be easily pasted
		 * into a letter for printing. An actions with identical / similar code has also
		 * been added below, and to KontakteView.java. Detailed comments regarding field
		 * access, and output including used newline/cr characters are maintained only
		 * there.
		 */
		copySelectedAddressesToClipboardAction = new Action(
				Messages.PatientenListeView_copySelectedAddressesToClipboard) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
				setToolTipText(Messages.PatientenListeView_copySelectedAddressesToClipboard); // $NON-NLS-1$
			}

			@Override
			public void run() {

				StringJoiner sj = new StringJoiner(System.lineSeparator());
				Object[] sel = cv.getSelection();
				if (sel != null && sel.length > 0) {
					for (int i = 0; i < sel.length; i++) {
						StringBuilder sb = new StringBuilder();
						IPatient selectedPatient = (IPatient) sel[i];
						sb.append(AddressFormatUtil.getAddressPhoneFaxEmail(selectedPatient, true, true));
						sj.add(sb.toString());
					}

					Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					Transfer[] transfers = new Transfer[] { textTransfer };
					Object[] data = new Object[] { sj.toString() };
					clipboard.setContents(data, transfers);
					clipboard.dispose();
				}
			};
		};
	}

	@Override
	public void activation(final boolean mode) {
		if (mode == true) {
			newPatAction.reflectRight();
			heartbeat();
			CoreHub.heart.addListener(this);
		} else {
			CoreHub.heart.removeListener(this);
		}
	}

	@Override
	public void visible(final boolean mode) {
		// TODO Auto-generated method stub
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void heartbeat() {
		cv.notify(CommonViewer.Message.update);
	}

	/**
	 * Select Patient when user presses ENTER in the control fields. If mor than one
	 * Patients are listed, the first one is selected. (This listener only
	 * implements selected().)
	 */
	class ControlFieldSelectionListener implements ControlFieldListener {
		@Override
		public void changed(HashMap<String, String> values) {
			// nothing to do (handled by LazyContentProvider)
		}

		@Override
		public void reorder(final String field) {
			// nothing to do (handled by LazyContentProvider)
		}

		/**
		 * ENTER has been pressed in the control fields, select the first listed patient
		 */
		// this is also implemented in KontakteView
		@Override
		public void selected() {
			StructuredViewer viewer = cv.getViewerWidget();
			Object[] elements = cv.getConfigurer().getContentProvider().getElements(viewer.getInput());
			if ((elements != null) && (elements.length > 0)) {
				Object element = elements[0];
				if (element instanceof IPatient) {
					ContextServiceHolder.get().getRootContext().setTyped((IPatient) element);
				}
			}
		}
	}

	public void userChanged(IUser user) {
		if (!Objects.equals(actUser, user)) {
			SWTHelper.reloadViewPart(UiResourceConstants.PatientenListeView_ID);
		}
		if (!cv.getViewerWidget().getControl().isDisposed()) {
			cv.getViewerWidget().getControl().setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
			cv.notify(CommonViewer.Message.update);

			collectUserFields();
			dcfp.updateFields(currentUserFields, true);
			plcp.updateFields(currentUserFields);

			updateFocusField();
			dcfp.getParent().layout(true);
		}
	}
}
