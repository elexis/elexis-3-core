package ch.elexis.core.ui.laboratory.dialogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.laboratory.laborlink.LaborLink;
import ch.elexis.core.ui.laboratory.preferences.LabGroupPrefs;
import ch.elexis.core.ui.util.IExternLaborOrder;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Anwender;
import ch.elexis.data.LabGroup;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.tools.TimeTool;

public class LaborVerordnungDialog extends TitleAreaDialog {
	// height of laborViewer
	private static final int LINES_TO_SHOW = 20;
	
	private static final String LAST_SELECTED_USER = LaborLink.PROVIDER_ID + "/last_selected_user"; //$NON-NLS-1$
	
	private Patient patient = null;
	private TimeTool date = null;
	
	private LabItemsViewerFilter filter = new LabItemsViewerFilter();
	private LabItemsContentProvider contentProvider = new LabItemsContentProvider();
	
	ArrayList<GroupItem> checkState = new ArrayList<GroupItem>();
	private ContainerCheckedTreeViewer laborViewer = null;
	
	private ComboViewer userViewer = null;
	
	private ComboViewer externViewer = null;
	
	private DateTime observationTime;
	private DateTime observationDate;

	private Text filterText;
	private Text orderId;
	
	public LaborVerordnungDialog(Shell parentShell, Patient patient, TimeTool date){
		super(parentShell);
		this.patient = patient;
		this.date = date;
	}
	
	/**
	 * Create a map with the groups of items
	 * 
	 */
	private Hashtable<String, LaborVerordnungDialog.Group> loadItems(){
		Hashtable<String, Group> allGroups = new Hashtable<String, LaborVerordnungDialog.Group>();
		
		Query<LabItem> query = new Query<LabItem>(LabItem.class);
		List<LabItem> lItems = query.execute();
		if (lItems == null) {
			// error empty map
			return allGroups;
		}
		
		if (!CoreHub.userCfg.get(LabGroupPrefs.SHOW_GROUPS_ONLY, false)) {
			for (LabItem it : lItems) {
				String groupName = it.getGroup();
				LaborVerordnungDialog.Group group = allGroups.get(groupName);
				if (group == null) {
					group = new Group(groupName, new ArrayList<LabItem>());
					allGroups.put(groupName, group);
				}
				group.addItem(it);
			}
		}
		
		allGroups.putAll(loadCustomGroups());
		
		return allGroups;
	}
	
	/**
	 * Load User-defined LabGroups
	 */
	private Hashtable<String, Group> loadCustomGroups(){
		Hashtable<String, Group> customGroups =
			new Hashtable<String, LaborVerordnungDialog.Group>();
		
		Query<LabGroup> query = new Query<LabGroup>(LabGroup.class);
		query.orderBy(false, "Name"); //$NON-NLS-1$
		List<LabGroup> labGroups = query.execute();
		if (labGroups != null) {
			for (LabGroup labGroup : labGroups) {
				LaborVerordnungDialog.Group group = new Group(labGroup);
				customGroups.put(labGroup.getName(), group);
			}
		}
		
		return customGroups;
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
		
		filterText = new Text(composite, SWT.SEARCH);
		filterText.setMessage("Filter"); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		filterText.setLayoutData(data);
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				if (filterText.getText().length() > 1) {
					filter.setSearchText(filterText.getText());
					laborViewer.refresh();
				} else {
					filter.setSearchText(""); //$NON-NLS-1$
					laborViewer.refresh();
				}
				restoreLeafCheckState();
				updateSelectionMessage();
			}
		});
		
		laborViewer =
			new ContainerCheckedTreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		laborViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event){
				// We use an additive check state cache so we need to remove
				// previously checked items if the user unchecked them.
				if (!event.getChecked() && checkState != null) {
					Iterator<GroupItem> iter = checkState.iterator();
					ArrayList<GroupItem> toRemove = new ArrayList<GroupItem>(1);
					if (event.getElement() instanceof GroupItem) {
						while (iter.hasNext()) {
							Object element = iter.next();
							if (element.equals(event.getElement())) {
								toRemove.add((GroupItem) element);
							}
						}
					} else if (event.getElement() instanceof Group) {
						toRemove.addAll(((Group) event.getElement()).items);
					}
					checkState.removeAll(toRemove);
				} else if (event.getChecked()) {
					rememberLeafCheckState(event);
				}
				updateSelectionMessage();
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// initially, show 10 lines
		gd.heightHint = laborViewer.getTree().getItemHeight() * LINES_TO_SHOW;
		laborViewer.getControl().setLayoutData(gd);
		
		ViewerFilter[] filters = new ViewerFilter[1];
		filters[0] = filter;
		laborViewer.setFilters(filters);
		
		laborViewer.setContentProvider(contentProvider);
		
		laborViewer.setLabelProvider(new LabItemsLabelProvider());
		
		laborViewer.setInput(loadItems());
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.LaborVerordnungDialog_labelResponsible);
		
		userViewer =
			new ComboViewer(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
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
		
		externViewer =
			new ComboViewer(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
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
		List<IExternLaborOrder> externLaborOrders =
			Extensions.getClasses(Extensions.getExtensions(ExtensionPointConstantsUi.LABORORDER),
				"class", //$NON-NLS-1$ //$NON-NLS-2$
				false);
		return externLaborOrders;
	}
	
	void rememberLeafCheckState(CheckStateChangedEvent event){
		Object[] checked = laborViewer.getCheckedElements();
		if (checkState == null) {
			checkState = new ArrayList<GroupItem>(checked.length);
		}
		for (int i = 0; i < checked.length; i++) {
			if (!laborViewer.getGrayed(checked[i])) {
				if (!checkState.contains(checked[i])) {
					if (checked[i] instanceof GroupItem) {
						checkState.add((GroupItem) checked[i]);
					} else if ((checked[i] instanceof Group) && (event.getElement() == checked[i])) {
						checkState.addAll(((Group) checked[i]).items);
					}
				}
			}
		}
	}
	
	void restoreLeafCheckState(){
		if (laborViewer == null || laborViewer.getTree().isDisposed())
			return;
		if (checkState == null)
			return;
		
		laborViewer.setCheckedElements(new Object[0]);
		laborViewer.setGrayedElements(new Object[0]);
		// Now we are only going to set the check state of the leaf nodes
		// and rely on our container checked code to update the parents properly.
		Iterator<GroupItem> iter = checkState.iterator();
		Object element = null;
		Object[] expanded = null;
		if (iter.hasNext()) {
			expanded = laborViewer.getExpandedElements();
			laborViewer.expandAll();
		}
		while (iter.hasNext()) {
			element = iter.next();
			laborViewer.setChecked(element, true);
		}
		laborViewer.collapseAll();
		if (expanded != null) {
			laborViewer.setExpandedElements(expanded);
		}
	}
	
	private void updateSelectionMessage(){
		List<GroupItem> selected = getSelectedItems();
		StringBuilder sb = new StringBuilder();
		
		for (GroupItem groupItem : selected) {
			sb.append(groupItem.groupname + " - " + groupItem.labItem.getKuerzel()); //$NON-NLS-1$
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
				LabOrder order =
					new LabOrder(CoreHub.actUser, CoreHub.actMandant, patient, groupItem.labItem,
						null, orderId.getText(), groupItem.groupname, now);
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
		if (orders != null && !orders.isEmpty()) {
			message
				.append(" ")
				.append(
					ch.elexis.core.ui.laboratory.controls.Messages.LaborOrdersComposite_columnOrdernumber)
				.append(": ").append(orders.get(0).get(LabOrder.FLD_ORDERID)); //$NON-NLS-1$
		}
		Reminder reminder =
			new Reminder(patient, date.toString(TimeTool.DATE_ISO), Reminder.Typ.anzeigeTodoAll,
				LabOrder.FLD_ORDERID + "=" + orders.get(0).get(LabOrder.FLD_ORDERID), message.toString()); //$NON-NLS-1$
		if (user != null) {
			reminder.set("Responsible", user.getId()); //$NON-NLS-1$
		}
	}
	
	private List<GroupItem> getSelectedItems(){
		return new ArrayList<GroupItem>(checkState);
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
		
		List<LabOrder> orders = createLabOrders(getSelectedItems());
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
	
	private class LabItemsViewerFilter extends ViewerFilter {
		protected String searchString;
		protected LabelProvider labelProvider = new LabItemsLabelProvider();
		
		public void setSearchText(String s){
			// Search must be a substring of the existing value
			this.searchString = ".*" + s + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		private boolean isSelect(Object leaf){
			String label = labelProvider.getText(leaf);
			if (label != null && label.toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
			return false;
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			
			StructuredViewer sviewer = (StructuredViewer) viewer;
			ITreeContentProvider provider = (ITreeContentProvider) sviewer.getContentProvider();
			Object[] children = provider.getChildren(element);
			if (children != null && children.length > 0) {
				for (Object child : children) {
					if (select(viewer, element, child)) {
						return true;
					}
				}
			}
			return isSelect(element);
		}
	}
	
	private class LabItemsLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element){
			if (element instanceof Group) {
				return ((Group) element).toString();
			} else if (element instanceof GroupItem) {
				List<LabOrder> orders =
					LabOrder.getLabOrders(patient, null, ((GroupItem) element).labItem, null, null,
						date, null);
				if (orders != null && !orders.isEmpty()) {
					return Messages.LaborVerordnungDialog_alreadyOrdered
						+ ((GroupItem) element).labItem.getLabel(); //$NON-NLS-1$
				} else {
					return ((GroupItem) element).labItem.getLabel();
				}
			}
			return super.getText(element);
		}
	}
	
	private static class LabItemsContentProvider implements ITreeContentProvider {
		private Hashtable<String, Group> items;
		
		@Override
		public Object[] getElements(Object inputElement){
			ArrayList<Group> ret = new ArrayList<LaborVerordnungDialog.Group>();
			ret.addAll(items.values());
			Collections.sort(ret, new Comparator<Group>() {
				@Override
				public int compare(Group o1, Group o2){
					return o1.shortName.compareTo(o2.shortName);
				}
			});
			return ret.toArray();
		}
		
		@Override
		public Object[] getChildren(Object parentElement){
			if (parentElement instanceof Group) {
				Group group = (Group) parentElement;
				return group.items.toArray();
			} else {
				return null;
			}
		}
		
		@Override
		public boolean hasChildren(Object element){
			return (element instanceof LaborVerordnungDialog.Group);
		}
		
		@Override
		public Object[] getParent(Object element){
			return null;
		}
		
		@Override
		public void dispose(){
			// nothing to do
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			if (newInput instanceof Hashtable<?, ?>) {
				items = (Hashtable<String, Group>) newInput;
			}
		}
	}
	
	private static class Group {
		String name;
		String shortName;
		List<GroupItem> items;
		
		Group(String name, List<LabItem> labItems){
			this.name = name;
			items = createGroupItems(labItems);
			
			// shortname as in LaborView (without ordering number)
			String[] gn = name.split(" +"); //$NON-NLS-1$
			if (gn.length > 1) {
				shortName = gn[1];
			} else {
				shortName = "? " + name + " ?"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		public void addItem(LabItem labItem){
			if (items == null) {
				items = new ArrayList<GroupItem>();
			}
			items.add(new GroupItem(name, labItem));
		}
		
		Group(LabGroup labGroup){
			this.name = labGroup.getName();
			this.shortName = this.name;
			
			List<LabItem> labItems = labGroup.getItems();
			items = createGroupItems(labItems);
		}
		
		private List<GroupItem> createGroupItems(List<LabItem> labItems){
			List<GroupItem> groupItems = new ArrayList<GroupItem>();
			for (LabItem labItem : labItems) {
				groupItems.add(new GroupItem(name, labItem));
			}
			return groupItems;
		}
		
		@Override
		public String toString(){
			return shortName;
		}
	}
	
	private static class GroupItem {
		String groupname;
		LabItem labItem;
		
		public GroupItem(String groupname, LabItem labItem){
			this.groupname = groupname;
			this.labItem = labItem;
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