package ch.elexis.core.ui.preferences;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.RoleBasedAccessControl;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Query;
import ch.elexis.data.Role;

public class RolesToAccessRightsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, IValueChangeListener, ICheckStateListener {
	private DataBindingContext m_bindingContext;
	
	private WritableValue wv = new WritableValue(null, Role.class);
	
	private Text txti18n;
	private CheckboxTreeViewer checkboxTreeViewer;
	
	private ACETreeCheckStateProvider acecsp = new ACETreeCheckStateProvider();
	
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
		
		SashForm sashForm = new SashForm(container, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeRoles = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeRoles = new GridLayout(1, false);
		gl_compositeRoles.verticalSpacing = 2;
		gl_compositeRoles.marginWidth = 0;
		gl_compositeRoles.marginHeight = 0;
		compositeRoles.setLayout(gl_compositeRoles);
		
		TableViewer tableViewerRoles = new TableViewer(compositeRoles, SWT.BORDER);
		Table tableRoles = tableViewerRoles.getTable();
		tableRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		txti18n = new Text(compositeRoles, SWT.BORDER);
		txti18n.setMessage("Lokale Bezeichnung");
		txti18n.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txti18n.setBounds(0, 0, 64, 19);
		
		tableViewerRoles.setContentProvider(ArrayContentProvider.getInstance());
		tableViewerRoles.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex){
				Role r = (Role) element;
				String val = super.getColumnText(element, columnIndex);
				if (r.isSystemRole())
					val = val + " (S)";
				return val;
			}
			
		});
		Query<Role> qbe = new Query<Role>(Role.class);
		System.out.println(qbe.getActualQuery());
		tableViewerRoles.setInput(qbe.execute());
		tableViewerRoles.addSelectionChangedListener((e) -> {
			StructuredSelection ss = (StructuredSelection) e.getSelection();
			wv.setValue(ss == null ? null : ss.getFirstElement());
		});
		
		checkboxTreeViewer = new CheckboxTreeViewer(sashForm, SWT.BORDER);
		checkboxTreeViewer.setContentProvider(new ACETreeContentProvider());
		checkboxTreeViewer.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				ACE a = (ACE) cell.getElement();
				cell.setText(a.getLocalizedName());
			}
		});
		checkboxTreeViewer.setCheckStateProvider(acecsp);
		checkboxTreeViewer.addCheckStateListener(this);
		checkboxTreeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				ACE t1 = (ACE) e1;
				ACE t2 = (ACE) e2;
				return t1.getLocalizedName().compareToIgnoreCase(t2.getLocalizedName());
			}
			
		});
		
		sashForm.setWeights(new int[] {
			3, 7
		});
		
		m_bindingContext = initDataBindings();
		wv.addValueChangeListener(this);
		checkboxTreeViewer.setInput(ACE.getAllDefinedRootACElements());
		
		return container;
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		// Initialize the preference page
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
		return bindingContext;
	}
	
	// the user selected a different role
	@Override
	public void handleValueChange(ValueChangeEvent event){
		refreshViewer();
	}
	
	@Override
	public void checkStateChanged(CheckStateChangedEvent event){
		Role r = (Role) wv.getValue();
		if (r == null)
			return;
		ACE ace = (ACE) event.getElement();
		
		boolean grayed = acecsp.isGrayed(ace);
		if(grayed) {
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
	
	private void refreshViewer(){;
		checkboxTreeViewer.refresh();
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
	
	private class ACETreeCheckStateProvider implements ICheckStateProvider {
		
		@Override
		public boolean isChecked(Object element){
			ACE ace = (ACE) element;
			Role r = (Role) wv.getValue();
			if (r == null || ace == null)
				return false;
			
			int state = determineChildAndSelfStates(r, ace);
			return (state>0);
		}
		
		@Override
		public boolean isGrayed(Object element){
			ACE ace = (ACE) element;
			Role r = (Role) wv.getValue();
			if (r == null || ace == null)
				return false;
			
			int state = determineChildAndSelfStates(r, ace);
			return (state == 1 || state == 2);
		}
		
		/**
		 * Check the state of the current ACE its parents and children, where <code>state</code> is 
		 * <ul>
		 * <li><code>0</code>: not allowed, none of the children, and no parents</li>
		 * <li><code>1</code>: allowed via grant to a parent</li>
		 * <li><code>2</code>: allowed by self</li>
		 * <li><code>3</code>: allowed, by entire chain (all children are allowed)</li>
		 * </ul>
		 * @param r
		 * @param ace
		 * @return <code>state</code> as determined
		 */
		private int determineChildAndSelfStates(@NonNull Role r, @NonNull ACE ace){
			if(CoreHub.acl.request(r, ace.getParent())) return 1;
			
			List<ACE> chain = ace.getChildren(true);
			List<Boolean> chainRights =
				chain.stream().map(ace2 -> CoreHub.acl.request(r, ace2))
					.collect(Collectors.toList());
			long trues = chainRights.stream().filter(p->p.booleanValue()==true).count();
			if(trues==0) return 0;
			if(trues==chainRights.size()) return 3;
			return 2;
		}
	}
	
}
