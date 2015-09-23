package ch.elexis.core.ui.preferences;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;
import ch.elexis.core.ui.util.BooleanNotConverter;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Query;
import ch.elexis.data.Role;

public class RolesToAccessRightsPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage, IValueChangeListener, ICheckStateListener {
	private DataBindingContext m_bindingContext;
	
	private WritableValue wv = new WritableValue(null, Role.class);
	
	private Text txti18n;
	private TreeViewer treeViewer;
	
	private Text txtRoleName;
	private MenuItem mntmNewRole;
	private TableViewer tableViewerRoles;
	private MenuItem mntmRemoveRole;
	
	/**
	 * Create the preference page.
	 */
	public RolesToAccessRightsPreferencePage(){
		setTitle("Rollen und Rechte");
		noDefaultAndApplyButton();
	}
	
	/**
	 * Create contents of the preference page.
	 * 
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent){
		if (!CoreHub.acl.request(AccessControlDefaults.ACL_USERS)) {
			return new PrefAccessDenied(parent);
		}
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeRoles = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeRoles = new GridLayout(1, false);
		gl_compositeRoles.verticalSpacing = 2;
		gl_compositeRoles.marginWidth = 0;
		gl_compositeRoles.marginHeight = 0;
		compositeRoles.setLayout(gl_compositeRoles);
		
		tableViewerRoles = new TableViewer(compositeRoles, SWT.BORDER);
		Table tableRoles = tableViewerRoles.getTable();
		tableRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Menu menu = new Menu(tableRoles);
		tableRoles.setMenu(menu);
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e){
				super.menuShown(e);
				Role r = (Role) wv.getValue();
				mntmRemoveRole.setEnabled(!r.isSystemRole());
			}
		});
		
		mntmNewRole = new MenuItem(menu, SWT.NONE);
		mntmNewRole.setText("Rolle hinzufügen");
		mntmNewRole.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Role newRole = new Role(false);
				updateRolesList();
				tableViewerRoles.setSelection(new StructuredSelection(newRole));
			}
		});
		
		mntmRemoveRole = new MenuItem(menu, SWT.NONE);
		mntmRemoveRole.setText("Rolle löschen");
		mntmRemoveRole.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Role r = (Role) wv.getValue();
				r.delete();
				updateRolesList();
			}
		});
		
		Composite composite = new Composite(compositeRoles, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		gl_composite.horizontalSpacing = 0;
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		txtRoleName = new Text(composite, SWT.BORDER);
		txtRoleName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtRoleName.setMessage("Bezeichnung");
		
		Link linkChangeRoleName = new Link(composite, SWT.NONE);
		linkChangeRoleName.setText(UserManagementPreferencePage.CHANGE_LINK);
		linkChangeRoleName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				String newRoleName = txtRoleName.getText();
				if (Role.verifyRoleNameNotTaken(newRoleName)) {
					setErrorMessage(null);
					Role r = (Role) wv.getValue();
					Role changedRole = r.setRoleName(newRoleName);
					updateRolesList();
					tableViewerRoles.setSelection(new StructuredSelection(changedRole));
				} else {
					setErrorMessage("Rollenname bereits vergeben.");
				}
			}
		});
		
		txti18n = new Text(compositeRoles, SWT.BORDER);
		txti18n.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txti18n.setMessage("Lokale Bezeichnung");
		txti18n.setBounds(0, 0, 64, 19);
		
		tableViewerRoles.setContentProvider(ArrayContentProvider.getInstance());
		tableViewerRoles.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public Image getColumnImage(Object element, int columnIndex){
				Role r = (Role) element;
				if (r.isSystemRole()) {
					return Images.IMG_LOCK_CLOSED.getImage();
				} else {
					return Images.IMG_EMPTY_TRANSPARENT.getImage();
				}
			}
		});
		
		tableViewerRoles.addSelectionChangedListener((e) -> {
			StructuredSelection ss = (StructuredSelection) e.getSelection();
			wv.setValue(ss == null ? null : ss.getFirstElement());
		});
		
		treeViewer = new TreeViewer(sashForm, SWT.FULL_SELECTION);
		final Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		treeViewer.setContentProvider(new ACETreeContentProvider());
		treeViewer.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				ACE a = (ACE) cell.getElement();
				cell.setText(a.getLocalizedName());
			}
		});
		
		// edit on double-click
		TreeViewerEditor.create(treeViewer, new ColumnViewerEditorActivationStrategy(treeViewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event){
				return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
			}
		}, ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);
		
		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				ACE t1 = (ACE) e1;
				ACE t2 = (ACE) e2;
				return t1.getLocalizedName().compareToIgnoreCase(t2.getLocalizedName());
			}
		});
		
		TreeViewerColumn tvc_right = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn tc_right = tvc_right.getColumn();
		tc_right.setWidth(-1);
		tc_right.setWidth(280);
		tc_right.setText("Recht");
		tvc_right.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				ACE a = (ACE) cell.getElement();
				cell.setText(a.getLocalizedName());
			}
		});
		
		final CheckboxCellEditor cbce = new CheckboxCellEditor();
		final CellLabelProvider clp = new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				TreeColumn tc = tree.getColumn(cell.getColumnIndex());
				Role role = (Role) tc.getData("role");
				ACE ace = (ACE) cell.getElement();
				
				int val = determineChildAndSelfStates(role, ace);
				switch (val) {
				case 3:
					if (ace.getChildren(true).size() > 1) {
						cell.setText("A");
						cell.setForeground(UiDesk.getColor(UiDesk.COL_BLUE));
					} else {
						cell.setText("x");
					}
					
					break;
				case 2:
					cell.setText("v");
					break;
				case 1:
					cell.setText("x");
					break;
				default:
					cell.setText("");
					cell.setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
				}
				
			}
		};
		
		Query<Role> qbe = new Query<Role>(Role.class);
		List<Role> roles = qbe.execute();
		for (Role role : roles) {
			TreeViewerColumn tvc = new TreeViewerColumn(treeViewer, SWT.CENTER);
			tvc.getViewer().setData("role", role);
			TreeColumn tc = tvc.getColumn();
			tc.setData("role", role);
			tc.setWidth(20);
			tc.setText(role.getLabel().charAt(0) + "");
			tvc.setLabelProvider(clp);
			EditingSupport es = new EditingSupport(tvc.getViewer()) {
				
				@Override
				protected void setValue(Object element, Object value){
					Role role = (Role) tc.getData("role");
					ACE ace = (ACE) element;
					
					if (isChecked(ace, role)) {
						CoreHub.acl.revoke(role, ace);
					} else {
						CoreHub.acl.grant(role, ace);
					}
					
					refreshViewer();
				}
				
				@Override
				protected Object getValue(Object element){
					// irrelevant, not represented in UI;
					// done via LabelProvider, hence
					// we skip evaluation of this value
					return true;
				}
				
				@Override
				protected CellEditor getCellEditor(Object element){
					return cbce;
				}
				
				@Override
				protected boolean canEdit(Object element){
					return true;
				}
			};
			tvc.setEditingSupport(es);
		}
		
		sashForm.setWeights(new int[] {
			3, 7
		});
		
		Composite compositeBottom = new Composite(container, SWT.NONE);
		compositeBottom.setLayout(new GridLayout(1, false));
		compositeBottom.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		
		Link linkResetDefaults = new Link(compositeBottom, 0);
		linkResetDefaults.setText("<a>Standard-Rechte wiederherstellen</a>");
		linkResetDefaults.setBounds(0, 0, 43, 15);
		linkResetDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				boolean ret = MessageDialog.openQuestion(UiDesk.getTopShell(),
					"Standard-Rechte wiederherstellen", "Sind Sie sicher?");
				if (ret) {
					ACE.initializeACEDefaults(true);
					refreshViewer();
				}
			}
		});
		
		m_bindingContext = initDataBindings();
		wv.addValueChangeListener(this);
		treeViewer.setInput(ACE.getAllDefinedRootACElements());
		
		updateRolesList();
		
		return container;
	}
	
	private void updateRolesList(){
		Query<Role> qbe = new Query<Role>(Role.class);
		tableViewerRoles.setInput(qbe.execute());
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		// Initialize the preference page
	}
	
	// the user selected a different role
	@Override
	public void handleValueChange(ValueChangeEvent event){
		Role r = (Role) wv.getValue();
		txtRoleName.setText((r == null) ? "" : r.getRoleName());
	}
	
	@Override
	public void checkStateChanged(CheckStateChangedEvent event){
		Role r = (Role) wv.getValue();
		if (r == null)
			return;
		ACE ace = (ACE) event.getElement();
		
		boolean grayed = isGrayed(ace, r);
		if (grayed) {
			refreshViewer();
			return;
		}
		
		if (event.getChecked()) {
			CoreHub.acl.grant(r, ace);
		} else {
			CoreHub.acl.revoke(r, ace);
		}
		
		refreshViewer();
	}
	
	private void refreshViewer(){
		treeViewer.refresh();
	}
	
	private class ACETreeContentProvider implements ITreeContentProvider {
		
		@Override
		public void dispose(){}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
		
		@Override
		public Object[] getElements(Object inputElement){
			return (Object[]) inputElement;
		}
		
		@Override
		public Object[] getChildren(Object parentElement){
			ACE a = (ACE) parentElement;
			return ACE.getAllDefinedACElements().stream().filter(p -> p.getParent().equals(a))
				.toArray();
		}
		
		@Override
		public Object getParent(Object element){
			ACE a = (ACE) element;
			ACE parent = a.getParent();
			if (ACE.ACE_ROOT.equals(parent))
				return null;
			return parent;
		}
		
		@Override
		public boolean hasChildren(Object element){
			ACE a = (ACE) element;
			return ACE.getAllDefinedACElements().stream().filter(p -> p.getParent().equals(a))
				.count() > 0d;
		}
	}

	private boolean isGrayed(ACE ace, Role role){
		if (role == null || ace == null)
			return false;
			
		int state = determineChildAndSelfStates(role, ace);
		return (state == 1 || state == 2);
	}
	
	private boolean isChecked(ACE ace, Role r){
		if (r == null || ace == null)
			return false;
		int state = determineChildAndSelfStates(r, ace);
		return (state > 0);
	}
	
	/**
	 * Check the state of the current ACE its parents and children, where <code>state</code> is
	 * <ul>
	 * <li><code>0</code>: not allowed, none of the children, and no parents</li>
	 * <li><code>1</code>: allowed via grant to a parent</li>
	 * <li><code>2</code>: allowed by self</li>
	 * <li><code>3</code>: allowed, by entire chain (all children are allowed)</li>
	 * </ul>
	 * 
	 * @param r
	 * @param ace
	 * @return <code>state</code> as determined
	 */
	private int determineChildAndSelfStates(@NonNull Role r, @NonNull ACE ace){
		if (CoreHub.acl.request(r, ace.getParent()))
			return 1;
			
		List<ACE> chain = ace.getChildren(true);
		List<Boolean> chainRights = chain.stream().map(ace2 -> CoreHub.acl.request(r, ace2))
			.collect(Collectors.toList());
		long trues = chainRights.stream().filter(p -> p.booleanValue() == true).count();
		if (trues == 0)
			return 0;
		if (trues == chainRights.size())
			return 3;
		return 2;
	}
	
	protected DataBindingContext initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeTextTxti18nObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txti18n);
		IObservableValue wvTranslatedLabelObserveDetailValue =
			PojoProperties.value(Role.class, "translatedLabel", String.class).observeDetail(wv);
		bindingContext.bindValue(observeTextTxti18nObserveWidget,
			wvTranslatedLabelObserveDetailValue, null, null);
		//
		IObservableValue observeEnabledTxtRoleNameObserveWidget =
			WidgetProperties.enabled().observe(txtRoleName);
		IObservableValue wvSystemRoleObserveDetailValue =
			PojoProperties.value(Role.class, "systemRole", boolean.class).observeDetail(wv);
		UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setConverter(new BooleanNotConverter());
		bindingContext.bindValue(observeEnabledTxtRoleNameObserveWidget,
			wvSystemRoleObserveDetailValue,
			new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), strategy);
		//
		return bindingContext;
	}
}
