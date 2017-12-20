/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.ReminderDetailDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonContentProviderAdapter;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.reminder.ReminderStatusSubMenu;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.io.Settings;
import ch.rgw.tools.TimeTool;

public class ReminderView extends ViewPart implements IActivationListener, HeartListener {
	
	public static final String ID = "ch.elexis.reminderview"; //$NON-NLS-1$
	
	private IAction newReminderAction, deleteReminderAction, showOnlyOwnDueReminderToggleAction,
			showSelfCreatedReminderAction, toggleAutoSelectPatientAction, reloadAction,
			showOnlyFromCurrentPatientReminderAction, sortByDueDate;
	private RestrictedAction showOthersRemindersAction;
	private RestrictedAction selectPatientAction;
	private boolean bVisible;
	
	private ReminderLabelProvider reminderLabelProvider = new ReminderLabelProvider();
	
	private IAction filterActionType[] = new IAction[Type.values().length];
	private Set<Integer> filterActionSet = new HashSet<Integer>();
	
	private long cvHighestLastUpdate = 0l;
	
	private boolean autoSelectPatient =
		CoreHub.userCfg.get(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false);
	private boolean showOnlyDueReminders =
		CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, false);
	private boolean showAllReminders = (CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false)
		&& CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS));
	private boolean showSelfCreatedReminders =
		CoreHub.userCfg.get(Preferences.USR_REMINDEROWN, false);
	
	private CommonViewer cv = new CommonViewer();
	private ViewerConfigurer vc;
	private Query<Reminder> qbe;
	private ReminderFilter filter = new ReminderFilter();
	private Patient actPatient;
	private Text txtSearch;
	
	private ElexisEventListener eeli_reminder = new ElexisUiEventListenerImpl(Reminder.class,
		ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_UPDATE) {
		public void catchElexisEvent(ElexisEvent ev){
			cv.notify(CommonViewer.Message.update);
		}
	};
	
	// 1079 - nur wenn der View offen ist werden bei Patienten-Wechsel die Reminders abgefragt!
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		
		public void runInUi(final ElexisEvent ev){
			if (((Patient) ev.getObject()).equals(actPatient)) {
				return;
			}
			actPatient = (Patient) ev.getObject();
			// clear selection before update
			cv.getViewerWidget().setSelection(StructuredSelection.EMPTY);
			
			if (bVisible) {
				cv.notify(CommonViewer.Message.update);
			}
			/**
			 * ch.elexis.core.data.events.PatientEventListener will be called on opposite
			 * Preferences.USR_SHOWPATCHGREMINDER condition.
			 */
			if (!CoreHub.userCfg.get(Preferences.USR_SHOWPATCHGREMINDER, true)) {
				UiDesk.asyncExec(new Runnable() {
					
					public void run(){
						List<Reminder> list = Reminder.findOpenRemindersResponsibleFor(
							CoreHub.actUser, false, (Patient) ev.getObject(), true);
						if (list.size() != 0) {
							StringBuilder sb = new StringBuilder();
							for (Reminder r : list) {
								sb.append(r.getSubject() + "\n");
								sb.append(r.getMessage() + "\n\n");
							}
							SWTHelper.alert(Messages.ReminderView_importantRemindersCaption,
								sb.toString());
						}
					}
				});
			}
		}
	};
	
	private ElexisEventListener eeli_user =
		new ElexisUiEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			
			public void runInUi(ElexisEvent ev){
				refreshUserConfiguration();
				
				if (bVisible) {
					cv.notify(CommonViewer.Message.update);
				}
				
			}
		};
	
	public ReminderView(){
		qbe = new Query<>(Reminder.class, null, null, Reminder.TABLENAME, new String[] {
			Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE
		});
	}
	
	@Override
	public void createPartControl(final Composite parent){
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		Composite header = new Composite(content, SWT.NONE);
		GridLayout gl_header = new GridLayout(2, false);
		gl_header.horizontalSpacing = 0;
		gl_header.marginWidth = 0;
		gl_header.marginHeight = 0;
		header.setLayout(gl_header);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtSearch = new Text(header, SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.setMessage(Messages.ReminderView_txtSearch_message);
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				filter.setFilterText(txtSearch.getText());
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		});
		
		Label btnClear = new Label(header, SWT.NONE);
		btnClear.setImage(Images.IMG_CLEAR.getImage());
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e){
				txtSearch.setText(StringConstants.EMPTY);
				filter.setFilterText(null);
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		});
		
		reminderLabelProvider.updateUserConfiguration();
		
		ReminderViewCommonContentProvider contentProvider = new ReminderViewCommonContentProvider();
		vc = new ViewerConfigurer(contentProvider, reminderLabelProvider, null,
			new ViewerConfigurer.DefaultButtonProvider(),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.MULTI, cv));
		
		makeActions(contentProvider);
		
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(reloadAction, newReminderAction, toggleAutoSelectPatientAction);
		menu.createMenu(createActionList());
		
		if (CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS)) {
			showOthersRemindersAction.setEnabled(true);
			showOthersRemindersAction
				.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false));
		} else {
			showOthersRemindersAction.setEnabled(false);
		}
		cv.create(vc, content, SWT.NONE, getViewSite());
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
			public void doubleClicked(final PersistentObject obj, final CommonViewer cv){
				Reminder reminder = (Reminder) obj;
				AcquireLockBlockingUi.aquireAndRun(reminder, new ILockHandler() {
					@Override
					public void lockAcquired(){
						ReminderDetailDialog rdd =
							new ReminderDetailDialog(UiDesk.getTopShell(), (Reminder) obj);
						int retVal = rdd.open();
						if (retVal == Dialog.OK) {
							ElexisEventDispatcher.getInstance().fire(
								new ElexisEvent(reminder, getClass(), ElexisEvent.EVENT_UPDATE));
						}
					}
					
					@Override
					public void lockFailed(){
						cv.notify(CommonViewer.Message.update);
					}
				});
				
			}
		});
		
		List<IContributionItem> popupMenuActionList = new ArrayList<>();
		popupMenuActionList.add(new ReminderStatusSubMenu(cv));
		popupMenuActionList.add(new ActionContributionItem(deleteReminderAction));
		popupMenuActionList.add(new ActionContributionItem(selectPatientAction));
		popupMenuActionList.add(new Separator());
		popupMenuActionList.addAll(createActionList());
		menu.createViewerContextMenu(cv.getViewerWidget(), popupMenuActionList);
		cv.getViewerWidget().addFilter(filter);
		GlobalEventDispatcher.addActivationListener(this, getViewSite().getPart());
		
		cv.getViewerWidget().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectPatientAction.setEnabled(selection.size() <= 1);
				selectPatientAction.reflectRight();
				if (autoSelectPatient && selectPatientAction.isEnabled()) {
					selectPatientAction.doRun();
				}
			}
		});
	}
	
	private List<IContributionItem> createActionList(){
		Action labelFilter = new Action("Anzeige filtern") {};
		labelFilter.setEnabled(false);
		
		Action labelResponsibility = new Action("Anzeige erweitern") {};
		labelResponsibility.setEnabled(false);
		
		Action labelSorter = new Action("Anzeige sortieren") {};
		labelSorter.setEnabled(false);
		
		MenuManager typeFilterSubMenu = new MenuManager("Nach Aktionstyp");
		List<IContributionItem> ca =
			ViewMenus.convertActionsToContributionItems((filterActionType));
		for (IContributionItem iContributionItem : ca) {
			typeFilterSubMenu.add(iContributionItem);
		}
		return Arrays.asList(new ActionContributionItem(newReminderAction), null,
			new ActionContributionItem(labelSorter), new ActionContributionItem(sortByDueDate),
			null, new ActionContributionItem(labelFilter),
			new ActionContributionItem(showOnlyOwnDueReminderToggleAction), typeFilterSubMenu, null,
			new ActionContributionItem(labelResponsibility),
			new ActionContributionItem(showSelfCreatedReminderAction),
			new ActionContributionItem(showOthersRemindersAction),
			new ActionContributionItem(showOnlyFromCurrentPatientReminderAction),
			null);
	}
	
	private void refreshUserConfiguration(){
		boolean bChecked = CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, true);
		showOnlyOwnDueReminderToggleAction.setChecked(bChecked);
		showSelfCreatedReminderAction
			.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROWN, false));
		toggleAutoSelectPatientAction
			.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false));

		// get state from user's configuration
		showOthersRemindersAction
			.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false));
		showOnlyFromCurrentPatientReminderAction.setChecked(
					CoreHub.userCfg.get(Preferences.USR_REMINDERS_SHOW_ONLY_FROM_CURRENT_PATIENT, false));
		
		// update action's access rights
		showOthersRemindersAction.reflectRight();
		
		reminderLabelProvider.updateUserConfiguration();
	}
	
	@Override
	public void setFocus(){}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, getViewSite().getPart());
		CoreHub.userCfg.set(Preferences.USR_REMINDERSOPEN,
			showOnlyOwnDueReminderToggleAction.isChecked());
	}
	
	private Images determineActionTypeImage(Type actionType){
		switch (actionType) {
		case PRINT:
		case PRINT_DRUG_STICKER:
			return Images.IMG_PRINTER;
		case MAKE_APPOINTMENT:
			return Images.IMG_CALENDAR;
		case DISPENSE_MEDICATION:
			return Images.IMG_PILL;
		case PROCESS_SERVICE_RECORDING:
			return Images.IMG_MONEY;
		case CHECK_LAB_RESULT:
		case READ_DOCUMENT:
			return Images.IMG_EYE_WO_SHADOW;
		case SEND_DOCUMENT:
			return Images.IMG_MAIL_SEND;
		default:
			return Images.IMG_EMPTY_TRANSPARENT;
		}
	}
	
	private void makeActions(final ReminderViewCommonContentProvider contentProvider){
		newReminderAction = new Action(Messages.ReminderView_newReminderAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.ReminderView_newReminderToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				ReminderDetailDialog erd = new ReminderDetailDialog(getViewSite().getShell(), null);
				int retVal = erd.open();
				if (retVal == Dialog.OK) {
					Reminder reminder = erd.getReminder();
					CoreHub.getLocalLockService().acquireLock(reminder);
					CoreHub.getLocalLockService().releaseLock(reminder);
				}
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		};
		deleteReminderAction = new Action(Messages.ReminderView_deleteAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.ReminderView_deleteToolTip);
			}
			
			@Override
			public void run(){
				Object[] selections = cv.getSelection();
				if (selections != null && selections.length == 1
					&& selections[0] instanceof Reminder) {
					Reminder r = (Reminder) selections[0];
					LockResponse lockResponse = CoreHub.getLocalLockService().acquireLock(r);
					if (lockResponse.isOk()) {
						r.delete();
						CoreHub.getLocalLockService().releaseLock(r);
					} else {
						LockResponseHelper.showInfo(lockResponse, r, null);
					}
					
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			}
			
			@Override
			public boolean isEnabled(){
				Object[] sel = cv.getSelection();
				return (sel != null && sel.length == 1 && sel[0] instanceof Reminder);
			}
		};
		sortByDueDate = new Action(Messages.ReminderView_sortByDueDate, Action.AS_CHECK_BOX) {
			
			int state = 0;
			
			@Override
			public void run(){
				
				if (state == 0) {
					contentProvider.setComparator(new Comparator<Reminder>() {
						@Override
						public int compare(Reminder o1, Reminder o2){
							return TimeTool.compare(o2.getDateDue(), o1.getDateDue());
						}
					});
					state = 1;
					sortByDueDate.setText(Messages.ReminderView_sortByDueDateAscending);
					sortByDueDate.setChecked(true);
				} else if (state == 1) {
					contentProvider.setComparator(new Comparator<Reminder>() {
						@Override
						public int compare(Reminder o1, Reminder o2){
							return TimeTool.compare(o1.getDateDue(), o2.getDateDue());
						}
					});
					state = 2;
					sortByDueDate.setText(Messages.ReminderView_sortByDueDateDescending);
					sortByDueDate.setChecked(true);
				} else if (state == 2) {
					contentProvider.setComparator(null);
					state = 0;
					sortByDueDate.setText(Messages.ReminderView_sortByDueDate);
					sortByDueDate.setChecked(false);
				}
				
				cv.notify(CommonViewer.Message.update_keeplabels);
			}
		};
		
		showOnlyOwnDueReminderToggleAction =
			new Action(Messages.ReminderView_onlyDueAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setToolTipText(Messages.ReminderView_onlyDueToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					showOnlyDueReminders = showOnlyOwnDueReminderToggleAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDERSOPEN, showOnlyDueReminders);
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};
		showSelfCreatedReminderAction =
			new Action(Messages.ReminderView_myRemindersAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setToolTipText(Messages.ReminderView_myRemindersToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					showSelfCreatedReminders = showSelfCreatedReminderAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDEROWN, showSelfCreatedReminders);
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};
		showOthersRemindersAction =
			new RestrictedAction(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS,
				Messages.ReminderView_foreignAction, Action.AS_CHECK_BOX) {
				{
					setToolTipText(Messages.ReminderView_foreignTooltip);
					setImageDescriptor(Images.IMG_ACHTUNG.getImageDescriptor());
				}
				
				@Override
				public void doRun(){
					showAllReminders = showOthersRemindersAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDEROTHERS, showAllReminders);
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};

			showOnlyFromCurrentPatientReminderAction =
				new Action(Messages.ReminderView_overrideShowOnlyFromCurrentPatientReminderAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
					{
						setToolTipText(Messages.ReminderView_overrideShowOnlyFromCurrentPatientReminderToolTip); //$NON-NLS-1$
					}

					@Override
					public void run(){
						boolean bChecked = showOnlyFromCurrentPatientReminderAction.isChecked();
						CoreHub.userCfg.set(Preferences.USR_REMINDERS_SHOW_ONLY_FROM_CURRENT_PATIENT, bChecked);
						cv.notify(CommonViewer.Message.update_keeplabels);
					}
				};

		selectPatientAction = new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY,
			Messages.ReminderView_activatePatientAction, Action.AS_UNSPECIFIED) {
			{
				setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
				setToolTipText(Messages.ReminderView_activatePatientTooltip);
			}
			
			public void doRun(){
				Object[] sel = cv.getSelection();
				if (sel != null && sel.length > 1) {
					SWTHelper.showInfo(Messages.ReminderView_onePatOnly,
						Messages.ReminderView_onlyOnePatientForActivation);
				} else if (sel != null && sel.length > 0) {
					Reminder reminder = (Reminder) sel[0];
					Patient patient = reminder.getKontakt();
					Anwender creator = reminder.getCreator();
					if (patient != null) {
						if (!patient.getId().equals(creator.getId())) {
							ElexisEventDispatcher.fireSelectionEvent(patient);
						}
					}
				}
			}
			
			@Override
			public boolean isEnabled(){
				Object[] sel = cv.getSelection();
				if (sel != null && sel.length == 1 && sel[0] instanceof Reminder) {
					Reminder reminder = (Reminder) sel[0];
					return reminder.isPatientRelated();
				}
				return false;
			}
		};
		
		toggleAutoSelectPatientAction =
			new Action(Messages.ReminderView_activatePatientAction, Action.AS_CHECK_BOX) {
				{
					setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
					setToolTipText(Messages.ReminderView_toggleSelectPatientActionTooltip);
					setChecked(autoSelectPatient);
				}
				
				@Override
				public void run(){
					autoSelectPatient = toggleAutoSelectPatientAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT,
						autoSelectPatient);
				}
			};
		
		reloadAction = new Action(Messages.PatHeuteView_reloadAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
				setToolTipText(Messages.PatHeuteView_reloadToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				heartbeat();
			}
		};
		
		for (int i = 0; i < Type.values().length; i++) {
			Type type = Type.values()[i];
			filterActionType[i] = new Action(type.getLocaleText(), Action.AS_CHECK_BOX) {
				@Override
				public void run(){
					if (isChecked()) {
						filterActionSet.add(type.numericValue());
					} else {
						filterActionSet.remove(type.numericValue());
					}
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};
			filterActionType[i].setChecked(filterActionSet.contains(type.numericValue()));
			filterActionType[i]
				.setImageDescriptor(determineActionTypeImage(type).getImageDescriptor());
		}
	}
	
	public void activation(final boolean mode){
		/* egal */
	}
	
	public void visible(final boolean mode){
		bVisible = mode;
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_user, eeli_reminder);
			CoreHub.heart.addListener(this);
			heartbeat();
			refreshUserConfiguration();
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_user, eeli_reminder);
			CoreHub.heart.removeListener(this);
		}
	}
	
	public void heartbeat(){
		long highestLastUpdate = PersistentObject.getHighestLastUpdate(Reminder.TABLENAME);
		if (highestLastUpdate > cvHighestLastUpdate) {
			cv.notify(CommonViewer.Message.update);
			cvHighestLastUpdate = highestLastUpdate;
		}
	}
	
	private class ReminderLabelProvider extends DefaultLabelProvider
			implements IColorProvider, IFontProvider {
		
		private Font boldFont;
		private Color colorDue;
		private Color colorOverdue;
		private Color colorOpen;
		
		public Color getBackground(final Object element){
			if (element instanceof Reminder) {
				Reminder reminder = (Reminder) element;
				switch (reminder.getDueState()) {
				case 1:
					return colorDue;
				case 2:
					return colorOverdue;
				default:
					ProcessStatus processStatus = reminder.getProcessStatus();
					if (ProcessStatus.OPEN == processStatus) {
						return colorOpen;
					}
					return null;
				}
			}
			return UiDesk.getColor(UiDesk.COL_BLACK);
		}
		
		public void updateUserConfiguration(){
			Settings cfg = CoreHub.userCfg.getBranch(Preferences.USR_REMINDERCOLORS, true);
			colorDue = UiDesk.getColorFromRGB(cfg.get(ProcessStatus.DUE.getLocaleText(), "FFFFFF")); //$NON-NLS-1$;
			colorOverdue =
				UiDesk.getColorFromRGB(cfg.get(ProcessStatus.OVERDUE.getLocaleText(), "FF0000")); //$NON-NLS-1$
			colorOpen =
				UiDesk.getColorFromRGB(cfg.get(ProcessStatus.OPEN.getLocaleText(), "00FF00")); //$NON-NLS-1$
		}
		
		public Color getForeground(final Object element){
			if (element instanceof Reminder) {
				Reminder reminder = (Reminder) element;
				Priority prio = reminder.getPriority();
				if (Priority.LOW == prio) {
					return UiDesk.getColor(UiDesk.COL_GREY);
				}
				return null;
			}
			
			return UiDesk.getColor(UiDesk.COL_WHITE);
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex){
			if (element instanceof Reminder) {
				Reminder reminder = (Reminder) element;
				
				ProcessStatus status = reminder.getStatus();
				if (ProcessStatus.CLOSED == status) {
					return Images.IMG_TICK.getImage();
				}
				
				Type actionType = reminder.getActionType();
				return determineActionTypeImage(actionType).getImage();
			}
			return null;
		}
		
		@Override
		public String getColumnText(Object element, int column){
			if (element instanceof Reminder) {
				Reminder reminder = (Reminder) element;
				ProcessStatus status = reminder.getStatus();
				if (ProcessStatus.ON_HOLD == status) {
					return "[" + status.getLocaleText() + "] " + super.getText(element);
				}
			}
			return super.getText(element);
		}
		
		@Override
		public Font getFont(Object element){
			if (element instanceof Reminder) {
				Reminder reminder = (Reminder) element;
				if (boldFont == null) {
					Display disp = Display.getCurrent();
					Font defaultFont = cv.getViewerWidget().getControl().getFont();
					FontDescriptor boldDescriptor =
						FontDescriptor.createFrom(defaultFont).setStyle(SWT.BOLD);
					boldFont = boldDescriptor.createFont(disp);
				}
				Priority prio = reminder.getPriority();
				if (Priority.HIGH == prio) {
					return boldFont;
				}
			}
			return null;
		}
	}
	
	private class ReminderFilter extends ViewerFilter {
		
		private String filterText;
		
		@Override
		public boolean select(final Viewer viewer, final Object parentElement,
			final Object element){
			
			if (element instanceof Reminder) {
				Reminder check = (Reminder) element;
				if (showOnlyOwnDueReminderToggleAction.isChecked()) {
					int determineDueState = Reminder.determineDueState(check.getDateDue());
					if (determineDueState == 0) {
						return false;
					}
				}
				Patient act = ElexisEventDispatcher.getSelectedPatient();
				String patientId = (act != null) ? act.getId() : "INVALID_ID"; //$NON-NLS-1$
				patientId = (act != null) ? act.getId() : "INVALID_ID"; //$NON-NLS-1$
				if (!check.get("IdentID").equals(patientId) && showOnlyFromCurrentPatientReminderAction.isChecked()) { //$NON-NLS-1$
					return false;
				}
				String[] vals = check.get(true, Reminder.FLD_SUBJECT, Reminder.FLD_MESSAGE,
					Reminder.FLD_KONTAKT_ID, Reminder.FLD_VISIBILITY);
				if (!vals[2].equals(patientId)) {
					Visibility vis = Visibility.byNumericSafe(vals[3]);
					if (vis != Visibility.ALWAYS && vis != Visibility.POPUP_ON_LOGIN ) {
						// other (non-selected patient) and not marked always visible
						return false;
					}
				}
				
				if (filterText != null && filterText.length() > 0) {
					if (!StringUtils.containsIgnoreCase(vals[0], filterText)
						&& !StringUtils.containsIgnoreCase(vals[1], filterText)) {
						return false;
					}
				}
			}
			return true;
		}
		
		public void setFilterText(String text){
			filterText = text;
		}
	}
	
	private class ReminderViewCommonContentProvider extends CommonContentProviderAdapter {
		
		private Comparator<Reminder> comparator;
		
		@Override
		public Object[] getElements(final Object inputElement){
			// Display reminders only if one is logged in
			if (CoreHub.actUser == null) {
				return new Object[0];
			}
			
			SortedSet<Reminder> reminders = new TreeSet<Reminder>();
			
			if (showAllReminders
				&& CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS)) {
				qbe.clear();
				reminders.addAll(qbe.execute());
			} else {
				reminders.addAll(Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser,
					showOnlyDueReminders, null, false));
				
				if (showSelfCreatedReminders) {
					qbe.clear();
					qbe.add(Reminder.FLD_CREATOR, Query.EQUALS, CoreHub.actUser.getId());
					
					reminders.addAll(qbe.execute());
				}
			}
			
			if (filterActionSet.size() > 0) {
				reminders
					.removeIf(p -> !(filterActionSet.contains(p.getActionType().numericValue())));
			}
			
			// split into sublists
			List<Reminder> patientRelatedReminders = new ArrayList<>();
			List<Reminder> patientRelatedRemindersCurrentPatient = new ArrayList<>();
			List<Reminder> otherReminders = new ArrayList<>();
			
			Patient currentPatient = ElexisEventDispatcher.getSelectedPatient();
			for (Reminder reminder : reminders) {
				if (reminder.isPatientRelated()) {
					if (currentPatient != null
						&& reminder.get(Reminder.FLD_KONTAKT_ID).equals(currentPatient.getId())) {
						patientRelatedRemindersCurrentPatient.add(reminder);
					} else {
						patientRelatedReminders.add(reminder);
					}
				} else {
					otherReminders.add(reminder);
				}
			}
			
			if (comparator != null) {
				Collections.sort(patientRelatedReminders, comparator);
				Collections.sort(patientRelatedRemindersCurrentPatient, comparator);
				Collections.sort(otherReminders, comparator);
			}
			
			List<Object> resultList = new ArrayList<>();
			resultList.add("------------ Aktueller Patient");
			resultList.addAll(patientRelatedRemindersCurrentPatient);
			resultList.add("------------ Allgemein");
			resultList.addAll(otherReminders);
			resultList
				.add("------------ Patientenbezogen (nicht aktueller Patient, immer anzeigen)");
			resultList.addAll(patientRelatedReminders);
			
			return resultList.toArray();
		}
		
		public void setComparator(Comparator<Reminder> comparator){
			this.comparator = comparator;
		}
		
	}
}
