package ch.elexis.core.ui.e4.dialog;

import java.util.Collections;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.icons.Images;

public class IContactSelectorDialog extends TitleAreaDialog {

	private IModelService coreModelService;

	private IContact selectedContact;

	private Text text;
	private TableViewer tableViewerContacts;

	private String initialSearchText;

	public IContactSelectorDialog(Shell parentShell, IModelService coreModelService) {
		super(parentShell);
		this.coreModelService = coreModelService;
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

		setTitle(Messages.Core_Please_Select_Contact);

		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.addListener(SWT.CHANGED, (e) -> {
			refresh(); // delayed?
		});
		text.setMessage("Suche ... mindestens 3 Zeichen");

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		tableViewerContacts = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);

		tableViewerContacts.setContentProvider(ArrayContentProvider.getInstance());
		tableViewerContacts.addSelectionChangedListener((e) -> {
			selectedContact = (IContact) tableViewerContacts.getStructuredSelection().getFirstElement();
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
				}
				return Images.IMG_PERSON.getImage();
			}
		});
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn, new ColumnWeightData(100));
		tblclmnNewColumn.setText("New Column");

		if (initialSearchText != null) {
			text.setText(initialSearchText);
			refresh();
		}

		return area;
	}

	private void refresh() {
		String _text = text.getText();
		if (StringUtils.isNotBlank(_text) && _text.length() > 2) {
			IQuery<IContact> query = coreModelService.getQuery(IContact.class);
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + _text + "%");
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, "%" + _text + "%");
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION3, COMPARATOR.LIKE, "%" + _text + "%");
			query.limit(50);
			tableViewerContacts.setInput(query.execute());
		} else {
			tableViewerContacts.setInput(Collections.emptyList());
		}

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

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
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

}
