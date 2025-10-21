package ch.elexis.core.ui.preferences;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.AccessControlList;
import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.services.holder.UserServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.coolbar.MandantSelectionContributionItem;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.databinding.SavingUpdateValueStrategy;
import ch.elexis.core.ui.dialogs.ChangePasswordDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.util.BooleanNotConverter;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Person;

public class UserManagementPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IUnlockable {
	private TableViewer tableViewerUsers;

	private List<IUserGroup> userGroups = Collections.emptyList();

	private WritableValue<IUser> wvUser = new WritableValue<>(null, IUser.class);
	private WritableValue<IContact> wvUserContact = new WritableValue<>(null, IContact.class);
	private Button btnIsExecutiveDoctor;
	private Label lblRespPhysColor;

	private Group grpAccounting;

	public static final String CHANGE_LINK = "<a>ändern</a>";
	private Link linkContact;
	private CheckboxTableViewer checkboxTableViewerAssociation;
	private CheckboxTableViewer checkboxTableViewerRoles;
	private Link linkChangePassword;
	private Button btnUserIsAdmin;
	private Button btnMandatorIsInactive;
	private Color lblRespPhysColorDefColor;
	private Link linkRechnungssteller;
	private RestrictedAction addUserAction, deleteUserAction, lockUserAction;
	private Button btnUserIsLocked;
	private Label userInfoLabel;
	private boolean isShowOnlyActive = true;
	private Composite compositeAssociation;

	/**
	 * Create the preference page.
	 */
	public UserManagementPreferencePage() {
		setTitle("Benutzerverwaltung");
		noDefaultAndApplyButton();
	}

	/**
	 * Create contents of the preference page.
	 *
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(2, false));

		MenuManager popManager = new MenuManager();

		addUserAction = new RestrictedAction(EvACE.of(IUser.class, Right.CREATE), Messages.Core_Add_ellipsis) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			}

			@Override
			public void doRun() {
				IInputValidator iiv = new IInputValidator() {

					@Override
					public String isValid(String newText) {
						if (newText.length() < 3) {
							return "Mindestens 3 Zeichen";
						}
						boolean allLettersOrDigits = newText.chars().allMatch(x -> Character.isLetterOrDigit(x));
						if (!allLettersOrDigits) {
							return "Nur Buchstaben und Zahlen erlaubt";
						}
						boolean isFree = UserServiceHolder.get().verifyUsernameNotTaken(newText);
						if (!isFree) {
							return "Benuterzname vergeben (evtl. für gelöschten Benutzer)";
						}

						return null;
					}
				};
				InputDialog id = new InputDialog(Hub.getActiveShell(), "Benutzernamen festlegen",
						"Benutzernamen festlegen - dieser kann nicht mehr geändert, sowie zukünftig anderweitig verwendet werden.",
						null, iiv);
				int retVal = id.open();
				if (retVal == Dialog.OK) {
					IUser newUser = new IUserBuilder(CoreModelServiceHolder.get(), id.getValue(), null).buildAndSave();
					updateUserList();
					tableViewerUsers.setSelection(new StructuredSelection(newUser));
				}
			}
		};
		popManager.add(addUserAction);

		deleteUserAction = new LockedRestrictedAction<IUser>(EvACE.of(IUser.class, Right.DELETE),
				Messages.Core_Delete) {

			@Override
			public IUser getTargetedObject() {
				if (tableViewerUsers == null) {
					return null;
				}
				StructuredSelection ss = (StructuredSelection) tableViewerUsers.getSelection();
				return (ss != null) ? (IUser) ss.getFirstElement() : null;
			}

			@Override
			public void doRun(IUser user) {
				IUser currentUser = ContextServiceHolder.get().getActiveUser().orElse(null);
				if (currentUser != null) {
					if (currentUser.getId().equals(user.getId())) {
						
						MessageDialog.openWarning(getShell(), "Warnung",
								"Dieser Benutzer ist gerade eingeloggt und kann daher nicht entfernt werden!");
					} else {
						CoreModelServiceHolder.get().delete(user);
						updateUserList();
						wvUser.setValue(null);
						wvUserContact.setValue(null);
					}
				}
			}
		};
		popManager.add(deleteUserAction);

		if (!(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus())) {
			lockUserAction = new RestrictedAction(EvACE.of(IUser.class, Right.UPDATE),
					Messages.Leistungscodes_editItem) {

				@Override
				public void doRun() {
					StructuredSelection ss = (StructuredSelection) tableViewerUsers.getSelection();
					IUser u = (IUser) ss.getFirstElement();
					LockResponse acquireLock = LocalLockServiceHolder.get().acquireLock(u);
					if (acquireLock.isOk()) {
						setUnlocked(true);
					}
				}

				@Override
				public ImageDescriptor getImageDescriptor() {
					return Images.IMG_LOCK_OPEN.getImageDescriptor();
				}
			};
			popManager.add(lockUserAction);
		}

		popManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				addUserAction.reflectRight();
				deleteUserAction.reflectRight();
				if (lockUserAction != null) {
					lockUserAction.reflectRight();
				}
			}
		});
		SashForm sash = new SashForm(container, SWT.HORIZONTAL);
		sash.setLayout(new GridLayout(2, false));
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		Composite compositeLeft = new Composite(sash, SWT.NONE);
		compositeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compositeLeft = new GridLayout(1, false);
		gl_compositeLeft.marginWidth = 0;
		gl_compositeLeft.marginHeight = 0;
		compositeLeft.setLayout(gl_compositeLeft);

		Composite compositeButtons = new Composite(compositeLeft, SWT.NONE);
		GridLayout gridLayoutButtons = new GridLayout(3, false);
		gridLayoutButtons.marginWidth = 0;
		gridLayoutButtons.marginHeight = 0;
		gridLayoutButtons.horizontalSpacing = 0;
		compositeButtons.setLayout(gridLayoutButtons);
		compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnAdd = new Button(compositeButtons, SWT.FLAT);
		btnAdd.setImage(Images.IMG_NEW.getImage());
		GridData gd_btnAdd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_btnAdd.widthHint = 30;
		btnAdd.setLayoutData(gd_btnAdd);
		btnAdd.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        addUserAction.doRun();
		    }
		});

		Button btnToggleUserFilter = new Button(compositeButtons, SWT.PUSH);
		btnToggleUserFilter.setImage(Images.IMG_EYE_WO_SHADOW.getImage());
		btnToggleUserFilter.setText("Alle User");//$NON-NLS-1$
		GridData gd_btnToggleUserFilter = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		btnToggleUserFilter.setLayoutData(gd_btnToggleUserFilter);

		btnToggleUserFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isShowOnlyActive = !isShowOnlyActive;
				updateUserList(); 
				updateAssociations();
				btnToggleUserFilter.setText(isShowOnlyActive ? "Alle User" : "Aktive User");//$NON-NLS-1$ //$NON-NLS-2$
				btnToggleUserFilter.setImage(
						isShowOnlyActive ? Images.IMG_EYE_WO_SHADOW.getImage() : Images.IMG_REMOVEITEM.getImage());
				resetAll();

			}
		});

		if (!(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus())) {
			Button btnLock = new Button(compositeButtons, SWT.FLAT | SWT.TOGGLE);
			btnLock.setSelection(LocalLockServiceHolder.get().isLocked(wvUser.getValue()));
			btnLock.setImage(Images.IMG_LOCK_OPEN.getImage());
			btnLock.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IUser user = wvUser.getValue();
					if (LocalLockServiceHolder.get().isLocked(user)) {
						LocalLockServiceHolder.get().releaseLock(user);
					} else {
						lockUserAction.doRun();
					}
					boolean locked = LocalLockServiceHolder.get().isLocked(wvUser.getValue());
					btnLock.setSelection(locked);
					setUnlocked(locked);
				}
			});
		}

		Composite compositeSelectorTable = new Composite(compositeLeft, SWT.NONE);
		compositeSelectorTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_compositeSelectorTable = new TableColumnLayout();
		compositeSelectorTable.setLayout(tcl_compositeSelectorTable);

		Composite compositeEdit = new Composite(sash, SWT.NONE);
		GridLayout gl_compositeEdit = new GridLayout(2, false);
		gl_compositeEdit.horizontalSpacing = 0;
		gl_compositeEdit.verticalSpacing = 0;
		gl_compositeEdit.marginWidth = 0;
		gl_compositeEdit.marginHeight = 0;
		compositeEdit.setLayout(gl_compositeEdit);
		compositeEdit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		userInfoLabel = new Label(compositeEdit, SWT.NONE);
		userInfoLabel.setFont(SWTResourceManager.getFont(".AppleSystemUIFont", 14, SWT.BOLD)); //$NON-NLS-1$
		userInfoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(compositeEdit, SWT.None);

		tableViewerUsers = new TableViewer(compositeSelectorTable, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewerUsers.setContentProvider(ArrayContentProvider.getInstance());
		Table tableUsers = tableViewerUsers.getTable();
		tableUsers.setLinesVisible(true);
		tableViewerUsers.addSelectionChangedListener(e -> {
			releaseLockIfRequired();

			StructuredSelection ss = (StructuredSelection) e.getSelection();
			wvUser.setValue(ss == null ? null : (IUser) ss.getFirstElement());
			setUnlocked(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus());

			compositeEdit.layout(true, true);
		});

		TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewerUsers, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tcl_compositeSelectorTable.setColumnData(tblclmnName, new ColumnWeightData(100));
		tableViewerColumnName.setLabelProvider(new AnwenderCellLabelProvider());

		Menu menu = popManager.createContextMenu(tableUsers);
		tableUsers.setMenu(menu);
		new Label(compositeEdit, SWT.NONE);
		new Label(compositeEdit, SWT.NONE);

		Group grpSysAccess = new Group(compositeEdit, SWT.NONE);
		grpSysAccess.setText("Systemzugang");
		grpSysAccess.setLayout(new GridLayout(3, true));
		grpSysAccess.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		linkChangePassword = new Link(grpSysAccess, SWT.NONE);
		linkChangePassword.setText("<a>Passwort ändern</a>");
		linkChangePassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		linkChangePassword.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IUser user = wvUser.getValue();
				if (user == null) {
					return;
				}
				new ChangePasswordDialog(UiDesk.getTopShell(), user).open();
			}
		});

		btnUserIsAdmin = new Button(grpSysAccess, SWT.CHECK);
		btnUserIsAdmin.setToolTipText("Administratoren unterliegen keinerlei Beschränkungen.");
		btnUserIsAdmin.setText("Administrator");

		btnUserIsLocked = new Button(grpSysAccess, SWT.CHECK);
		btnUserIsLocked.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnUserIsLocked.setToolTipText("Sperrt die Möglichkeit sich am System anzumelden.");
		btnUserIsLocked.setText("Zugang sperren");

		grpAccounting = new Group(compositeEdit, SWT.NONE);
		grpAccounting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
		grpAccounting.setText("Verrechnung");
		GridLayout gl_grpAccounting = new GridLayout(1, false);
		gl_grpAccounting.marginHeight = 5;
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
		lblKontakt.setText("Kontakt: ");

		linkContact = new Link(compositeContact, SWT.NONE);
		linkContact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		linkContact.setText("nicht gesetzt " + CHANGE_LINK);
		linkContact.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IUser user = wvUser.getValue();
				if (user == null) {
					return;
				}
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Person.class, "Kontakt auswählen",
						"Bitte selektieren Sie den zugeordneten Kontakt", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					IPerson p = NoPoUtil.loadAsIdentifiable((Person) ks.getSelection(), IPerson.class).get();
					p.setUser(true);
					user.setAssignedContact(p);
					linkContact.setText(p.getLabel() + StringUtils.SPACE + CHANGE_LINK);
					CoreModelServiceHolder.get().save(p);
					CoreModelServiceHolder.get().save(user);
				}
			}
		});

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
			public void mouseDown(MouseEvent e) {
				if (!btnIsExecutiveDoctor.getSelection()) {
					return;
				}
				ColorDialog cd = new ColorDialog(UiDesk.getTopShell());
				cd.setRGB(lblRespPhysColor.getBackground().getRGB());
				cd.setText(Messages.UserManagementPreferencePage_MandatorColorSelectTitle);
				RGB rgb = cd.open();

				IUser user = wvUser.getValue();
				if (user.getAssignedContact() != null) {
					Optional<IMandator> mandator = CoreModelServiceHolder.get().load(user.getAssignedContact().getId(),
							IMandator.class);
					if (mandator.isPresent()) {
						UiMandant.setColorForMandator(Mandant.load(mandator.get().getId()), rgb);
						lblRespPhysColor
								.setBackground(UiMandant.getColorForMandator(Mandant.load(mandator.get().getId())));
					}
				}
				updateUserList();
			}
		});

		Label lblRechnungssteller = new Label(compositeIsRespPhys, SWT.NONE);
		lblRechnungssteller.setText("Rechnungssteller:");

		linkRechnungssteller = new Link(compositeIsRespPhys, SWT.NONE);
		linkRechnungssteller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		linkRechnungssteller.setText("nicht gesetzt " + CHANGE_LINK);
		linkRechnungssteller.setToolTipText("Set the invoice contact for this mandator");
		linkRechnungssteller.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IUser user = wvUser.getValue();
				if (user == null) {
					return;
				}
				Optional<IMandator> mandator = Optional.empty();
				if (user.getAssignedContact() != null) {
					mandator = CoreModelServiceHolder.get().load(user.getAssignedContact().getId(), IMandator.class);
				}
				if (!mandator.isPresent()) {
					MessageDialog.openWarning(UiDesk.getTopShell(), "Fehler",
							"Der selektierte Kontakt ist kein Mandant.");
					return;
				}

				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Kontakt.class,
						"Rechnungs-Kontakt auswählen",
						"Bitte selektieren Sie den dem Mandant zugeordneten Rechnungs-Kontakt", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Kontakt kontakt = (Kontakt) ks.getSelection();
					if (kontakt == null)
						return;
					mandator.get().setBiller(NoPoUtil.loadAsIdentifiable(kontakt, IContact.class).get());
					CoreModelServiceHolder.get().save(mandator.get());
					linkRechnungssteller
							.setText(mandator.get().getBiller().getLabel() + StringUtils.SPACE + CHANGE_LINK);
				}
			}
		});

		Composite compositeMandator = new Composite(grpAccounting, SWT.NONE);
		GridLayout gl_compositeMandator = new GridLayout(2, true);
		gl_compositeMandator.verticalSpacing = 0;
		gl_compositeMandator.horizontalSpacing = 0;
		gl_compositeMandator.marginHeight = 0;
		gl_compositeMandator.marginWidth = 0;
		compositeMandator.setLayout(gl_compositeMandator);
		compositeMandator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		btnIsExecutiveDoctor = new Button(compositeMandator, SWT.CHECK);
		btnIsExecutiveDoctor.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		btnIsExecutiveDoctor.setText("ist verantwortlicher Arzt");

		btnIsExecutiveDoctor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnIsExecutiveDoctor.getSelection()) {
					IUser user = wvUser.getValue();
					if (user == null) {
						return;
					}
					IContact ac = user.getAssignedContact();
					if (ac == null) {
						return;
					}
					Optional<IMandator> mandator = CoreModelServiceHolder.get().load(ac.getId(), IMandator.class);
					if (!mandator.isPresent()) {
						boolean changeIt = MessageDialog.openQuestion(UiDesk.getTopShell(), "Kontakt ist kein Mandant",
								"Der selektierte Kontakt ist kein Mandant. Wollen Sie diesen Kontakt in einen Mandanten ändern?");
						if (changeIt) {
							ac.setMandator(true);
							CoreModelServiceHolder.get().save(ac);
						}
						btnIsExecutiveDoctor.setSelection(changeIt);
					}
				}
				btnMandatorIsInactive.setEnabled(btnIsExecutiveDoctor.getSelection());
			}
		});

		btnMandatorIsInactive = new Button(compositeMandator, SWT.CHECK);
		btnMandatorIsInactive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnMandatorIsInactive.setText("ehemalig (Verrechn. sperren)");

		btnMandatorIsInactive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IUser user = wvUser.getValue();
				if (user == null) {
					return;
				}
				Optional<IMandator> mandator = Optional.empty();
				if (user.getAssignedContact() != null) {
					mandator = CoreModelServiceHolder.get().load(user.getAssignedContact().getId(), IMandator.class);
				}
				if (!mandator.isPresent()) {
					btnMandatorIsInactive.setSelection(false);
					return;
				}

				if (btnMandatorIsInactive.getSelection()
						&& SWTHelper.askYesNo("Mandanten deaktivieren", mandator.get().getDescription1() + " "
								+ mandator.get().getDescription2() + " wirklich deaktivieren?")) {
					btnMandatorIsInactive.setEnabled(true);
				} else {
					btnMandatorIsInactive.setSelection(false);
				}
				mandator.get().setActive(!btnMandatorIsInactive.getSelection());
				CoreModelServiceHolder.get().save(mandator.get());
			}
		});

		Composite compositeAccounting = new Composite(grpAccounting, SWT.NONE);
		compositeAccounting.setLayout(new GridLayout(2, true));
		compositeAccounting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Group grpAssociation = new Group(compositeAccounting, SWT.NONE);
		grpAssociation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpAssociation.setText("tätig für");
		GridLayout gl_grp = new GridLayout(2, false);
		gl_grp.marginHeight = 0;
		grpAssociation.setLayout(gl_grp);

		compositeAssociation = new Composite(grpAssociation, SWT.NONE);
		compositeAssociation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		TableColumnLayout tcl_compositeAssociation = new TableColumnLayout();
		compositeAssociation.setLayout(tcl_compositeAssociation);

		checkboxTableViewerAssociation = CheckboxTableViewer.newCheckList(compositeAssociation,
				SWT.BORDER | SWT.FULL_SELECTION);
		checkboxTableViewerAssociation.addCheckStateListener((e) -> {
			IMandator m = (IMandator) e.getElement();
			if (m == null)
				return;
			IUser user = wvUser.getValue();
			if (user.getAssignedContact() != null) {
				UserServiceHolder.get().addOrRemoveExecutiveDoctorWorkingFor(user, m, e.getChecked());
			} else {
				SWTHelper.showError("No contact assigned", "There is no contact assigned to user " + user.getLabel());
			}
		});
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = checkboxTableViewerAssociation.getStructuredSelection();
				if (!selection.isEmpty()) {
					Object selected = selection.getFirstElement();
					if (selected instanceof IMandator) {
						IUser user = wvUser.getValue();
						if (user != null) {
							if (user.getAssignedContact() != null) {
								IMandator stdWorkingFor = UserServiceHolder.get()
										.getDefaultExecutiveDoctorWorkingFor(user).orElse(null);
								if (stdWorkingFor != null && stdWorkingFor.equals(selected)) {
									manager.add(new Action() {
										@Override
										public String getText() {
											return "Std. Mandant entfernen";
										};

										@Override
										public void run() {
											UserServiceHolder.get().setDefaultExecutiveDoctorWorkingFor(user, null);
											checkboxTableViewerAssociation.refresh();
										};
									});
								} else {
									manager.add(new Action() {
										@Override
										public String getText() {
											return "Std. Mandant setzen";
										};

										@Override
										public void run() {
											UserServiceHolder.get().setDefaultExecutiveDoctorWorkingFor(user,
													(IMandator) selected);
											checkboxTableViewerAssociation.refresh();
										};
									});
								}
							}
						}
					}
				}
			}
		});
		Menu contextMenu = menuManager.createContextMenu(checkboxTableViewerAssociation.getTable());
		checkboxTableViewerAssociation.getTable().setMenu(contextMenu);

		Group grpRoles = new Group(compositeAccounting, SWT.NONE);
		grpRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpRoles.setText("Rollenzuordnung");
		GridLayout gl_grpRoles = new GridLayout(2, false);
		gl_grpRoles.marginHeight = 0;
		grpRoles.setLayout(gl_grpRoles);

		Composite compositeRoles = new Composite(grpRoles, SWT.NONE);
		compositeRoles.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		MenuManager rolePopManager = new MenuManager();
		rolePopManager.add(new Action() {

			@Override
			public void run() {
				List<String> existingRoleIds = CoreModelServiceHolder.get().getQuery(IRole.class).execute().stream()
						.map(r -> r.getId()).collect(Collectors.toList());

				InputDialog dialog = new InputDialog(getShell(), "Neue Rolle", "Rollen Name", null,
						new IInputValidator() {

							@Override
							public String isValid(String newText) {
								if (StringUtils.isBlank(newText)) {
									return "Rollen Name kann nicht leer sein.";
								} else if (existingRoleIds.contains(newText)) {
									return "Rollen mit Name existiert bereits.";
								}
								return null;
							}
						});
				if (dialog.open() == Dialog.OK) {
					IRole role = CoreModelServiceHolder.get().create(IRole.class);
					role.setId(dialog.getValue());
					role.setSystemRole(false);
					CoreModelServiceHolder.get().save(role);
					updateRoles();
				}
			};

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_NEW.getImageDescriptor();
			};

			@Override
			public String getText() {
				return "Neue Rolle";
			};
		});
		rolePopManager.add(new Action() {

			@Override
			public void run() {
				IRole role = (IRole) checkboxTableViewerRoles.getStructuredSelection().getFirstElement();
				// remove from all users
				for (IUser user : CoreModelServiceHolder.get().getQuery(IUser.class).execute()) {
					if (user.getRoles().contains(role)) {
						user.getRoles().remove(role);
						CoreModelServiceHolder.get().save(user);
					}
				}
				CoreModelServiceHolder.get().remove(role);
				updateRoles();
			};

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_DELETE.getImageDescriptor();
			};

			@Override
			public String getText() {
				return "Rolle entfernen";
			};

			@Override
			public boolean isEnabled() {
				if (checkboxTableViewerRoles != null && checkboxTableViewerRoles.getStructuredSelection() != null
						&& !checkboxTableViewerRoles.getStructuredSelection().isEmpty()) {
					return !((IRole) checkboxTableViewerRoles.getStructuredSelection().getFirstElement())
							.isSystemRole();
				}
				return false;
			};
		});
		rolePopManager.add(new Action() {

			@Override
			public void run() {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setFilterExtensions(new String[] { "*.json" });
				if (dialog.open() != null) {
					File file = new File(dialog.getFilterPath() + File.separator + dialog.getFileName());
					if (file.exists()) {
						try {
							String jsonContent = FileUtils.readFileToString(file, "UTF-8");

							Optional<AccessControlList> acl = AccessControlServiceHolder.get()
									.readAccessControlList(new ByteArrayInputStream(jsonContent.getBytes("UTF-8")));
							if (acl.isPresent()) {
								IRole role = (IRole) checkboxTableViewerRoles.getStructuredSelection()
										.getFirstElement();
								role.setExtInfo("json", jsonContent);
								CoreModelServiceHolder.get().save(role);
								updateRoles();
							} else {
								MessageDialog.openError(getShell(), "Fehler", "Fehlerhafter Berechtigungs Definition");
							}
						} catch (IOException e) {
							LoggerFactory.getLogger(getClass()).error("Error reading json file", e);
						}
					}
				}
			};

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_IMPORT.getImageDescriptor();
			};

			@Override
			public String getText() {
				return "Berechtigungen importieren";
			};

			@Override
			public boolean isEnabled() {
				if (checkboxTableViewerRoles != null && checkboxTableViewerRoles.getStructuredSelection() != null
						&& !checkboxTableViewerRoles.getStructuredSelection().isEmpty()) {
					return !((IRole) checkboxTableViewerRoles.getStructuredSelection().getFirstElement())
							.isSystemRole();
				}
				return false;
			};
		});
		rolePopManager.add(new Action() {

			@Override
			public void run() {
				IRole role = (IRole) checkboxTableViewerRoles.getStructuredSelection().getFirstElement();
				String jsonString = (String) role.getExtInfo("json");
				if (StringUtils.isNotBlank(jsonString)) {
					FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
					if (dialog.open() != null) {
						File file = new File(dialog.getFilterPath() + File.separator + dialog.getFileName());
						try {
							FileUtils.writeStringToFile(file, jsonString, "UTF-8");
						} catch (IOException e) {
							LoggerFactory.getLogger(getClass()).error("Error writing json file", e);
						}
					}
				} else {
					MessageDialog.openError(getShell(), "Fehler", "Die Rolle hat keine Berechtigungs Definition");
				}
			};

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_EXPORT.getImageDescriptor();
			};

			@Override
			public String getText() {
				return "Berechtigungen exportieren";
			};

			@Override
			public boolean isEnabled() {
				if (checkboxTableViewerRoles != null && checkboxTableViewerRoles.getStructuredSelection() != null
						&& !checkboxTableViewerRoles.getStructuredSelection().isEmpty()) {
					return !((IRole) checkboxTableViewerRoles.getStructuredSelection().getFirstElement())
							.isSystemRole();
				}
				return false;
			};
		});

		checkboxTableViewerRoles = CheckboxTableViewer.newCheckList(compositeRoles, SWT.BORDER | SWT.FULL_SELECTION);
		new Label(compositeEdit, SWT.NONE);
		new Label(compositeEdit, SWT.NONE);
		checkboxTableViewerRoles.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerRoles.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				StringBuilder sb = new StringBuilder();
				IRole r = (IRole) element;
				sb.append(r.getId());
				if (!r.isSystemRole()) {
					sb.append(" *");
					String jsonString = (String) r.getExtInfo("json");
					if (StringUtils.isEmpty(jsonString)) {
						sb.append(" (leer)");
					}
				}
				return sb.toString();
			}
		});
		checkboxTableViewerRoles.addCheckStateListener((e) -> {
			IRole r = (IRole) e.getElement();
			if (r == null)
				return;
			IUser user = wvUser.getValue();
			if (e.getChecked()) {
				user.addRole(r);
			} else {
				user.removeRole(r);
			}
			CoreModelServiceHolder.get().save(user);
		});
		Menu roleContextmenu = rolePopManager.createContextMenu(checkboxTableViewerRoles.getControl());
		checkboxTableViewerRoles.getControl().setMenu(roleContextmenu);
		rolePopManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				for (IContributionItem item : manager.getItems()) {
					item.update();
				}
			}
		});

		checkboxTableViewerAssociation.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerAssociation.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				IMandator stdWorkingFor = null;
				IUser user = wvUser.getValue();
				if (user != null) {
					if (user.getAssignedContact() != null) {
						stdWorkingFor = UserServiceHolder.get().getDefaultExecutiveDoctorWorkingFor(user).orElse(null);
					}
				}

				IMandator m = (IMandator) element;
				StringBuilder sb = new StringBuilder();
				if (m.equals(stdWorkingFor)) {
					sb.append("* "); //$NON-NLS-1$
				}
				sb.append(m.getDescription1() + StringUtils.SPACE + m.getDescription2());
				if (StringUtils.isNoneBlank(m.getDescription3())) {
					sb.append(" (").append(m.getDescription3()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return sb.toString();
			}
		});
		checkboxTableViewerAssociation.setComparator(new MandantViewerComparator(checkboxTableViewerAssociation));

		updateRoles();
		updateAssociations();

		initDataBindings();

		wvUser.addValueChangeListener(new ValueChangedAdapter());

		updateUserList();


		setUnlocked(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus());

		sash.setWeights(new int[] { 1, 5 });
		return container;
	}

	/**
	 * Initialize the preference page.
	 */
	@Override
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}

	private void updateUserList() {
		List<IUser> users = CoreModelServiceHolder.get().getQuery(IUser.class).execute();
		if (isShowOnlyActive) {
			users = users.stream().filter(IUser::isActive).collect(Collectors.toList());
		}
		users.sort(Comparator.comparing(IUser::getLabel));
		tableViewerUsers.setInput(users);
	}
	private class ValueChangedAdapter implements IValueChangeListener<IUser> {

		@Override
		public void handleValueChange(ValueChangeEvent event) {
			IUser user = wvUser.getValue();
			if (user == null) {
				wvUserContact.setValue(null);
				return;
			}

			setErrorMessage(null);

			IContact anw = user.getAssignedContact();
			wvUserContact.setValue(anw);
			String text = (anw != null) ? anw.getLabel() : "Nicht gesetzt";
			linkContact.setText(text + StringUtils.SPACE + CHANGE_LINK);

			userInfoLabel.setText(text + " [" + user.getId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

			updateRoles();

			Object[] assignedRoles = UserServiceHolder.get().getUserRoles(user).toArray();
			checkboxTableViewerRoles.setCheckedElements(assignedRoles);

			updateAssociations();

			userGroups = UserServiceHolder.get().getUserGroups(user);
			checkUserGroups();

			if (anw != null) {
				Optional<IMandator> mandator = CoreModelServiceHolder.get().load(anw.getId(), IMandator.class);
				if (mandator.isPresent()) {
					btnMandatorIsInactive.setSelection(!mandator.get().isActive());
				}
				btnIsExecutiveDoctor.setSelection(mandator.isPresent());
				btnMandatorIsInactive.setEnabled(mandator.isPresent());
			}

			linkRechnungssteller.setText("- " + CHANGE_LINK); //$NON-NLS-1$
			lblRespPhysColor.setBackground(lblRespPhysColorDefColor);

			if (anw != null) {
				checkboxTableViewerAssociation.setCheckedElements(
						UserServiceHolder.get().getExecutiveDoctorsWorkingFor(user, true).toArray());
				Optional<IMandator> mandator = CoreModelServiceHolder.get().load(anw.getId(), IMandator.class);
				if (mandator.isPresent()) {
					Color color = UiMandant.getColorForMandator(Mandant.load(mandator.get().getId()));
					lblRespPhysColor.setBackground(color);

					IContact rs = mandator.get().getBiller();
					String rst = (rs != null) ? rs.getLabel() : "Nicht gesetzt";
					linkRechnungssteller.setText(rst + StringUtils.SPACE + CHANGE_LINK);
				}
			}
		}

	}

	private void checkUserGroups() {
		List<CheckboxTableViewer> tableViewers = List.of(checkboxTableViewerAssociation, checkboxTableViewerRoles);
		boolean hasUserGroups = !userGroups.isEmpty();

		tableViewers.forEach(tableViewer -> {
			Table table = tableViewer.getTable();

			if (hasUserGroups) {
				setMessage("Der Benutzer ist in Gruppe(n) "
						+ userGroups.stream().map(ug -> ug.getGroupname()).collect(Collectors.joining(","))
						+ ". Es werden die Mandanten und Rollen der Gruppe verwendet.", WARNING);
				table.getParent().setEnabled(false);
				table.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_TEXT_DISABLED_BACKGROUND));
				table.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			} else {
				setMessage(null, WARNING);
				table.getParent().setEnabled(true);
				table.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				table.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			}
		});
	}

	private void updateRoles() {
		List<IRole> roles = CoreModelServiceHolder.get().getQuery(IRole.class).execute();
		checkboxTableViewerRoles.setInput(roles);
	}

	private void resetAll() {
		tableViewerUsers.setSelection(StructuredSelection.EMPTY);
		wvUser.setValue(null);
		wvUserContact.setValue(null);
		updateRoles();
		updateAssociations();
		checkboxTableViewerRoles.setCheckedElements(new IRole[0]);
		checkboxTableViewerAssociation.setCheckedElements(new IMandator[0]);
		btnIsExecutiveDoctor.setSelection(false);
		btnUserIsLocked.setSelection(false);
		for (Link link : new Link[] { linkContact, linkRechnungssteller }) {
			link.setText(StringUtils.EMPTY);
		}
		lblRespPhysColor.setBackground(lblRespPhysColorDefColor);
		userInfoLabel.setText(StringUtils.EMPTY);
	}

	private void updateAssociations() {
		checkboxTableViewerAssociation.setInput(CoreModelServiceHolder.get().getQuery(IMandator.class).execute());
		checkboxTableViewerAssociation.setCheckedElements(new IMandator[] {});
		List<IMandator> allMandators = CoreModelServiceHolder.get().getQuery(IMandator.class).execute();
		if (isShowOnlyActive) {
			final List<IUser> activeUsers = CoreModelServiceHolder.get().getQuery(IUser.class)
					.and(ModelPackage.Literals.IMANDATOR__ACTIVE, COMPARATOR.EQUALS, true).execute();
			allMandators = activeUsers.stream().map(IUser::getAssignedContact).filter(Objects::nonNull)
					.map(contact -> CoreModelServiceHolder.get().load(contact.getId(), IMandator.class))
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

			}
			checkboxTableViewerAssociation.setInput(null);
			checkboxTableViewerAssociation.setInput(allMandators);
			checkboxTableViewerAssociation.refresh();
			compositeAssociation.redraw();
	}

	private class AnwenderCellLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			IUser user = (IUser) cell.getElement();
			cell.setText(user.getLabel());
			IContact ac = user.getAssignedContact();
			if (ac != null) {
				Optional<IMandator> mandator = CoreModelServiceHolder.get().load(ac.getId(), IMandator.class);
				if (mandator.isPresent()) {
					Color mc = UiMandant.getColorForMandator(Mandant.load(mandator.get().getId()));
					cell.setImage(MandantSelectionContributionItem.getBoxSWTColorImage(mc));
				} else {
					cell.setImage(Images.IMG_EMPTY_TRANSPARENT.getImage());
				}
			}
			if (user.isAdministrator()) {
				cell.setForeground(UiDesk.getColor(UiDesk.COL_RED));
				cell.setImage(Images.IMG_AUSRUFEZ.getImage());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue<Boolean> observeSelectionBtnIsAdminObserveWidget = WidgetProperties.buttonSelection()
				.observe(btnUserIsAdmin);
		IObservableValue<Boolean> wvAdminObserveDetailValue = PojoProperties
				.value(IUser.class, "administrator", Boolean.class) //$NON-NLS-1$
				.observeDetail(wvUser);
		bindingContext.bindValue(observeSelectionBtnIsAdminObserveWidget, wvAdminObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), wvUser), null);

		IObservableValue<Boolean> observeSelectionBtnIsActiveObserveWidget = WidgetProperties.buttonSelection()
				.observe(btnUserIsLocked);
		IObservableValue<Boolean> wvActiveObserveDetailValue = PojoProperties
				.value(IUser.class, "active", Boolean.class) //$NON-NLS-1$
				.observeDetail(wvUser);
		bindingContext.bindValue(observeSelectionBtnIsActiveObserveWidget, wvActiveObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), wvUser).setConverter(new BooleanNotConverter()),
				new UpdateValueStrategy().setConverter(new BooleanNotConverter()));
		//
		return bindingContext;
	}

	@Override
	protected void performApply() {
		releaseLockIfRequired();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		releaseLockIfRequired();
		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		releaseLockIfRequired();
		return super.performCancel();
	}

	private void releaseLockIfRequired() {
		IUser user = wvUser.getValue();
		if (user != null && LocalLockServiceHolder.get().isLocked(user)) {
			LocalLockServiceHolder.get().releaseLock(user);
		}
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		btnIsExecutiveDoctor.setEnabled(unlocked);
		linkChangePassword.setEnabled(unlocked);
		linkContact.setEnabled(unlocked);
		linkRechnungssteller.setEnabled(unlocked);
		btnUserIsAdmin.setEnabled(unlocked);
		btnUserIsLocked.setEnabled(unlocked);
		checkboxTableViewerAssociation.getTable().setEnabled(unlocked);
		checkboxTableViewerRoles.getTable().setEnabled(unlocked);
		lblRespPhysColor.setEnabled(unlocked);
	}

	public class MandantViewerComparator extends ViewerComparator {

		public MandantViewerComparator(Viewer viewer) {
		}

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			IMandator m1 = (IMandator) o1;
			IMandator m2 = (IMandator) o2;
			String desc1 = m1.getDescription1();
			String desc2 = m2.getDescription1();
			
			// Handle null values - treat them as empty strings for comparison
			if (desc1 == null && desc2 == null) {
				return 0;
			}
			if (desc1 == null) {
				return 1; // null values go to the end
			}
			if (desc2 == null) {
				return -1; // null values go to the end
			}
			
			return desc1.compareToIgnoreCase(desc2);
		}
	}



}
