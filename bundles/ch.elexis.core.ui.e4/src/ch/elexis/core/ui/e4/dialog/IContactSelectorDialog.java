package ch.elexis.core.ui.e4.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
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
	private String _assertOkMessageTemplate;
	private DialogTrayWithSelectionListener dialogTray;
	public static final int CREATE_NEW_ID = IDialogConstants.CLIENT_ID + 1;
	private boolean showCreateNewButton = false;
	private String finalSearchText;

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

	/**
	 * If set, a selection made by the user will have to be re-asserted by the user
	 * before the Dialog returns with Dialog.OK status.
	 * 
	 * @param messageTemplate
	 */
	public void setAssertOkTemplateMessage(String messageTemplate) {
		_assertOkMessageTemplate = messageTemplate;
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

	@Override
	protected void okPressed() {
		if (selectedContact == null) {
			boolean isPatientSearch = IPatient.class.isAssignableFrom(queryClass);
			if (!isPatientSearch) {
				MessageDialog.openWarning(getShell(), Messages.McProviderSelectionDialog_KontaktSelektorTitel,
						Messages.KontaktFieldEditor_PleaseSelectContact);
				return;
			}
		}
		if (_assertOkMessageTemplate != null && selectedContact != null) {
			boolean isAssertOk = MessageDialog.openQuestion(getShell(), null,
					String.format(_assertOkMessageTemplate, selectedContact.getLabel()));
			if (!isAssertOk) {
				return;
			}
		}
		super.okPressed();
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

			Button okButton = getButton(IDialogConstants.OK_ID);
			if (okButton != null) {
				okButton.setEnabled(selectedContact != null);
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

	private ResultsSupplier currentSupplier;

	private void refresh() {
		String _text = text.getText().trim();
		if (StringUtils.isNotBlank(_text) && _text.length() > 2) {
			if (currentSupplier != null) {
				currentSupplier.cancel();
			}
			currentSupplier = new ResultsSupplier(_text);
			CompletableFuture.supplyAsync(currentSupplier).thenAccept((list) -> {
				if (list != null) {
					Display.getDefault().asyncExec(() -> {
						tableViewerContacts.setInput(list);
					});
				}
			});
		} else {
			tableViewerContacts.setInput(Collections.emptyList());
		}
	}

	private class ResultsSupplier implements Supplier<List<? extends IContact>> {

		private boolean cancel;
		private String text;

		public ResultsSupplier(String text) {
			this.text = text;
		}

		@Override
		public List<? extends IContact> get() {
			String[] originalPatterns = text.split("\\s+");
			if (cancel) {
				return null;
			}
			List<? extends IContact> allMatches = executeDatabaseSearch(originalPatterns[0], text);
			if (cancel) {
				return null;
			}
			List<? extends IContact> bestMatches = filterResults(new ArrayList<>(allMatches), originalPatterns);
			if (cancel) {
				return null;
			}
			List<IContact> finalDisplayList = new ArrayList<>();
			finalDisplayList.addAll(bestMatches);
			for (IContact contact : allMatches) {
				if (!bestMatches.contains(contact)) {
					finalDisplayList.add(contact);
				}
			}
			return cancel ? null : finalDisplayList;
		}

		public void cancel() {
			cancel = true;
		}
	}

	private List<? extends IContact> executeDatabaseSearch(String firstWord, String fullSearchText) {
		IQuery<? extends IContact> query = coreModelService.getQuery(queryClass);

		String sqlSearchTerm = fullSearchText.toLowerCase().replace("ae", "%").replace("oe", "%").replace("ue", "%");
		String sqlFirstWord = sqlSearchTerm.split("\\s+")[0];
		boolean possibleDate = possibleDate(firstWord);
		if (!possibleDate) {
			String value = "%" + sqlFirstWord + "%";
			query.startGroup();
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, value, true);
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, value, true);
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION3, COMPARATOR.LIKE, value, true);
			query.or(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, value, true);
			query.andJoinGroups();
			return query.execute();
		} else if (IPerson.class.isAssignableFrom(queryClass) && possibleDate) {
			query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
					new TimeTool(firstWord).toLocalDate(), true);
			return query.execute();
		}

		return new ArrayList<IContact>();
	}

	private List<? extends IContact> filterResults(List<? extends IContact> inputList, String[] patterns) {
		List<? extends IContact> result = inputList;

		for (int i = 1; i < patterns.length; ++i) {
			final String word = patterns[i].toLowerCase();
			final String altWord = word.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ae", "ä")
					.replace("oe", "ö").replace("ue", "ü");

			if (word.length() > 0) {
				List<IContact> filtered = new ArrayList<>(result);
				filtered.removeIf(c -> {
					String label = (StringUtils.defaultString(c.getDescription1()) + StringUtils.SPACE
							+ StringUtils.defaultString(c.getDescription2()) + StringUtils.SPACE
							+ StringUtils.defaultString(c.getDescription3())).toLowerCase();

					return !label.contains(word) && !label.contains(altWord);
				});

				if (!filtered.isEmpty()) {
					result = filtered;
				}
			}
		}
		return result;
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
		if (showCreateNewButton) {
			createButton(parent, CREATE_NEW_ID, "Neu erstellen", false);
		}
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(selectedContact != null);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (text != null && !text.isDisposed()) {
			finalSearchText = text.getText();
		}

		if (buttonId == CREATE_NEW_ID) {
			setReturnCode(CREATE_NEW_ID);
			close();
		} else {
			super.buttonPressed(buttonId);
		}
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

	public void setShowCreateNewButton(boolean show) {
		this.showCreateNewButton = show;
	}

	public String getSearchText() {
		if (finalSearchText != null) {
			return finalSearchText;
		}
		if (text != null && !text.isDisposed()) {
			return text.getText();
		}
		return initialSearchText;
	}
}
