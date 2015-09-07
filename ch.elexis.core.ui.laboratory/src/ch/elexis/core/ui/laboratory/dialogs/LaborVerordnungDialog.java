package ch.elexis.core.ui.laboratory.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite.Group;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite.GroupItem;
import ch.elexis.core.ui.laboratory.laborlink.LaborLink;
import ch.elexis.core.ui.util.IExternLaborOrder;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Anwender;
import ch.elexis.data.LabOrder;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.tools.TimeTool;

public class LaborVerordnungDialog extends TitleAreaDialog {
	private static final String LAST_SELECTED_USER = LaborLink.PROVIDER_ID + "/last_selected_user"; //$NON-NLS-1$
	
	private Patient patient = null;
	private TimeTool date = null;
	
	private LabItemTreeSelectionComposite selectionComposite;
	
	private ComboViewer userViewer = null;
	
	private ComboViewer externViewer = null;
	
	private DateTime observationTime;
	private DateTime observationDate;
	
	private Text orderId;
	
	public LaborVerordnungDialog(Shell parentShell, Patient patient, TimeTool date){
		super(parentShell);
		this.patient = patient;
		this.date = date;
	}
	
	private void selectLastSelectedUser(){
		String id = CoreHub.userCfg.get(LAST_SELECTED_USER, ""); //$NON-NLS-1$
		Anwender user = Anwender.load(id);
		if (user != null && user.exists()) {
			StructuredSelection newSelection = new StructuredSelection(user);
			userViewer.setSelection(newSelection);
		}
	}
	
	private void saveLastSelectedUser(){
		Anwender user = getSelectedUser();
		String id = ""; //$NON-NLS-1$
		if (user != null) {
			id = user.getId();
		}
		CoreHub.userCfg.set(LAST_SELECTED_USER, id);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		
		selectionComposite = new LabItemTreeSelectionComposite(composite,
			new LabItemsLabelProvider(), true, SWT.NONE);
		selectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				updateSelectionMessage();
			}
		});
		selectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.LaborVerordnungDialog_labelResponsible);
		
		userViewer = new ComboViewer(composite,
			SWT.SINGLE | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		userViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		userViewer.setContentProvider(new ArrayContentProvider());
		
		userViewer.setLabelProvider(new DefaultLabelProvider());
		
		Query<Anwender> query = new Query<Anwender>(Anwender.class);
		List<Anwender> users = query.execute();
		users.set(0, new NoAnwender());
		userViewer.setInput(users);
		
		selectLastSelectedUser();
		
		label = new Label(composite, SWT.NONE);
		label.setText("Extern verordnen"); //$NON-NLS-1$
		
		externViewer = new ComboViewer(composite,
			SWT.SINGLE | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		externViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		externViewer.setContentProvider(new ArrayContentProvider());
		
		externViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IExternLaborOrder) {
					IExternLaborOrder extern = (IExternLaborOrder) element;
					return extern.getLabel();
				} else {
					return "???";
				}
			}
		});
		
		externViewer.setInput(getExternLaborOrder());
		
		label = new Label(composite, SWT.NONE);
		label.setText("Entnahme-/Beobachtungszeitpunkt");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		Composite dateComposite = new Composite(composite, SWT.NONE);
		dateComposite.setLayout(new GridLayout(2, true));
		dateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		observationTime = new DateTime(dateComposite, SWT.TIME);
		observationTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		observationDate = new DateTime(dateComposite, SWT.DATE);
		observationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		observationTime.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
			date.get(Calendar.SECOND));
		observationDate.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
			date.get(Calendar.DAY_OF_MONTH));
			
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.LaborVerordnungDialog_labelOrderNumber);
		orderId = new Text(composite, SWT.SEARCH);
		orderId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		orderId.setText(LabOrder.getNextOrderId());
		orderId.setEnabled(false);
		
		return composite;
	}
	
	private List<IExternLaborOrder> getExternLaborOrder(){
		List<IExternLaborOrder> externLaborOrders = Extensions.getClasses(
			Extensions.getExtensions(ExtensionPointConstantsUi.LABORORDER), "class", //$NON-NLS-1$ //$NON-NLS-2$
			false);
		return externLaborOrders;
	}
	
	private void updateSelectionMessage(){
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
	protected Control createContents(Composite parent){
		Control contents = super.createContents(parent);
		setTitle(Messages.LaborVerordnungDialog_title);
		setMessage(Messages.LaborVerordnungDialog_message
			+ " verantwortliche Person aus. (Der verantwortlichen Person" //$NON-NLS-1$
			+ " wird eine Pendenz zugeordnet.)"); //$NON-NLS-1$
		getShell().setText(Messages.LaborVerordnungDialog_shellTitle);
		return contents;
	}
	
	private List<LabOrder> createLabOrders(List<GroupItem> items){
		List<LabOrder> ret = new ArrayList<LabOrder>();
		getTime(observationTime, date);
		getDate(observationDate, date);
		TimeTool now = new TimeTool();
		if (items != null) {
			for (GroupItem groupItem : items) {
				LabOrder order = new LabOrder(CoreHub.actUser, CoreHub.actMandant, patient,
					groupItem.getLabItem(), null, orderId.getText(), groupItem.getGroupname(), now);
				order.setObservationTimeWithResults(date);
				ret.add(order);
			}
		}
		return ret;
	}
	
	private void getTime(DateTime widget, TimeTool time){
		time.set(Calendar.HOUR_OF_DAY, widget.getHours());
		time.set(Calendar.MINUTE, widget.getMinutes());
		time.set(Calendar.SECOND, widget.getSeconds());
	}
	
	private void getDate(DateTime widget, TimeTool date){
		date.set(Calendar.YEAR, widget.getYear());
		date.set(Calendar.MONTH, widget.getMonth());
		date.set(Calendar.DAY_OF_MONTH, widget.getDay());
	}
	
	private void createReminder(Anwender user, List<LabOrder> orders){
		StringBuilder message = new StringBuilder("Labor"); //$NON-NLS-1$
		StringBuilder params = new StringBuilder();
		if (orders != null && !orders.isEmpty()) {
			message.append(" ")
				.append(
					ch.elexis.core.ui.laboratory.controls.Messages.LaborOrdersComposite_columnOrdernumber)
				.append(": ").append(orders.get(0).get(LabOrder.FLD_ORDERID)); //$NON-NLS-1$
			params.append(LabOrder.FLD_ORDERID + "=" + orders.get(0).get(LabOrder.FLD_ORDERID));
		}
		Reminder reminder = new Reminder(patient, date.toString(TimeTool.DATE_ISO),
			Reminder.Typ.anzeigeTodoAll, params.toString(), message.toString()); //$NON-NLS-1$
		if (user != null) {
			reminder.set("Responsible", user.getId()); //$NON-NLS-1$
		}
	}
	
	private Anwender getSelectedUser(){
		Object sel = ((IStructuredSelection) userViewer.getSelection()).getFirstElement();
		if (sel instanceof Anwender) {
			if (!(sel instanceof NoAnwender)) {
				return (Anwender) sel;
			}
		}
		return null;
	}
	
	@Override
	protected void okPressed(){
		List<LabOrder> existing =
			LabOrder.getLabOrders(null, null, null, null, orderId.getText(), null, null);
		if (existing != null) {
			String newOrderId = LabOrder.getNextOrderId();
			setErrorMessage(String.format(Messages.LaborVerordnungDialog_errorOrderNumber,
				orderId.getText(), newOrderId));
			orderId.setText(newOrderId);
			return;
		}
		
		List<LabOrder> orders = createLabOrders(selectionComposite.getSelectedItems());
		if (getSelectedUser() != null) {
			createReminder(getSelectedUser(), orders);
		}
		
		saveLastSelectedUser();
		
		StructuredSelection externSelection = (StructuredSelection) externViewer.getSelection();
		if (!externSelection.isEmpty()) {
			IExternLaborOrder extern = (IExternLaborOrder) externSelection.getFirstElement();
			extern.order(patient, orders);
		}
		
		super.okPressed();
	}
	
	private class LabItemsLabelProvider extends LabelProvider {
		private HashMap<Object, String> cache = new HashMap<Object, String>();
		
		@Override
		public String getText(Object element){
			String label = cache.get(element);
			if (label == null) {
				if (element instanceof Group) {
					label = ((Group) element).toString();
					cache.put(element, label);
				} else if (element instanceof GroupItem) {
					List<LabOrder> orders = LabOrder.getLabOrders(patient, null,
						((GroupItem) element).getLabItem(), null, null, date, null);
					if (orders != null && !orders.isEmpty()) {
						label = Messages.LaborVerordnungDialog_alreadyOrdered
							+ ((GroupItem) element).getLabItem().getLabel(); //$NON-NLS-1$
					} else {
						label = ((GroupItem) element).getLabItem().getLabel();
					}
					cache.put(element, label);
				}
			}
			return label;
		}
	}
	
	private static class NoAnwender extends Anwender {
		@Override
		public String getId(){
			return "";
		}
		
		@Override
		public String getLabel(){
			return "";
		}
		
		@Override
		public String getLabel(boolean shortLabel){
			return getLabel();
		}
	}
}