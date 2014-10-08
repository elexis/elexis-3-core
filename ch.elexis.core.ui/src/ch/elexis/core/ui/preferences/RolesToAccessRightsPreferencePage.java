package ch.elexis.core.ui.preferences;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.data.Role;
import org.eclipse.swt.custom.SashForm;

public class RolesToAccessRightsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
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
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewer treeViewerRoles = new TreeViewer(sashForm, SWT.BORDER);
		Tree treeRoles = treeViewerRoles.getTree();
		
		treeViewerRoles.setContentProvider(new RoleTreeContentProvider());
		treeViewerRoles.setLabelProvider(new LabelProvider());
		treeViewerRoles.setInput(Role.getRoot());
		
		TreeViewer treeViewer_1 = new TreeViewer(sashForm, SWT.BORDER);
		Tree treeACE = treeViewer_1.getTree();
		

		sashForm.setWeights(new int[] {3, 7});
		
		return container;
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		// Initialize the preference page
	}
	
}
