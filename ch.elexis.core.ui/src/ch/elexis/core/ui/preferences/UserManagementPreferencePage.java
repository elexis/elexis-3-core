package ch.elexis.core.ui.preferences;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.ILocalLockService.Status;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.coolbar.MandantSelectionContributionItem;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;
import ch.elexis.core.ui.util.BooleanNotConverter;
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
		implements IWorkbenchPreferencePage, IUnlockable {
	private TableViewer tableViewerUsers;
	
	private WritableValue wvUser = new WritableValue(null, User.class);
	private WritableValue wvAnwender = new WritableValue(null, Anwender.class);
	private Label lblUsername;
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
	private RestrictedAction addUserAction, deleteUserAction, lockUserAction;
	private Button btnUserIsLocked;
	
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
		container.setLayout(new GridLayout(2, false));
		
		MenuManager popManager = new MenuManager();
		
		addUserAction =
			new RestrictedAction(AccessControlDefaults.USER_CREATE, Messages.LabGroupPrefs_add) {
				{
					setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				}
				
				@Override
				public void doRun(){
					IInputValidator iiv = new IInputValidator() {
						
						@Override
						public String isValid(String newText){
							if (newText.length() < 3) {
								return "Mindestens 3 Zeichen";
							}
							boolean allLetters =
								newText.chars().allMatch(x -> Character.isLetter(x));
							if (!allLetters) {
								return "Nur Buchstaben erlaubt";
							}
							boolean isFree = User.verifyUsernameNotTaken(newText);
							if (!isFree) {
								return "Benuterzname vergeben (evtl. für gelöschten Benutzer)";
							}
							
							return null;
						}
					};
					InputDialog id =
						new InputDialog(Hub.getActiveShell(), "Benutzernamen festlegen",
							"Benutzernamen festlegen - dieser kann nicht mehr geändert, sowie zukünftig anderweitig verwendet werden.",
							null, iiv);
					int retVal = id.open();
					if (retVal == Dialog.OK) {
						User newUser = new User(null, id.getValue(), "");
						updateUserList();
						tableViewerUsers.setSelection(new StructuredSelection(newUser));
					}
				}
			};
		popManager.add(addUserAction);
		
		deleteUserAction = new LockedRestrictedAction<User>(AccessControlDefaults.USER_DELETE,
			Messages.LabGroupPrefs_delete) {
			
			@Override
			public User getTargetedObject(){
				if (tableViewerUsers == null) {
					return null;
				}
				StructuredSelection ss = (StructuredSelection) tableViewerUsers.getSelection();
				return (ss != null) ? (User) ss.getFirstElement() : null;
			}
			
			@Override
			public void doRun(User user){
				user.delete();
				updateUserList();
				wvUser.setValue(null);
				wvAnwender.setValue(null);
			}
		};
		popManager.add(deleteUserAction);
		
		if (!(Status.STANDALONE == CoreHub.getLocalLockService().getStatus())) {
			lockUserAction = new RestrictedAction(AccessControlDefaults.USER_CREATE,
				Messages.Leistungscodes_editItem) {
				
				@Override
				public void doRun(){
					StructuredSelection ss = (StructuredSelection) tableViewerUsers.getSelection();
					User u = (User) ss.getFirstElement();
					LockResponse acquireLock = CoreHub.getLocalLockService().acquireLock(u);
					if (acquireLock.isOk()) {
						setUnlocked(true);
					}
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return Images.IMG_LOCK_OPEN.getImageDescriptor();
				}
			};
			popManager.add(lockUserAction);
		}
		
		popManager.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				addUserAction.reflectRight();
				deleteUserAction.reflectRight();
				if (lockUserAction != null) {
					lockUserAction.reflectRight();
				}
			}
		});
		
		Composite compositeLeft = new Composite(container, SWT.NONE);
		compositeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compositeLeft = new GridLayout(1, false);
		gl_compositeLeft.marginWidth = 0;
		gl_compositeLeft.marginHeight = 0;
		compositeLeft.setLayout(gl_compositeLeft);
		
		Composite compositeButtons = new Composite(compositeLeft, SWT.None);
		compositeButtons.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAdd = new Button(compositeButtons, SWT.FLAT);
		btnAdd.setImage(Images.IMG_NEW.getImage());
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				addUserAction.doRun();
			}
		});
		
		if (!(Status.STANDALONE == CoreHub.getLocalLockService().getStatus())) {
			Button btnLock = new Button(compositeButtons, SWT.FLAT | SWT.TOGGLE);
			btnLock.setSelection(
				CoreHub.getLocalLockService().isLocked((IPersistentObject) wvUser.getValue()));
			btnLock.setImage(Images.IMG_LOCK_OPEN.getImage());
			btnLock.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					IPersistentObject user = (IPersistentObject) wvUser.getValue();
					if (CoreHub.getLocalLockService().isLocked(user)) {
						CoreHub.getLocalLockService().releaseLock(user);
					} else {
						lockUserAction.doRun();
					}
					boolean locked = CoreHub.getLocalLockService()
						.isLocked((IPersistentObject) wvUser.getValue());
					btnLock.setSelection(locked);
					setUnlocked(locked);
				}
			});
		}
		
		Composite compositeSelectorTable = new Composite(compositeLeft, SWT.NONE);
		compositeSelectorTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_compositeSelectorTable = new TableColumnLayout();
		compositeSelectorTable.setLayout(tcl_compositeSelectorTable);
		
		tableViewerUsers = new TableViewer(compositeSelectorTable, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewerUsers.setContentProvider(ArrayContentProvider.getInstance());
		Table tableUsers = tableViewerUsers.getTable();
		tableUsers.setLinesVisible(true);
		tableViewerUsers.addSelectionChangedListener(e -> {
			releaseLockIfRequired();
			
			StructuredSelection ss = (StructuredSelection) e.getSelection();
			wvUser.setValue(ss == null ? null : ss.getFirstElement());
			setUnlocked(Status.STANDALONE == CoreHub.getLocalLockService().getStatus());
		});
		
		TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewerUsers, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tcl_compositeSelectorTable.setColumnData(tblclmnName, new ColumnWeightData(100));
		tableViewerColumnName.setLabelProvider(new AnwenderCellLabelProvider());
		
		Menu menu = popManager.createContextMenu(tableUsers);
		tableUsers.setMenu(menu);
		
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
		
		lblUsername = new Label(grpSysAccess, SWT.BORDER);
		lblUsername.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		btnUserIsAdmin = new Button(grpSysAccess, SWT.CHECK);
		btnUserIsAdmin.setToolTipText("Administratoren unterliegen keinerlei Beschränkungen.");
		btnUserIsAdmin.setText("Administrator");
		
		btnUserIsLocked = new Button(grpSysAccess, SWT.CHECK);
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
		txtPassword2.setToolTipText(
			"Zur Kontrolle Password hier nochmals eingeben. Nur * werden angezeigt");
		
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
				updateUserList();
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
			if (anw != null) {
				anw.addOrRemoveExecutiveDoctorWorkingFor(m, e.getChecked());
			} else {
				SWTHelper.showError("No contact assigned",
					"There is no contact assigned to user " + user.getLabel());
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
		
		initDataBindings();
		
		wvUser.addValueChangeListener(new ValueChangedAdapter());
		
		updateUserList();
		
		setUnlocked(Status.STANDALONE == CoreHub.getLocalLockService().getStatus());
		
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
			lblUsername.setText(user.getUsername());
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
			Anwender ac = user.getAssignedContact();
			if (ac != null && ac.isExecutiveDoctor()) {
				Mandant m = Mandant.load(ac.getId());
				Color mc = UiMandant.getColorForMandator(m);
				cell.setImage(MandantSelectionContributionItem.getBoxSWTColorImage(mc));
			} else {
				cell.setImage(Images.IMG_EMPTY_TRANSPARENT.getImage());
			}
			if (user.isAdministrator()) {
				cell.setForeground(UiDesk.getColor(UiDesk.COL_RED));
				cell.setImage(Images.IMG_AUSRUFEZ.getImage());
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
		IObservableValue observeSelectionBtnIsActiveObserveWidget =
			WidgetProperties.selection().observe(btnUserIsLocked);
		IObservableValue wvActiveObserveDetailValue =
			PojoProperties.value(User.class, "active", boolean.class).observeDetail(wvUser);
		bindingContext.bindValue(observeSelectionBtnIsActiveObserveWidget,
			wvActiveObserveDetailValue,
			new UpdateValueStrategy().setConverter(new BooleanNotConverter()),
			new UpdateValueStrategy().setConverter(new BooleanNotConverter()));
		//
		return bindingContext;
	}
	
	@Override
	protected void performApply(){
		releaseLockIfRequired();
		super.performApply();
	}
	
	@Override
	public boolean performOk(){
		releaseLockIfRequired();
		return super.performOk();
	}
	
	@Override
	public boolean performCancel(){
		releaseLockIfRequired();
		return super.performCancel();
	}
	
	private void releaseLockIfRequired(){
		User user = (User) wvUser.getValue();
		if (user != null && CoreHub.getLocalLockService().isLocked(user)) {
			CoreHub.getLocalLockService().releaseLock(user);
		}
	}
	
	@Override
	public void setUnlocked(boolean unlocked){
		btnIsExecutiveDoctor.setEnabled(unlocked);
		txtPassword.setEnabled(unlocked);
		txtPassword2.setEnabled(unlocked);
		linkChangePassword.setEnabled(unlocked);
		linkContact.setEnabled(unlocked);
		linkRechnungssteller.setEnabled(unlocked);
		btnUserIsAdmin.setEnabled(unlocked);
		btnUserIsLocked.setEnabled(unlocked);
		checkboxTableViewerAssociation.getTable().setEnabled(unlocked);
		checkboxTableViewerRoles.getTable().setEnabled(unlocked);
		lblRespPhysColor.setEnabled(unlocked);
	}
}
