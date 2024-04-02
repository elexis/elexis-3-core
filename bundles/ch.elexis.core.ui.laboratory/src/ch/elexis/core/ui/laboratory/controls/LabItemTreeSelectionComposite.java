package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.laboratory.preferences.LabGroupPrefs;
import ch.elexis.data.LabGroup;
import ch.elexis.data.LabItem;
import ch.elexis.data.Query;

public class LabItemTreeSelectionComposite extends Composite {
	// height of laborViewer
	private static final int LINES_TO_SHOW = 20;

	private ListenerList listenerList = new ListenerList();

	private LabItemsViewerFilter filter = new LabItemsViewerFilter();
	private LabItemsContentProvider contentProvider = new LabItemsContentProvider();
	private ILabelProvider labelProvider;

	ArrayList<GroupItem> checkState = new ArrayList<>();
	private ContainerCheckedTreeViewer laborViewer = null;
	private Text filterText;

	private boolean addCustomGroups;

	public LabItemTreeSelectionComposite(Composite parent, ILabelProvider labelProvider, int style) {
		this(parent, labelProvider, false, style);
	}

	public LabItemTreeSelectionComposite(Composite parent, ILabelProvider labelProvider, boolean addCustomGroups,
			int style) {
		super(parent, style);
		this.labelProvider = labelProvider;
		this.addCustomGroups = addCustomGroups;

		setLayout(new GridLayout(1, false));

		filterText = new Text(this, SWT.SEARCH);
		filterText.setMessage("Filter"); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		filterText.setLayoutData(data);
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (filterText.getText().length() > 1) {
					filter.setSearchText(filterText.getText());
					laborViewer.refresh();
				} else {
					filter.setSearchText(StringUtils.EMPTY);
					laborViewer.refresh();
				}
				restoreLeafCheckState();
				fireSelectionChanged();
			}
		});

		laborViewer = new ContainerCheckedTreeViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		laborViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				// We use an additive check state cache so we need to remove
				// previously checked items if the user unchecked them.
				if (!event.getChecked() && checkState != null) {
					Iterator<GroupItem> iter = checkState.iterator();
					ArrayList<GroupItem> toRemove = new ArrayList<>(1);
					if (event.getElement() instanceof GroupItem) {
						while (iter.hasNext()) {
							Object element = iter.next();
							if (element.equals(event.getElement())) {
								toRemove.add((GroupItem) element);
							}
						}
					} else if (event.getElement() instanceof Group) {
						toRemove.addAll(((Group) event.getElement()).getItems());
					}
					checkState.removeAll(toRemove);
				} else if (event.getChecked()) {
					rememberLeafCheckState(event);
				}
				fireSelectionChanged();
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

		laborViewer.setLabelProvider(labelProvider);

		laborViewer.setInput(loadItems());
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listenerList.add(listener);
	}

	private void fireSelectionChanged() {
		Object[] listeners = listenerList.getListeners();
		for (Object object : listeners) {
			if (object instanceof ISelectionChangedListener) {
				SelectionChangedEvent event = new SelectionChangedEvent(laborViewer,
						new StructuredSelection(getSelectedItems()));
				((ISelectionChangedListener) object).selectionChanged(event);
			}
		}
	}

	private void rememberLeafCheckState(CheckStateChangedEvent event) {
		Object[] checked = laborViewer.getCheckedElements();
		if (checkState == null) {
			checkState = new ArrayList<>(checked.length);
		}
		for (int i = 0; i < checked.length; i++) {
			if (!laborViewer.getGrayed(checked[i])) {
				if (!checkState.contains(checked[i])) {
					if (checked[i] instanceof GroupItem) {
						checkState.add((GroupItem) checked[i]);
					} else if ((checked[i] instanceof Group) && (event.getElement() == checked[i])) {
						checkState.addAll(((Group) checked[i]).getItems());
					}
				}
			}
		}
	}

	private void restoreLeafCheckState() {
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

	public List<GroupItem> getSelectedItems() {
		return new ArrayList<>(checkState);
	}

	/**
	 * Create a map with the groups of items
	 *
	 */
	private Hashtable<String, Group> loadItems() {
		Hashtable<String, Group> allGroups = new Hashtable<>();

		Query<LabItem> query = new Query<>(LabItem.class, "LABORITEMS", false, //$NON-NLS-1$
				new String[] { LabItem.GROUP, LabItem.SHORTNAME, LabItem.TITLE });
		List<LabItem> lItems = query.execute();
		if (lItems == null) {
			// error empty map
			return allGroups;
		}

		if (!ConfigServiceHolder.getUser(LabGroupPrefs.SHOW_GROUPS_ONLY, false)) {
			for (LabItem it : lItems) {
				String groupName = it.getGroup();
				Group group = allGroups.get(groupName);
				if (group == null) {
					group = new Group(groupName, new ArrayList<LabItem>());
					allGroups.put(groupName, group);
				}
				group.addItem(it);
			}
		}

		if (addCustomGroups) {
			allGroups.putAll(loadCustomGroups());
		}

		return allGroups;
	}

	/**
	 * Load User-defined LabGroups
	 */
	private Hashtable<String, Group> loadCustomGroups() {
		Hashtable<String, Group> customGroups = new Hashtable<>();

		Query<LabGroup> query = new Query<>(LabGroup.class);
		query.orderBy(false, "Name"); //$NON-NLS-1$
		List<LabGroup> labGroups = query.execute();
		if (labGroups != null) {
			for (LabGroup labGroup : labGroups) {
				Group group = new Group(labGroup);
				customGroups.put(labGroup.getName(), group);
			}
		}

		return customGroups;
	}

	public static class Group {
		String name;
		String shortName;
		private List<GroupItem> items;
		private boolean sorted;

		Group(String name, List<LabItem> labItems) {
			this.name = name;
			items = createGroupItems(labItems);

			// shortname as in LaborView (without ordering number)
			String[] gn = name.split(" +"); //$NON-NLS-1$
			if (gn.length > 1) {
				shortName = gn[1];
			} else {
				shortName = "? " + name + " ?"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			sorted = false;
		}

		public void addItem(LabItem labItem) {
			if (items == null) {
				items = new ArrayList<>();
			}
			items.add(new GroupItem(name, labItem));
		}

		Group(LabGroup labGroup) {
			this.name = labGroup.getName();
			this.shortName = this.name;

			List<LabItem> labItems = labGroup.getItems();
			items = createGroupItems(labItems);
		}

		public List<GroupItem> getItems() {
			if (!sorted) {
				Collections.sort(items, new Comparator<GroupItem>() {
					@Override
					public int compare(GroupItem left, GroupItem right) {
						String prio1 = left.getLabItemPrio().orElse(StringUtils.EMPTY);
						String prio2 = right.getLabItemPrio().orElse(StringUtils.EMPTY);
						if (StringUtils.isNumeric(prio1) && StringUtils.isNumeric(prio2)) {
							try {
								return Integer.valueOf(prio1).compareTo(Integer.valueOf(prio2));
							} catch (NumberFormatException nfe) {
								// ignore
							}
						}
						return prio1.compareTo(prio2);
					}
				});
				sorted = true;
			}
			return items;
		}

		private List<GroupItem> createGroupItems(List<LabItem> labItems) {
			List<GroupItem> groupItems = new ArrayList<>();
			for (LabItem labItem : labItems) {
				groupItems.add(new GroupItem(name, labItem));
			}
			return groupItems;
		}

		@Override
		public String toString() {
			return shortName;
		}
	}

	public static class GroupItem {
		private String groupname;
		private LabItem labItem;

		public GroupItem(String groupname, LabItem labItem) {
			this.setGroupname(groupname);
			this.setLabItem(labItem);
		}

		public LabItem getLabItem() {
			return labItem;
		}

		public void setLabItem(LabItem labItem) {
			this.labItem = labItem;
		}

		public String getGroupname() {
			return groupname;
		}

		public void setGroupname(String groupname) {
			this.groupname = groupname;
		}

		public Optional<String> getLabItemPrio() {
			if (labItem != null) {
				return Optional.ofNullable(labItem.getPrio());
			}
			return Optional.empty();
		}
	}

	private class LabItemsViewerFilter extends ViewerFilter {
		protected String searchString;

		public void setSearchText(String s) {
			// Search must be a substring of the existing value
			this.searchString = ".*" + s + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		private boolean isSelect(Object leaf) {
			String label = labelProvider.getText(leaf);
			if (label != null && label.toLowerCase().matches(searchString.toLowerCase())) {
				return true;
			}
			return false;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
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

	private static class LabItemsContentProvider implements ITreeContentProvider {
		private Hashtable<String, Group> items;

		@Override
		public Object[] getElements(Object inputElement) {
			ArrayList<Group> ret = new ArrayList<>();
			ret.addAll(items.values());
			Collections.sort(ret, new Comparator<Group>() {
				@Override
				public int compare(Group o1, Group o2) {
					return o1.shortName.compareTo(o2.shortName);
				}
			});
			return ret.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Group) {
				Group group = (Group) parentElement;
				return group.getItems().toArray();
			} else {
				return null;
			}
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element instanceof Group);
		}

		@Override
		public Object[] getParent(Object element) {
			return null;
		}

		@Override
		public void dispose() {
			// nothing to do
		}

		@Override
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof Hashtable<?, ?>) {
				items = (Hashtable<String, Group>) newInput;
			}
		}
	}
}
