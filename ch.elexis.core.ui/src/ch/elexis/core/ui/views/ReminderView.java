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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
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
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.io.Settings;
import ch.rgw.tools.TimeTool;

public class ReminderView extends ViewPart implements IActivationListener, HeartListener {
	public static final String ID = "ch.elexis.reminderview"; //$NON-NLS-1$
	
	private IAction newReminderAction, deleteReminderAction, onlyOpenReminderAction,
			ownReminderAction, toggleAutoSelectPatientAction;
	private RestrictedAction othersReminderAction;
	private RestrictedAction selectPatientAction;
	private boolean bVisible;
	private boolean autoSelectPatient;
	
	private ReminderLabelProvider reminderLabelProvider = new ReminderLabelProvider();
	
	private IAction filterActionType[] = new IAction[Type.values().length];
	private Set<Integer> filterActionSet = new HashSet<Integer>();
	
	// 1079 - nur wenn der View offen ist werden bei Patienten-Wechsel die Reminders abgefragt!
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		
		public void runInUi(final ElexisEvent ev){
			if (((Patient) ev.getObject()).equals(actPatient)) {
				return;
			}
			actPatient = (Patient) ev.getObject();
			if (bVisible) {
				cv.notify(CommonViewer.Message.update);
			}
			
			if (!CoreHub.userCfg.get(Preferences.USR_SHOWPATCHGREMINDER, true)) {
				UiDesk.asyncExec(new Runnable() {
					
					public void run(){
						List<Reminder> list = Reminder.findRemindersDueFor((Patient) ev.getObject(),
							CoreHub.actUser, true);
						if (list.size() != 0) {
							StringBuilder sb = new StringBuilder();
							for (Reminder r : list) {
								sb.append(r.getMessage()).append("\n\n"); //$NON-NLS-1$
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
				boolean bChecked = CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, true);
				onlyOpenReminderAction.setChecked(bChecked);
				ownReminderAction
					.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROWN, false));
				
				// get state from user's configuration
				othersReminderAction
					.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false));
				
				// update action's access rights
				othersReminderAction.reflectRight();
				
				reminderLabelProvider.updateUserConfiguration();
				
				if (bVisible) {
					cv.notify(CommonViewer.Message.update);
				}
				
			}
		};
	
	private ElexisEventListener eeli_reminder = new ElexisUiEventListenerImpl(Reminder.class,
		ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_UPDATE) {
		public void catchElexisEvent(ElexisEvent ev){
			cv.notify(CommonViewer.Message.update);
		}
	};
	
	final CommonViewer cv;
	ViewerConfigurer vc;
	Query<Reminder> qbe;
	Settings cfg;
	ReminderFilter filter;
	private Patient actPatient;
	
	public ReminderView(){
		cv = new CommonViewer();
		qbe = new Query<Reminder>(Reminder.class);
		Patient.load("0");
	}
	
	@Override
	public void createPartControl(final Composite parent){
		reminderLabelProvider.updateUserConfiguration();
		
		filter = new ReminderFilter();
		vc = new ViewerConfigurer(new CommonContentProviderAdapter() {
			@Override
			public Object[] getElements(final Object inputElement){
				// Display reminders only if one is logged in
				if (CoreHub.actUser == null) {
					return new Object[0];
				}
				
				SortedSet<Reminder> reminders = new TreeSet<Reminder>();
				
				boolean ownReminders = ownReminderAction.isChecked();
				boolean otherReminders = (othersReminderAction.isChecked()
					&& CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS));
				
				if(otherReminders) {
					qbe.clear();
					reminders.addAll(qbe.execute());
				} else {
					if(ownReminders) {
						qbe.clear();
						qbe.add(Reminder.CREATOR, Query.EQUALS, CoreHub.actUser.getId());
						qbe.or();
						qbe.add(Reminder.RESPONSIBLE, Query.EQUALS, CoreHub.actUser.getId());

						reminders.addAll(qbe.execute());
					}
				}
				
				if (filterActionSet.size() > 0) {
					reminders.removeIf( p->(filterActionSet.contains(p.getActionType().numericValue())));
				}
				
				return reminders.toArray();
				
			}
		}, reminderLabelProvider, null, new ViewerConfigurer.DefaultButtonProvider(),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.MULTI, cv));
		
		makeActions();
		
		IAction[] list = new IAction[] {
			newReminderAction, deleteReminderAction, null, onlyOpenReminderAction,
			ownReminderAction, othersReminderAction, null
		};
		List<IAction> actionList = new ArrayList<IAction>();
		actionList.addAll(Arrays.asList(list));
		actionList.addAll(Arrays.asList(filterActionType));
		actionList.add(null);
		actionList.add(selectPatientAction);
		
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newReminderAction, toggleAutoSelectPatientAction);
		menu.createMenu(actionList.toArray(new IAction[] {}));
		
		if (CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS)) {
			othersReminderAction.setEnabled(true);
			othersReminderAction
				.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false));
		} else {
			othersReminderAction.setEnabled(false);
		}
		cv.create(vc, parent, SWT.NONE, getViewSite());
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
			public void doubleClicked(final PersistentObject obj, final CommonViewer cv){
				Reminder reminder = (Reminder) obj;
				AcquireLockBlockingUi.aquireAndRun(reminder, new ILockHandler() {
					@Override
					public void lockAcquired(){
						ReminderDetailDialog rdd =
							new ReminderDetailDialog(UiDesk.getTopShell(), (Reminder) obj);
						rdd.open();
						cv.getViewerWidget().update(obj, null);
					}
					
					@Override
					public void lockFailed(){
						cv.notify(CommonViewer.Message.update);
					}
				});
				
			}
		});
		
		menu.createViewerContextMenu(cv.getViewerWidget(), actionList.toArray(new IAction[] {}));
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
	
	@Override
	public void setFocus(){}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, getViewSite().getPart());
		CoreHub.userCfg.set(Preferences.USR_REMINDERSOPEN, onlyOpenReminderAction.isChecked());
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
	
	class ReminderLabelProvider extends DefaultLabelProvider
			implements IColorProvider, IFontProvider {
		
		private Font boldFont;
		private Color colorDue;
		private Color colorOverdue;
		private Color colorOpen;
		
		public Color getBackground(final Object element){
			if (element instanceof Reminder) {
				ProcessStatus stat = ((Reminder) element).getStatus();
				if (stat == ProcessStatus.DUE) {
					return colorDue;
				} else if (stat == ProcessStatus.OVERDUE) {
					return colorOverdue;
				} else if (stat == ProcessStatus.OPEN) {
					return colorOpen;
				} else {
					return null;
				}
			}
			return null;
		}
		
		public void updateUserConfiguration(){
			cfg = CoreHub.userCfg.getBranch(Preferences.USR_REMINDERCOLORS, true);
			colorDue = UiDesk.getColorFromRGB(cfg.get(ProcessStatus.DUE.getLocaleText(), "FFFFFF")); //$NON-NLS-1$;
			colorOverdue =
				UiDesk.getColorFromRGB(cfg.get(ProcessStatus.OVERDUE.getLocaleText(), "FF0000")); //$NON-NLS-1$
			colorOpen =
				UiDesk.getColorFromRGB(cfg.get(ProcessStatus.OPEN.getLocaleText(), "00FF00")); //$NON-NLS-1$
		}
		
		public Color getForeground(final Object element){
			Reminder reminder = (Reminder) element;
			Priority prio = reminder.getPriority();
			if (Priority.LOW == prio) {
				return UiDesk.getColor(UiDesk.COL_GREY);
			}
			return null;
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex){
			Reminder reminder = (Reminder) element;
			Type actionType = reminder.getActionType();
			return determineActionTypeImage(actionType).getImage();
		}
		
		@Override
		public String getColumnText(Object element, int column){
			Reminder reminder = (Reminder) element;
			ProcessStatus status = reminder.getStatus();
			if (ProcessStatus.CLOSED == status) {
				return "[" + status.getLocaleText() + "] " + super.getText(element);
			}
			return super.getText(element);
		}
		
		@Override
		public Font getFont(Object element){
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
			return null;
		}
	}
	
	private void makeActions(){
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
		deleteReminderAction = new Action(Messages.ReminderView_deleteAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.ReminderView_deleteToolTip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				Object[] selections = cv.getSelection();
				if ((selections != null) && (selections.length > 0)) {
					for (Object sel : selections) {
						Reminder r = (Reminder) sel;
						LockResponse lockResponse = CoreHub.getLocalLockService().acquireLock(r);
						if (lockResponse.isOk()) {
							r.delete();
							CoreHub.getLocalLockService().releaseLock(r);
						} else {
							LockResponseHelper.showInfo(lockResponse, r, null);
						}
					}
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			}
		};
		onlyOpenReminderAction =
			new Action(Messages.ReminderView_onlyDueAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setToolTipText(Messages.ReminderView_onlyDueToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					boolean bChecked = onlyOpenReminderAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDERSOPEN, bChecked);
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};
		ownReminderAction =
			new Action(Messages.ReminderView_myRemindersAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setToolTipText(Messages.ReminderView_myRemindersToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					boolean bChecked = ownReminderAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDEROWN, bChecked);
					cv.notify(CommonViewer.Message.update_keeplabels);
				}
			};
		othersReminderAction = new RestrictedAction(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS,
			Messages.ReminderView_foreignAction, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.ReminderView_foreignTooltip);
			}
			
			@Override
			public void doRun(){
				CoreHub.userCfg.set(Preferences.USR_REMINDEROTHERS,
					othersReminderAction.isChecked());
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
		};
		
		toggleAutoSelectPatientAction =
			new Action(Messages.ReminderView_activatePatientAction, Action.AS_CHECK_BOX) {
				{
					setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
					setToolTipText(Messages.ReminderView_toggleSelectPatientActionTooltip);
					autoSelectPatient =
						CoreHub.userCfg.get(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false);
					setChecked(autoSelectPatient);
				}
				
				@Override
				public void run(){
					autoSelectPatient = toggleAutoSelectPatientAction.isChecked();
					CoreHub.userCfg.set(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT,
						autoSelectPatient);
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
			cv.notify(CommonViewer.Message.update);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_user, eeli_reminder);
			CoreHub.heart.removeListener(this);
		}
		
	}
	
	public void heartbeat(){
		cv.notify(CommonViewer.Message.update);
	}
	
	class ReminderFilter extends ViewerFilter {
		@Override
		public boolean select(final Viewer viewer, final Object parentElement,
			final Object element){
			if (element instanceof Reminder) {
				Reminder check = (Reminder) element;
				if (onlyOpenReminderAction.isChecked()) {
					if (check.getDateDue().isAfter(new TimeTool())) {
						return false;
					}
					if (check.getStatus().ordinal() > 2) {
						return false;
					}
				}
				Patient act = ElexisEventDispatcher.getSelectedPatient();
				if (act != null) {
					String[] vals = check.get(true, Reminder.KONTAKT_ID, Reminder.FLD_VISIBILITY);
					if (!vals[0].equals(act.getId())) {
						if (Visibility.byNumericSafe(vals[1]) != Visibility.ALWAYS) {
							return false;
						}
					}
				}
				
			}
			return true;
		}
	}
}
