package ch.elexis.core.ui.preferences;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wb.swt.SWTResourceManager;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.services.holder.UserServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;

public class UserGroupsManagementPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IUnlockable {
	private TableViewer tableViewerUserGroups;

	private WritableValue<IUserGroup> wvUserGroup = new WritableValue<>(null, IUserGroup.class);

	private CheckboxTableViewer checkboxTableViewerUsers;
	private CheckboxTableViewer checkboxTableViewerAssociation;
	private CheckboxTableViewer checkboxTableViewerRoles;

	private RestrictedAction addUserGroupAction, deleteUserGroupAction, lockUserGroupAction;
	private Label userInfoLabel;

	/**
	 * Create the preference page.
	 */
	public UserGroupsManagementPreferencePage() {
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
		if (!AccessControlServiceHolder.get()
				.evaluate(EvACE.of(IUserGroup.class, Right.CREATE).and(Right.UPDATE).and(Right.DELETE))) {
			return new PrefAccessDenied(parent);
		}

		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(2, false));

		MenuManager popManager = new MenuManager();

		addUserGroupAction = new RestrictedAction(EvACE.of(IUserGroup.class, Right.CREATE),
				Messages.Core_Add_ellipsis) {
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
						boolean isFree = UserServiceHolder.get().verifyGroupnameNotTaken(newText);
						if (!isFree) {
							return "Gruppennamen vergeben";
						}

						return null;
					}
				};
				InputDialog id = new InputDialog(Hub.getActiveShell(), "Gruppennamen festlegen",
						"Gruppennamen festlegen - dieser kann nicht mehr geändert, sowie zukünftig anderweitig verwendet werden.",
						null, iiv);
				int retVal = id.open();
				if (retVal == Dialog.OK) {
					IUserGroup newUser = new IUserGroupBuilder(CoreModelServiceHolder.get(), id.getValue())
							.buildAndSave();
					updateUserGroupList();
					tableViewerUserGroups.setSelection(new StructuredSelection(newUser));
				}
			}
		};
		popManager.add(addUserGroupAction);

		deleteUserGroupAction = new LockedRestrictedAction<IUserGroup>(EvACE.of(IUserGroup.class, Right.REMOVE),
				Messages.Core_Delete) {

			@Override
			public IUserGroup getTargetedObject() {
				if (tableViewerUserGroups == null) {
					return null;
				}
				StructuredSelection ss = (StructuredSelection) tableViewerUserGroups.getSelection();
				return (ss != null) ? (IUserGroup) ss.getFirstElement() : null;
			}

			@Override
			public void doRun(IUserGroup userGroup) {
				CoreModelServiceHolder.get().remove(userGroup);
				updateUserGroupList();
				wvUserGroup.setValue(null);
			}
		};
		popManager.add(deleteUserGroupAction);

		if (!(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus())) {
			lockUserGroupAction = new RestrictedAction(EvACE.of(IUserGroup.class, Right.UPDATE),
					Messages.Leistungscodes_editItem) {

				@Override
				public void doRun() {
					StructuredSelection ss = (StructuredSelection) tableViewerUserGroups.getSelection();
					IUserGroup u = (IUserGroup) ss.getFirstElement();
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
			popManager.add(lockUserGroupAction);
		}

		popManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				addUserGroupAction.reflectRight();
				deleteUserGroupAction.reflectRight();
				if (lockUserGroupAction != null) {
					lockUserGroupAction.reflectRight();
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

		Composite compositeButtons = new Composite(compositeLeft, SWT.None);
		compositeButtons.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnAdd = new Button(compositeButtons, SWT.FLAT);
		btnAdd.setImage(Images.IMG_NEW.getImage());
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addUserGroupAction.doRun();
			}
		});

		if (!(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus())) {
			Button btnLock = new Button(compositeButtons, SWT.FLAT | SWT.TOGGLE);
			btnLock.setSelection(LocalLockServiceHolder.get().isLocked(wvUserGroup.getValue()));
			btnLock.setImage(Images.IMG_LOCK_OPEN.getImage());
			btnLock.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IUserGroup userGroup = wvUserGroup.getValue();
					if (LocalLockServiceHolder.get().isLocked(userGroup)) {
						LocalLockServiceHolder.get().releaseLock(userGroup);
					} else {
						lockUserGroupAction.doRun();
					}
					boolean locked = LocalLockServiceHolder.get().isLocked(wvUserGroup.getValue());
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

		tableViewerUserGroups = new TableViewer(compositeSelectorTable, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewerUserGroups.setContentProvider(ArrayContentProvider.getInstance());
		Table tableUserGroups = tableViewerUserGroups.getTable();
		tableUserGroups.setLinesVisible(true);
		tableViewerUserGroups.addSelectionChangedListener(e -> {
			releaseLockIfRequired();

			StructuredSelection ss = (StructuredSelection) e.getSelection();
			wvUserGroup.setValue(ss == null ? null : (IUserGroup) ss.getFirstElement());
			setUnlocked(ConnectionStatus.STANDALONE == ElexisServerServiceHolder.get().getConnectionStatus());

			compositeEdit.layout(true, true);
		});

		TableViewerColumn tableViewerColumnName = new TableViewerColumn(tableViewerUserGroups, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tcl_compositeSelectorTable.setColumnData(tblclmnName, new ColumnWeightData(100));
		tableViewerColumnName.setLabelProvider(new UserGroupCellLabelProvider());

		Menu menu = popManager.createContextMenu(tableUserGroups);
		tableUserGroups.setMenu(menu);
		new Label(compositeEdit, SWT.NONE);
		new Label(compositeEdit, SWT.NONE);

		Composite compositeAccounting = new Composite(compositeEdit, SWT.NONE);
		compositeAccounting.setLayout(new GridLayout(3, true));
		compositeAccounting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Group grpUsers = new Group(compositeAccounting, SWT.NONE);
		grpUsers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpUsers.setText("Benutzer");
		GridLayout gl_grp = new GridLayout(2, false);
		gl_grp.marginHeight = 0;
		grpUsers.setLayout(gl_grp);

		Composite compositeUsers = new Composite(grpUsers, SWT.NONE);
		compositeUsers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		TableColumnLayout tcl_compositeUsers = new TableColumnLayout();
		compositeUsers.setLayout(tcl_compositeUsers);

		checkboxTableViewerUsers = CheckboxTableViewer.newCheckList(compositeUsers,
				SWT.BORDER | SWT.FULL_SELECTION);
		checkboxTableViewerUsers.addCheckStateListener((e) -> {
			IUser u = (IUser) e.getElement();
			if (u == null)
				return;
			IUserGroup userGroup = wvUserGroup.getValue();
			if (e.getChecked()) {
				userGroup.addUser(u);
			} else {
				userGroup.removeUser(u);
			}
			CoreModelServiceHolder.get().save(userGroup);
		});
		checkboxTableViewerUsers.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerUsers.setLabelProvider(new DefaultLabelProvider());
		checkboxTableViewerUsers.setComparator(new UserViewerComparator());

		Group grpAssociation = new Group(compositeAccounting, SWT.NONE);
		grpAssociation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpAssociation.setText("tätig für");
		gl_grp = new GridLayout(2, false);
		gl_grp.marginHeight = 0;
		grpAssociation.setLayout(gl_grp);

		Composite compositeAssociation = new Composite(grpAssociation, SWT.NONE);
		compositeAssociation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		TableColumnLayout tcl_compositeAssociation = new TableColumnLayout();
		compositeAssociation.setLayout(tcl_compositeAssociation);

		checkboxTableViewerAssociation = CheckboxTableViewer.newCheckList(compositeAssociation,
				SWT.BORDER | SWT.FULL_SELECTION);
		checkboxTableViewerAssociation.addCheckStateListener((e) -> {
			IMandator m = (IMandator) e.getElement();
			if (m == null)
				return;
			IUserGroup userGroup = wvUserGroup.getValue();
			UserServiceHolder.get().addOrRemoveExecutiveDoctorWorkingFor(userGroup, m, e.getChecked());
		});

		// TODO std. Mandant für die Gruppe macht keinen Sinn?

		Group grpRoles = new Group(compositeAccounting, SWT.NONE);
		grpRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpRoles.setText("Rollenzuordnung");
		GridLayout gl_grpRoles = new GridLayout(2, false);
		gl_grpRoles.marginHeight = 0;
		grpRoles.setLayout(gl_grpRoles);

		Composite compositeRoles = new Composite(grpRoles, SWT.NONE);
		compositeRoles.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeRoles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		checkboxTableViewerRoles = CheckboxTableViewer.newCheckList(compositeRoles, SWT.BORDER | SWT.FULL_SELECTION);
		new Label(compositeEdit, SWT.NONE);
		new Label(compositeEdit, SWT.NONE);
		checkboxTableViewerRoles.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerRoles.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				IRole r = (IRole) element;
				return r.getId();
			}
		});
		checkboxTableViewerRoles.addCheckStateListener((e) -> {
			IRole r = (IRole) e.getElement();
			if (r == null)
				return;
			IUserGroup userGroup = wvUserGroup.getValue();
			if (e.getChecked()) {
				userGroup.addRole(r);
			} else {
				userGroup.removeRole(r);
			}
			CoreModelServiceHolder.get().save(userGroup);
		});
		checkboxTableViewerAssociation.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewerAssociation.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				IUserGroup userGroup = wvUserGroup.getValue();

				IMandator m = (IMandator) element;
				StringBuilder sb = new StringBuilder();
				sb.append(m.getDescription1() + StringUtils.SPACE + m.getDescription2());
				if (StringUtils.isNoneBlank(m.getDescription3())) {
					sb.append(" (").append(m.getDescription3()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return sb.toString();
			}
		});
		checkboxTableViewerAssociation.setComparator(new MandantViewerComparator());

		updateUsers();
		updateRoles();
		updateAssociations();

		wvUserGroup.addValueChangeListener(new ValueChangedAdapter());

		updateUserGroupList();

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

	private void updateUserGroupList() {
		List<IUserGroup> users = CoreModelServiceHolder.get().getQuery(IUserGroup.class).execute();
		users.sort((u1, u2) -> u1.getLabel().compareTo(u2.getLabel()));
		tableViewerUserGroups.setInput(users);
	}

	private class ValueChangedAdapter implements IValueChangeListener<IUserGroup> {

		@Override
		public void handleValueChange(ValueChangeEvent event) {
			IUserGroup userGroup = wvUserGroup.getValue();

			setErrorMessage(null);

			updateUsers();

			Object[] assignedUsers = userGroup != null ? userGroup.getUsers().toArray() : new Object[0];
			checkboxTableViewerUsers.setCheckedElements(assignedUsers);

			updateRoles();

			Object[] assignedRoles = userGroup != null ? userGroup.getRoles().toArray() : new Object[0];
			checkboxTableViewerRoles.setCheckedElements(assignedRoles);

			updateAssociations();

			checkboxTableViewerAssociation
					.setCheckedElements(
							UserServiceHolder.get().getExecutiveDoctorsWorkingFor(userGroup, true).toArray());
		}
	}

	private void updateUsers() {
		List<IUser> users = CoreModelServiceHolder.get().getQuery(IUser.class).execute();
		checkboxTableViewerUsers.setInput(users);
	}

	private void updateRoles() {
		List<IRole> roles = CoreModelServiceHolder.get().getQuery(IRole.class).execute();
		checkboxTableViewerRoles.setInput(roles);
	}

	private void updateAssociations() {
		checkboxTableViewerAssociation.setInput(CoreModelServiceHolder.get().getQuery(IMandator.class).execute());
		checkboxTableViewerAssociation.setCheckedElements(new IMandator[] {});
	}

	private class UserGroupCellLabelProvider extends CellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			IUserGroup userGroup = (IUserGroup) cell.getElement();
			cell.setText(userGroup.getLabel());
		}
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
		IUserGroup userGroup = wvUserGroup.getValue();
		if (userGroup != null && LocalLockServiceHolder.get().isLocked(userGroup)) {
			LocalLockServiceHolder.get().releaseLock(userGroup);
		}
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		checkboxTableViewerUsers.getTable().setEnabled(unlocked);
		checkboxTableViewerAssociation.getTable().setEnabled(unlocked);
		checkboxTableViewerRoles.getTable().setEnabled(unlocked);
	}

	public class MandantViewerComparator extends ViewerComparator {

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			IMandator m1 = (IMandator) o1;
			IMandator m2 = (IMandator) o2;
			return m1.getDescription1().compareToIgnoreCase(m2.getDescription1());
		}
	}

	public class UserViewerComparator extends ViewerComparator {

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			IUser u1 = (IUser) o1;
			IUser u2 = (IUser) o2;
			return u1.getId().compareToIgnoreCase(u2.getId());
		}
	}
}
