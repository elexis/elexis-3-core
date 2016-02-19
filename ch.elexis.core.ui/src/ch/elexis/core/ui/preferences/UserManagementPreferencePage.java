package ch.elexis.core.ui.preferences;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.coolbar.MandantSelectionContributionItem;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.Role;
import ch.elexis.data.User;

public class UserManagementPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	private DataBindingContext m_bindingContext;
	private TableViewer tableViewerUsers;
	
	private WritableValue wvUser = new WritableValue(null, User.class);
	private WritableValue wvAnwender = new WritableValue(null, Anwender.class);
	private Text txtUsername;
	private Button btnIsExecutiveDoctor;
	private Label lblRespPhysColor;
	
	private Group grpAccounting;
	
	public static final String CHANGE_LINK = "<a>ändern</a>";
	private Link linkContact;
	private Text txtPassword;
	private Text txtPassword2;
	private CheckboxTableViewer checkboxTableViewerAssociation;
	private CheckboxTableViewer checkboxTableViewerRoles;
	private Link linkChangePassword;
	private Button btnUserIsAdmin;
	private Color lblRespPhysColorDefColor;
	private Link linkRechnungssteller;
	private MenuItem addUserMenuItem;
	private MenuItem deleteUserMenuItem;
	
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
		if (!CoreHub.acl.request(AccessControlDefaults.ACL_USERS)) {
			return new PrefAccessDenied(parent);
		}
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));
		
		Composite compositeSelectorTable = new Composite(container, SWT.NONE);
		GridData gd_compositeSelectorTable = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_compositeSelectorTable.widthHint = 130;
		compositeSelectorTable.setLayoutData(gd_compositeSelectorTable);
		TableColumnLayout tcl_compositeSelectorTable = new TableColumnLayout();
		compositeSelectorTable.setLayout(tcl_compositeSelectorTable);
		
		tableViewerUsers = new TableViewer(compositeSelectorTable, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewerUsers.setContentProvider(ArrayContentProvider.getInstance());
		Table tableUsers = tableViewerUsers.getTable();
		tableUsers.setLinesVisible(true);
		tableViewerUsers.addSelectionChangedListener(e -> {
			StructuredSelection ss = (StructuredSelection) e.getSelection();
			wvUser.setValue(ss == null ? null : ss.getFirstElement());
		});
		
		TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewerUsers, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tcl_compositeSelectorTable.setColumnData(tblclmnName, new ColumnWeightData(100));
		tableViewerColumnName.setLabelProvider(new AnwenderCellLabelProvider());
		
		Menu tableUsersMenu = new Menu(tableUsers);
		tableUsers.setMenu(tableUsersMenu);
		tableUsersMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e){
				StructuredSelection ss = (StructuredSelection) tableViewerUsers.getSelection();
				deleteUserMenuItem.setEnabled(!ss.isEmpty());
			}
		});
		
		addUserMenuItem = new MenuItem(tableUsersMenu, SWT.NONE);
		addUserMenuItem.setText("Benutzer hinzufügen");
		addUserMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				User newUser = new User(null, null, "");
				updateUserList();
				tableViewerUsers.setSelection(new StructuredSelection(newUser));
			}
		});
		
		deleteUserMenuItem = new MenuItem(tableUsersMenu, SWT.NONE);
		deleteUserMenuItem.setText("Benutzer löschen");
		deleteUserMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				StructuredSelection ss = (StructuredSelection) tableViewerUsers.getSelection();
				User u = (User) ss.getFirstElement();
				u.delete();
				updateUserList();
			}
		});
		
		Composite compositeEdit = new Composite(container, SWT.NONE);
		GridLayout gl_compositeEdit = new GridLayout(2, false);
		gl_compositeEdit.marginHeight = 0;
		gl_compositeEdit.marginWidth = 0;
		compositeEdit.setLayout(gl_compositeEdit);
		compositeEdit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group grpSysAccess = new Group(compositeEdit, SWT.NONE);
		grpSysAccess.setText("Systemzugang");
		grpSysAccess.setLayout(new GridLayout(4, false));
		grpSysAccess.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		Label lblBenutzername = new Label(grpSysAccess, SWT.NONE);
		lblBenutzername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBenutzername.setText("Benutzername");
		
		Composite compUsername = new Composite(grpSysAccess, SWT.NONE);
		compUsername.setLayout(new FillLayout(SWT.HORIZONTAL));
		compUsername.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		txtUsername = new Text(compUsername, SWT.BORDER);
		
		Link linkChangeUsername = new Link(compUsername, SWT.NONE);
		linkChangeUsername.setText(CHANGE_LINK);
		linkChangeUsername.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				String newUsername = txtUsername.getText();
				boolean isFree = User.verifyUsernameNotTaken(newUsername);
				if (isFree) {
					setErrorMessage(null);
					User u = (User) wvUser.getValue();
					User changedUser = u.setUsername(newUsername);
					updateUserList();
					tableViewerUsers.setSelection(new StructuredSelection(changedUser));
				} else {
					setErrorMessage("Dieser Username ist bereits vergeben.");
				}
			}
		});
		
		btnUserIsAdmin = new Button(grpSysAccess, SWT.CHECK);
		btnUserIsAdmin.setToolTipText("Administratoren unterliegen keinerlei Beschränkungen.");
		btnUserIsAdmin.setText("Administrator");
		
		Button btnUserIsLocked = new Button(grpSysAccess, SWT.CHECK);
		btnUserIsLocked.setToolTipText("Sperrt die Möglichkeit sich am System anzumelden.");
		btnUserIsLocked.setText("Gesperrt");
		
		Label lblPasswort = new Label(grpSysAccess, SWT.NONE);
		lblPasswort.setText("Passwort");
		
		Composite composite = new Composite(grpSysAccess, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPassword2 = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setToolTipText("Password hier eingeben. Nur * werden angezeigt");
		txtPassword2.setToolTipText("Zur Kontrolle Password hier nochmals eingeben. Nur * werden angezeigt");
		
		linkChangePassword = new Link(grpSysAccess, SWT.NONE);
		linkChangePassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		linkChangePassword.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				String pw1 = txtPassword.getText();
				String pw2 = txtPassword2.getText();
				if (pw1 != null && pw1.length() > 2 && pw1.equals(pw2)) {
					setErrorMessage(null);
					User u = (User) wvUser.getValue();
					u.setPassword(pw1);
					linkChangePassword.setText(CHANGE_LINK + " OK");
				} else {
					setErrorMessage(
						"Passwörter nicht ident, oder Passwort zu kurz (min 3 Zeichen)");
				}
			}
		});
		
		Composite sashComposite = new Composite(compositeEdit, SWT.NONE);
		sashComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		grpAccounting = new Group(sashComposite, SWT.NONE);
		grpAccounting.setText("Verrechnung");
		GridLayout gl_grpAccounting = new GridLayout(1, false);
		gl_grpAccounting.marginHeight = 0;
		grpAccounting.setLayout(gl_grpAccounting);
		
		Composite compositeContact = new Composite(grpAccounting, SWT.NONE);
		GridLayout gl_compositeContact = new GridLayout(2, false);
		gl_compositeContact.horizontalSpacing = 0;
		gl_compositeContact.verticalSpacing = 0;
		gl_compositeContact.marginWidth = 0;
		gl_compositeContact.marginHeight = 0;
		compositeContact.setLayout(gl_compositeContact);
		compositeContact.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Label lblKontakt = new Label(compositeContact, SWT.NONE);
		lblKontakt.setText("Kontakt");
		
		linkContact = new Link(compositeContact, SWT.NONE);
		linkContact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		linkContact.setText("nicht gesetzt " + CHANGE_LINK);
		linkContact.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				User user = (User) wvUser.getValue();
				if (user == null)
					return;
				KontaktSelektor ks =
					new KontaktSelektor(UiDesk.getTopShell(), Person.class, "Kontakt auswählen",
						"Bitte selektieren Sie den zugeordneten Kontakt", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Person p = (Person) ks.getSelection();
					user.setAssignedContact(p);
					linkContact.setText(p.getPersonalia() + " " + CHANGE_LINK);
				}
			}
		});
		
		btnIsExecutiveDoctor = new Button(grpAccounting, SWT.CHECK);
		btnIsExecutiveDoctor.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnIsExecutiveDoctor.setText("ist verantwortlicher Arzt");
		
		Composite compositeIsRespPhys = new Composite(grpAccounting, SWT.BORDER);
		compositeIsRespPhys.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compositeIsRespPhys = new GridLayout(2, false);
		gl_compositeIsRespPhys.marginHeight = 0;
		compositeIsRespPhys.setLayout(gl_compositeIsRespPhys);
		
		lblRespPhysColor = new Label(compositeIsRespPhys, SWT.NONE);
		lblRespPhysColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblRespPhysColor.setText("zugeordnete Farbe");
		lblRespPhysColorDefColor = lblRespPhysColor.getBackground();
		lblRespPhysColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e){
				if (!btnIsExecutiveDoctor.getSelection()) {
					return;
				}
				ColorDialog cd = new ColorDialog(UiDesk.getTopShell());
				cd.setRGB(lblRespPhysColor.getBackground().getRGB());
				cd.setText(Messages.UserManagementPreferencePage_MandatorColorSelectTitle);
				RGB rgb = cd.open();
				
				User user = (User) wvUser.getValue();
				Mandant m = Mandant.load(user.getAssignedContactId());
				UiMandant.setColorForMandator(m, rgb);
				lblRespPhysColor.setBackground(UiMandant.getColorForMandator(m));
			}
		});
		
		Label lblRechnungssteller = new Label(compositeIsRespPhys, SWT.NONE);
		lblRechnungssteller.setText("Rechnungssteller");
		
		linkRechnungssteller = new Link(compositeIsRespPhys, SWT.NONE);
		linkRechnungssteller.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		linkRechnungssteller.setText("nicht gesetzt " + CHANGE_LINK);
		linkRechnungssteller.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				User user = (User) wvUser.getValue();
				if (user == null)
					return;
				Anwender ac = user.getAssignedContact();
				if (ac == null || !ac.isExecutiveDoctor())
					return;
				KontaktSelektor ks =
					new KontaktSelektor(UiDesk.getTopShell(), Person.class, "Kontakt auswählen",
						"Bitte selektieren Sie den zugeordneten Kontakt", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Kontakt kontakt = (Kontakt) ks.getSelection();
					if (kontakt == null)
						return;
					Mandant mand = Mandant.load(ac.getId());
					mand.setRechnungssteller(kontakt);
					linkRechnungssteller
						.setText(mand.getRechnungssteller().getLabel() + " " + CHANGE_LINK);
				}
			}
		});
		
		Label lblFrVerantwortlichenArzt = new Label(grpAccounting, SWT.NONE);
		lblFrVerantwortlichenArzt
			.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblFrVerantwortlichenArzt.setText("tätig für");
		
		Composite compositeAssociation = new Composite(grpAccounting, SWT.NONE);
		compositeAssociation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		TableColumnLayout tcl_compositeAssociation = new TableColumnLayout();
		compositeAssociation.setLayout(tcl_compositeAssociation);
		
		checkboxTableViewerAssociation =
			CheckboxTableViewer.newCheckList(compositeAssociation, SWT.BORDER | SWT.FULL_SELECTION);
		checkboxTableViewerAssociation.addCheckStateListener((e) -> {
			Mandant m = (Mandant) e.getElement();
			if (m == null)
				return;
			User user = (User) wvUser.getValue();
			Anwender anw = user.getAssignedContact();
			if(anw!=null) {
				anw.addOrRemoveExecutiveDoctorWorkingFor(m, e.getChecked());
			} else {
				SWTHelper.showError("No contact assigned", "There is no contact assigned to user "+user.getLabel());
			}
		});
		
		Group grpRoles = new Group(sashComposite, SWT.NONE);
		grpRoles.setText("Rollenzuordnung");
		GridLayout gl_grpRoles = new GridLayout(2, false);
		gl_grpRoles.marginHeight = 0;
		grpRoles.setLayout(gl_grpRoles);
		
		Composite compositeRoles = new Composite(grpRoles, SWT.NONE);
		compositeRoles.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		checkboxTableViewerRoles =
			CheckboxTableViewer.newCheckList(compositeRoles, SWT.BORDER | SWT.FULL_SELECTION);
		new Label(compositeEdit, SWT.NONE);
		new Label(container, SWT.NONE);
		checkboxTableViewerRoles.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerRoles.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex){
				Role r = (Role) element;
				return r.getTranslatedLabel() != null ? r.getTranslatedLabel() : r.getId();
			}
		});
		checkboxTableViewerRoles.addCheckStateListener((e) -> {
			Role r = (Role) e.getElement();
			if (r == null)
				return;
			User user = (User) wvUser.getValue();
			user.setAssignedRole(r, e.getChecked());
		});
		checkboxTableViewerAssociation.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerAssociation.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex){
				Mandant m = (Mandant) element;
				return m.getName() + " " + m.getVorname();
			}
		});
		
		m_bindingContext = initDataBindings();
		
		wvUser.addValueChangeListener(new ValueChangedAdapter());
		
		updateUserList();
		
		return container;
	}
	
	/**
	 * Initialize the preference page.
	 */
	@Override
	public void init(IWorkbench workbench){
		// Initialize the preference page
	}
	
	private void updateUserList(){
		 List<User> query = new Query<User>(User.class).execute();
		 query.sort((u1, u2) -> u1.getLabel().compareTo(u2.getLabel()));
		 tableViewerUsers.setInput(query);
	}
	
	private class ValueChangedAdapter implements IValueChangeListener {
		
		@Override
		public void handleValueChange(ValueChangeEvent event){
			User user = (User) wvUser.getValue();
			if (user == null) {
				wvAnwender.setValue(null);
				return;
			}
			
			setErrorMessage(null);
			
			txtPassword.setText("");
			txtPassword2.setText("");
			txtUsername.setText(user.getUsername());
			linkChangePassword.setText(CHANGE_LINK + " (Passwort gesetzt)");
			
			Anwender anw = user.getAssignedContact();
			wvAnwender.setValue(anw);
			String text = (anw != null) ? anw.getPersonalia() : "Nicht gesetzt";
			linkContact.setText(text + " " + CHANGE_LINK);
			
			List<Role> roles = new Query<Role>(Role.class).execute();
			checkboxTableViewerRoles.setInput(roles);
			Object[] assignedRoles = user.getAssignedRoles().toArray();
			checkboxTableViewerRoles.setCheckedElements(assignedRoles);
			
			checkboxTableViewerAssociation.setInput(new Query<Mandant>(Mandant.class).execute());
			checkboxTableViewerAssociation.setCheckedElements(new Mandant[] {});
			
			linkRechnungssteller.setText("- " + CHANGE_LINK);
			lblRespPhysColor.setBackground(lblRespPhysColorDefColor);
			
			if (anw != null) {
				checkboxTableViewerAssociation
					.setCheckedElements(anw.getExecutiveDoctorsWorkingFor().toArray());
				if (anw.isExecutiveDoctor()) {
					Mandant m = Mandant.load(anw.getId());
					Color color = UiMandant.getColorForMandator(m);
					lblRespPhysColor.setBackground(color);
					
					Rechnungssteller rs = m.getRechnungssteller();
					String rst = (rs != null) ? rs.getLabel() : "Nicht gesetzt";
					linkRechnungssteller.setText(rst + " " + CHANGE_LINK);
				}
			}
		}
	}
	
	private class AnwenderCellLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell){
			User user = (User) cell.getElement();
			cell.setText(user.getLabel());
			if (user.isAdministrator()) {
				cell.setImage(Images.IMG_AUSRUFEZ.getImage());
			}
			Anwender ac = user.getAssignedContact();
			if (ac != null && ac.isExecutiveDoctor()) {
				Mandant m = Mandant.load(ac.getId());
				Color mc = UiMandant.getColorForMandator(m);
				cell.setImage(MandantSelectionContributionItem.getBoxSWTColorImage(mc));
			} else {
				cell.setImage(Images.IMG_EMPTY_TRANSPARENT.getImage());
			}
		}
	}
	
	protected DataBindingContext initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeSelectionBtnIsAdminObserveWidget =
			WidgetProperties.selection().observe(btnUserIsAdmin);
		IObservableValue wvAdminObserveDetailValue =
			PojoProperties.value(User.class, "administrator", boolean.class).observeDetail(wvUser);
		bindingContext.bindValue(observeSelectionBtnIsAdminObserveWidget, wvAdminObserveDetailValue,
			null, null);
		//
		IObservableValue observeSelectionBtnIsMandatorObserveWidget =
			WidgetProperties.selection().observe(btnIsExecutiveDoctor);
		IObservableValue wvMandatorObserveDetailValue = PojoProperties
			.value(Anwender.class, "executiveDoctor", boolean.class).observeDetail(wvAnwender);
		bindingContext.bindValue(observeSelectionBtnIsMandatorObserveWidget,
			wvMandatorObserveDetailValue, null, null);
		//
		return bindingContext;
	}
}
