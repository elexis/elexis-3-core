package ch.elexis.core.ui.e4.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.icons.Images;
import ch.rgw.tools.TimeTool;

public class IContactSelectorDialog extends TitleAreaDialog {

	private final IModelService coreModelService;
	private final Class<? extends IContact> queryClass;

	private IContact selectedContact;

	private Text text;
	private TableViewer tableViewerContacts;

	private String initialSearchText;
	private List<? extends IContact> initialInput;
	private String _message;
	private String _title;
	private DialogTrayWithSelectionListener dialogTray;

	/**
	 * @wbp.parser.constructor
	 */
	public IContactSelectorDialog(Shell parentShell, IModelService coreModelService) {
		this(parentShell, coreModelService, IContact.class);
	}

	public IContactSelectorDialog(Shell parentShell, IModelService coreModelService,
			Class<? extends IContact> queryClass) {
		super(parentShell);
		this.coreModelService = coreModelService;
		this.queryClass = queryClass;
	}

	public void setDialogTray(DialogTrayWithSelectionListener dialogTray) {
		this.dialogTray = dialogTray;
	}

	@Override
	public void create() {
		super.create();
		if (_title != null) {
			super.setTitle(_title);
		} else {
			setTitle(Messages.Core_Please_Select_Contact);
		}
		super.setMessage(_message);
		super.setTitle(_title);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		if (dialogTray != null) {
			openTray(dialogTray);
		}
		return contents;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.addListener(SWT.CHANGED, (e) -> {
			refresh(); // delayed?
		});
		text.setMessage("Suche ... mindestens 3 Zeichen");

		Composite composite = new Composite(container, SWT.NONE);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		int h = Math.round(text.getLineHeight());
		gridData.minimumHeight = (12 + 1) * (h + 3);
		gridData.heightHint = gridData.minimumHeight;
		composite.setLayoutData(gridData);

		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		tableViewerContacts = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);

		tableViewerContacts.setContentProvider(ArrayContentProvider.getInstance());
		tableViewerContacts.addSelectionChangedListener((e) -> {
			selectedContact = (IContact) tableViewerContacts.getStructuredSelection().getFirstElement();
			if (dialogTray != null) {
				dialogTray.selectionChanged(selectedContact);
			}
		});

		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewerContacts, SWT.NONE);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IContact) element).getLabel();
			}

			@Override
			public Image getImage(Object element) {
				IContact contact = ((IContact) element);
				if (contact.isOrganization() || contact.isLaboratory()) {
					return Images.IMG_ORGANISATION.getImage();
				} else if (contact.isPatient()) {
					return (Gender.MALE == contact.asIPatient().getGender()) ? Images.IMG_MANN.getImage()
							: Images.IMG_FRAU.getImage();
				}
				return Images.IMG_PERSON.getImage();
			}
		});
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn, new ColumnWeightData(100));

		if (initialSearchText != null) {
			text.setText(initialSearchText);
			refresh();
		}

		if (initialInput != null) {
			tableViewerContacts.setInput(initialInput);
		}

		return area;
	}

	private void refresh() {
		String _text = text.getText();

		if (StringUtils.isNotBlank(_text) && _text.length() > 2) {

			String[] patterns = _text.split(" ");

			IQuery<? extends IContact> query = coreModelService.getQuery(queryClass);

			List<? extends IContact> result = new ArrayList<IContact>();

			if (patterns[0].matches("[a-zA-Z-]+")) {
				String value = "%" + patterns[0] + "%";
				query.startGroup();
				query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, value, true);
				query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, value, true);
				query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION3, COMPARATOR.LIKE, value, true);
				query.or(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, value, true);
				query.andJoinGroups();
				result = query.execute();
			} else if (IPerson.class.isAssignableFrom(queryClass) && possibleDate(patterns[0])) {
				query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
						new TimeTool(patterns[0]).toLocalDate(), true);
				result = query.execute();
			}

			for (int i = 1; i < patterns.length; ++i) {
				if (patterns[i].matches("[a-zA-Z-]+")) {
					String value = patterns[i];
					result.removeIf(c -> !(StringUtils.containsIgnoreCase(c.getDescription1(), value)
							|| StringUtils.containsIgnoreCase(c.getDescription2(), value)
							|| StringUtils.containsIgnoreCase(c.getDescription3(), value)));
				} else if (IPerson.class.isAssignableFrom(queryClass) && possibleDate(patterns[i])) {
					TimeTool value = new TimeTool(patterns[i]);
					@SuppressWarnings("unchecked")
					List<IPerson> matches = (List<IPerson>) result;
					matches.removeIf(p -> !(p.getDateOfBirth().toLocalDate().equals(value.toLocalDate())));
					result = matches;
				}
			}

			if (IPerson.class.isAssignableFrom(queryClass)) {
				query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION1, ORDER.ASC);
			}
			tableViewerContacts.setInput(result);
		} else {
			tableViewerContacts.setInput(Collections.emptyList());
		}

	}

	private boolean possibleDate(String pattern) {
		return pattern.matches("[0-9./-]{5,}");
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public IContact getSelectedContact() {
		return selectedContact;
	}

	@Override
	public void setMessage(String message) {
		this._message = message;
	}

	@Override
	public void setTitle(String title) {
		this._title = title;
	}

	public void setSearchText(String searchText) {
		if (searchText != null) {
			if (text != null) {
				text.setText(searchText);
			} else {
				initialSearchText = searchText;
			}
		}
	}

	public void setInput(List<? extends IContact> initialInput) {
		this.initialInput = initialInput;
	}

}
