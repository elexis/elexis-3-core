package ch.elexis.core.ui.laboratory.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.ILabOrder;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite.Group;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite.GroupItem;
import ch.elexis.core.ui.laboratory.laborlink.LaborLink;
import ch.elexis.core.ui.laboratory.views.LabOrderView;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Anwender;
import ch.elexis.data.LabOrder;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.tools.TimeTool;

public class LaborVerordnungDialog extends TitleAreaDialog {
	private static final String LAST_SELECTED_USER = LaborLink.PROVIDER_ID + "/last_selected_user"; //$NON-NLS-1$
	private static final String PREV_PRINT_SETTING = LaborLink.PROVIDER_ID + "/prev_print_setting"; //$NON-NLS-1$
	public static final String OUTPUTLOG_EXTERNES_LABOR = "Externes Labor"; //$NON-NLS-1$
	private Patient patient = null;
	private TimeTool date = null;

	private LabItemTreeSelectionComposite selectionComposite;

	private ComboViewer userViewer = null;



	private DateTime observationTime;
	private DateTime observationDate;

	private Text orderId;

	private Button btnPrint;
	private Button btnExternLabor;

	public LaborVerordnungDialog(Shell parentShell, Patient patient, TimeTool date) {
		super(parentShell);
		this.patient = patient;
		this.date = date;
	}

	private void selectLastSelectedUser() {
		String id = ConfigServiceHolder.getUser(LAST_SELECTED_USER, StringUtils.EMPTY);
		Anwender user = Anwender.load(id);
		if (user != null && user.exists()) {
			StructuredSelection newSelection = new StructuredSelection(user);
			userViewer.setSelection(newSelection);
		}
	}

	private void saveLastSelectedUser() {
		Anwender user = getSelectedUser();
		String id = StringUtils.EMPTY;
		if (user != null) {
			id = user.getId();
		}
		ConfigServiceHolder.setUser(LAST_SELECTED_USER, id);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		selectionComposite = new LabItemTreeSelectionComposite(composite, new LabItemsLabelProvider(), true, SWT.NONE);
		selectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelectionMessage();
			}
		});
		selectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.LaborVerordnungDialog_labelResponsible);

		userViewer = new ComboViewer(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		userViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		userViewer.setContentProvider(new ArrayContentProvider());

		userViewer.setLabelProvider(new DefaultLabelProvider());

		Query<Anwender> query = new Query<>(Anwender.class);
		List<Anwender> users = query.execute();
		users.set(0, new NoAnwender());
		userViewer.setInput(users);

		selectLastSelectedUser();


		btnExternLabor = new Button(composite, SWT.CHECK);
		btnExternLabor.setText(Messages.LaborVerordnungDialog_externesLaborCheckbox);
		btnExternLabor.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		label = new Label(composite, SWT.NONE);
		label.setText(Messages.LaborOrdersComposite_columnObservationTime);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		Composite dateComposite = new Composite(composite, SWT.NONE);
		dateComposite.setLayout(new GridLayout(2, true));
		dateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		observationTime = new DateTime(dateComposite, SWT.TIME);
		observationTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		observationDate = new DateTime(dateComposite, SWT.DATE);
		observationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		observationTime.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));
		observationDate.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));

		label = new Label(composite, SWT.NONE);
		label.setText(Messages.LaborVerordnungDialog_labelOrderNumber);
		orderId = new Text(composite, SWT.SEARCH);
		orderId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		orderId.setText(LabOrder.getNextOrderId());
		orderId.setEnabled(false);

		Label lblSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		btnPrint = new Button(composite, SWT.CHECK);
		btnPrint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		btnPrint.setText(Messages.LaborVerordnungDialog_printOrders);
		btnPrint.setSelection(ConfigServiceHolder.getUser(PREV_PRINT_SETTING, false));

		return composite;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void updateSelectionMessage() {
		List<GroupItem> selected = selectionComposite.getSelectedItems();
		StringBuilder sb = new StringBuilder();

		for (GroupItem groupItem : selected) {
			sb.append(groupItem.getGroupname() + " - " + groupItem.getLabItem().getKuerzel()); //$NON-NLS-1$
			sb.append(", "); //$NON-NLS-1$
		}
		if (sb.length() > 2) {
			sb.setLength(sb.length() - 2);
		}

		setMessage(sb.toString(), IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(Messages.Core_prescribe_Laboratory);
		setMessage(Messages.LaborVerordnungDialog_message + " verantwortliche Person aus. (Der verantwortlichen Person" //$NON-NLS-1$
				+ " wird eine Pendenz zugeordnet.)"); //$NON-NLS-1$
		getShell().setText(Messages.Core_prescribe_Laboratory);
		return contents;
	}

	private List<LabOrder> createLabOrders(List<GroupItem> items) {
		List<LabOrder> ret = new ArrayList<>();
		getTime(observationTime, date);
		getDate(observationDate, date);
		TimeTool now = new TimeTool();
		if (items != null) {
			for (GroupItem groupItem : items) {
				LabOrder order = new LabOrder(CoreHub.getLoggedInContact().getId(),
						ContextServiceHolder.getActiveMandatorOrNull().getId(), patient.getId(), groupItem.getLabItem(),
						null, orderId.getText(), groupItem.getGroupname(), now);
				order.setObservationTimeWithResults(date);
				ret.add(order);
			}
		}
		return ret;
	}

	private void getTime(DateTime widget, TimeTool time) {
		time.set(Calendar.HOUR_OF_DAY, widget.getHours());
		time.set(Calendar.MINUTE, widget.getMinutes());
		time.set(Calendar.SECOND, widget.getSeconds());
	}

	private void getDate(DateTime widget, TimeTool date) {
		date.set(Calendar.YEAR, widget.getYear());
		date.set(Calendar.MONTH, widget.getMonth());
		date.set(Calendar.DAY_OF_MONTH, widget.getDay());
	}

	private void createReminder(Anwender user, List<LabOrder> orders) {
		StringBuilder message = new StringBuilder("Labor"); //$NON-NLS-1$
		StringBuilder params = new StringBuilder();
		if (orders != null && !orders.isEmpty()) {
			message.append(StringUtils.SPACE).append(ch.elexis.core.ui.laboratory.controls.Messages.Order_ID)
					.append(": ").append(orders.get(0).get(LabOrder.FLD_ORDERID)); //$NON-NLS-1$
			params.append(LabOrder.FLD_ORDERID + "=" + orders.get(0).get(LabOrder.FLD_ORDERID)); //$NON-NLS-1$
		}
		Reminder reminder = new Reminder(patient, date.toString(TimeTool.DATE_ISO), Visibility.ALWAYS,
				params.toString(), message.toString()); // $NON-NLS-1$
		if (user != null) {
			reminder.set("Responsible", user.getId()); //$NON-NLS-1$
		}
	}

	private Anwender getSelectedUser() {
		Object sel = ((IStructuredSelection) userViewer.getSelection()).getFirstElement();
		if (sel instanceof Anwender) {
			if (!(sel instanceof NoAnwender)) {
				return (Anwender) sel;
			}
		}
		return null;
	}

	@Override
	protected void okPressed() {
		List<ILabOrder> existing = LabOrder.getLabOrdersByOrderId(orderId.getText());
		if (existing != null) {
			String newOrderId = LabOrder.getNextOrderId();
			setErrorMessage(
					String.format(Messages.LaborVerordnungDialog_errorOrderNumber, orderId.getText(), newOrderId));
			orderId.setText(newOrderId);
			return;
		}

		final List<LabOrder> orders = createLabOrders(selectionComposite.getSelectedItems());
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, ch.elexis.core.model.ILabOrder.class);
		if (getSelectedUser() != null) {
			createReminder(getSelectedUser(), orders);
		}
		for (ILabOrder order : orders) {
			CoreModelServiceHolder.get().load(order.getId(), ch.elexis.core.model.ILabOrder.class)
					.ifPresent(laborder -> {
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, laborder);
					});

		}
		saveLastSelectedUser();

		// save print settings
		boolean doPrint = btnPrint.getSelection();
		ConfigServiceHolder.setUser(PREV_PRINT_SETTING, doPrint);
		if (btnExternLabor.getSelection()) {
			for (LabOrder order : orders) {
				logExternLaborToOutputLog(order);
			}
		}
		if (doPrint) {
			UiDesk.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					Patient pat = ElexisEventDispatcher.getSelectedPatient();
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						LabOrderView labOrderView = (LabOrderView) page.showView(LabOrderView.ID);
						labOrderView.createLabOrderPrint(pat, orders);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});

		}

		super.okPressed();
	}

	private class LabItemsLabelProvider extends LabelProvider {
		private HashMap<Object, String> cache = new HashMap<>();

		@Override
		public String getText(Object element) {
			String label = cache.get(element);
			if (label == null) {
				if (element instanceof Group) {
					label = ((Group) element).toString();
					cache.put(element, label);
				} else if (element instanceof GroupItem) {
					List<LabOrder> orders = LabOrder.getLabOrders(patient, null, ((GroupItem) element).getLabItem(),
							null, null, date, null);
					if (orders != null && !orders.isEmpty()) {
						label = Messages.LaborVerordnungDialog_alreadyOrdered
								+ ((GroupItem) element).getLabItem().getLabel(); // $NON-NLS-1$
					} else {
						label = ((GroupItem) element).getLabItem().getLabel();
					}
					cache.put(element, label);
				}
			}
			return label;
		}
	}

	private void logExternLaborToOutputLog(LabOrder order) {
		IOutputLog outputLog = CoreModelServiceHolder.get().create(IOutputLog.class);
		outputLog.setObjectId(order.getId());
		outputLog.setObjectType(order.getClass().getName());
		outputLog.setCreatorId(ContextServiceHolder.get().getActiveUser().map(user -> user.getId()).orElse("Unknown"));
		outputLog.setOutputter(getClass().getName());
		outputLog.setDate(java.time.LocalDate.now());
		outputLog.setOutputterStatus(OUTPUTLOG_EXTERNES_LABOR);
		CoreModelServiceHolder.get().save(outputLog);
	}

	private static class NoAnwender extends Anwender {
		@Override
		public String getId() {
			return StringUtils.EMPTY;
		}

		@Override
		public String getLabel() {
			return StringUtils.EMPTY;
		}

		@Override
		public String getLabel(boolean shortLabel) {
			return getLabel();
		}
	}
}