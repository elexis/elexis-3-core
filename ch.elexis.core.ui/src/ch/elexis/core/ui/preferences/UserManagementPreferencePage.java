package ch.elexis.core.ui.preferences;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.Role;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;

public class UserManagementPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private Table tableUsers;
	private Text text;
	private Tree treeRoles;
	private Table tableAssociation;
	
	/**
	 * Create the preference page.
	 */
	public UserManagementPreferencePage(){
		setTitle("Benutzerverwaltung");
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
		container.setLayout(new GridLayout(3, false));
		
		Composite compositeSelectorTable = new Composite(container, SWT.NONE);
		GridData gd_compositeSelectorTable = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_compositeSelectorTable.widthHint = 100;
		compositeSelectorTable.setLayoutData(gd_compositeSelectorTable);
		compositeSelectorTable.setLayout(new TableColumnLayout());
		
		TableViewer tableViewer =
			new TableViewer(compositeSelectorTable, SWT.BORDER | SWT.FULL_SELECTION);
		tableUsers = tableViewer.getTable();
		tableUsers.setLinesVisible(true);
		
		Composite compositeEdit = new Composite(container, SWT.NONE);
		GridLayout gl_compositeEdit = new GridLayout(2, false);
		gl_compositeEdit.marginHeight = 0;
		gl_compositeEdit.marginWidth = 0;
		compositeEdit.setLayout(gl_compositeEdit);
		compositeEdit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblBenutzername = new Label(compositeEdit, SWT.NONE);
		lblBenutzername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBenutzername.setText("Benutzername");
		
		text = new Text(compositeEdit, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPasswort = new Label(compositeEdit, SWT.NONE);
		lblPasswort.setText("Passwort");
		
		Link linkPassword = new Link(compositeEdit, SWT.NONE);
		linkPassword.setText("gesetzt <a>ändern</a>");
		
		Label lblKontakt = new Label(compositeEdit, SWT.NONE);
		lblKontakt.setText("Kontakt");
		
		Link link = new Link(compositeEdit, SWT.NONE);
		link.setText("nicht gesetzt <a>ändern</a>");
		
		Label lblZugang = new Label(compositeEdit, SWT.NONE);
		lblZugang.setText("Benutzertyp");
		
		Composite compositeAccessType = new Composite(compositeEdit, SWT.BORDER);
		compositeAccessType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeAccessType.setLayout(new GridLayout(1, false));
		
		Button btnIntern = new Button(compositeAccessType, SWT.CHECK);
		btnIntern.setText("Interner Benutzer");
		
		Button btnExtern = new Button(compositeAccessType, SWT.CHECK);
		btnExtern.setText("Externer Benutzer (Web-Zugang erlaubt)");
		
		Button btnIstVerantwortlicherArzt = new Button(compositeAccessType, SWT.CHECK);
		btnIstVerantwortlicherArzt.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnIstVerantwortlicherArzt.setText("verantwortlicher Arzt (Mandant)");
		
		new Label(compositeEdit, SWT.NONE);
		new Label(compositeEdit, SWT.NONE);
		
		Label lblRollen = new Label(compositeEdit, SWT.NONE);
		lblRollen.setText("Rollen");
		new Label(compositeEdit, SWT.NONE);
		
		Composite compositeRoles = new Composite(compositeEdit, SWT.NONE);
		compositeRoles.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		TreeViewer treeViewerRoles =
			new TreeViewer(compositeRoles, SWT.BORDER | SWT.CHECK);
		treeRoles = treeViewerRoles.getTree();
		
		treeViewerRoles.setContentProvider(new RoleTreeContentProvider());
		treeViewerRoles.setLabelProvider(new LabelProvider());
		treeViewerRoles.setInput(Role.getRoot());
		
		Label lblFrVerantwortlichenArzt = new Label(compositeEdit, SWT.NONE);
		lblFrVerantwortlichenArzt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblFrVerantwortlichenArzt.setText("Für verantwortlichen Arzt (Mandant)");
		
		Composite compositeAssociation = new Composite(compositeEdit, SWT.NONE);
		compositeAssociation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		compositeAssociation.setLayout(new TableColumnLayout());
		
		TableViewer tableViewerAssociation = new TableViewer(compositeAssociation, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		tableAssociation = tableViewerAssociation.getTable();
		tableAssociation.setLinesVisible(true);
		new Label(container, SWT.NONE);
		
		return container;
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		// Initialize the preference page
	}
}
