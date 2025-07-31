package ch.elexis.core.ui.documents.composites;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IXid;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class CategorySelectionEditComposite extends Composite {

	private ComboViewer cbCategories;

	private IDocument document;

	private boolean storeAvailable;

	/**
	 * @param parent
	 * @param style
	 * @param storeId
	 * @param categoryCrudAllowed
	 * @since 3.8
	 * @wbp.parser.constructor
	 */
	public CategorySelectionEditComposite(Composite parent, int style, String storeId, boolean categoryCrudAllowed) {
		this(parent, style, new TransientOmnivoreDocument(storeId), categoryCrudAllowed);
	}

	public CategorySelectionEditComposite(Composite parent, int style, IDocument document,
			boolean categoryCrudAllowed) {
		super(parent, style);

		this.document = document;

		storeAvailable = DocumentStoreServiceHolder.getService().getServiceById(document.getStoreId()) != null;
		setFocus();
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		cbCategories = new ComboViewer(this, SWT.SINGLE | SWT.READ_ONLY);
		cbCategories.setContentProvider(ArrayContentProvider.getInstance());
		cbCategories.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ICategory) {
					ICategory iCategory = (ICategory) element;
					return iCategory.getName();
				}
				return super.getText(element);
			}
		});
		List<ICategory> categories = Collections.emptyList();
		if (storeAvailable) {
			categories = DocumentStoreServiceHolder.getService().getCategories(document);
			cbCategories.setInput(categories);
		} else {
			cbCategories.setInput(categories);
			cbCategories.getControl().setToolTipText(document.getStoreId() + " nicht installiert");
			cbCategories.getControl().setEnabled(false);
		}

		Button bNewCat = new Button(this, SWT.PUSH);
		bNewCat.setVisible(categoryCrudAllowed);
		bNewCat.setImage(Images.IMG_NEW.getImage());
		bNewCat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog id = new InputDialog(getShell(), Messages.Core_New_Category,
						Messages.Core_New_Category, null, null);
				if (id.open() == Dialog.OK) {
					addAndSelectCategory(id.getValue());
				}
			}
		});
		Button bEditCat = new Button(this, SWT.PUSH);
		bEditCat.setImage(Images.IMG_EDIT.getImage());
		bEditCat.setVisible(categoryCrudAllowed);
		bEditCat.setToolTipText(Messages.DocumentMetaDataDialog_renameCategory);
		bEditCat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection old = cbCategories.getSelection();
				Object selection = ((StructuredSelection) old).getFirstElement();
				if (selection instanceof ICategory) {
					InputDialog id = new InputDialog(getShell(),
							MessageFormat.format(Messages.DocumentMetaDataDialog_renameCategoryConfirm,
									((ICategory) selection).getName()),
							Messages.DocumentMetaDataDialog_renameCategoryText, ((ICategory) selection).getName(),
							null);
					if (id.open() == Dialog.OK) {
						DocumentStoreServiceHolder.getService().renameCategory(document, id.getValue());
						((ICategory) selection).setName(id.getValue());
						cbCategories.refresh();
					}
				}

			}
		});

		Button bDeleteCat = new Button(this, SWT.PUSH);
		bDeleteCat.setVisible(categoryCrudAllowed);
		bDeleteCat.setImage(Images.IMG_DELETE.getImage());
		bDeleteCat.setToolTipText(Messages.Core_Delete_Document_Category);
		bDeleteCat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				ISelection old = cbCategories.getSelection();
				Object selection = ((StructuredSelection) old).getFirstElement();
				InputDialog id = new InputDialog(getShell(),
						MessageFormat.format(Messages.DocumentMetaDataDialog_deleteCategoryConfirm,
								((ICategory) selection).getName()),
						Messages.DocumentMetaDataDialog_deleteCategoryText, StringUtils.EMPTY, null);
				if (id.open() == Dialog.OK) {
					try {
						document.setCategory((ICategory) selection);
						document.setCategory(
								DocumentStoreServiceHolder.getService().removeCategory(document, id.getValue()));
						if (findComboElementByName(document.getCategory().getName()) == null) {
							cbCategories.add(document.getCategory());
						}
						cbCategories.remove(selection);
						cbCategories.setSelection(new StructuredSelection(document.getCategory()), true);
					} catch (ElexisException e) {
						LoggerFactory.getLogger(getClass()).warn("existing document references", e); //$NON-NLS-1$
						SWTHelper.showError(Messages.DocumentMetaDataDialog_deleteCategoryError,
								Messages.DocumentMetaDataDialog_deleteCategoryErrorText);
					}
				}
			}
		});

		Object cbSelection = document.getCategory() != null ? document.getCategory() : cbCategories.getElementAt(0);
		if (cbSelection != null) {
			if (storeAvailable) {
				if (!categories.contains(cbSelection)) {
					String categoryName = ((ICategory) cbSelection).getName();
					Optional<ICategory> matchingName = categories.stream()
							.filter(cat -> cat.getName() != null && cat.getName().equals(categoryName)).findFirst();
					if (matchingName.isPresent()) {
						cbSelection = matchingName.get();
					} else {
						cbSelection = DocumentStoreServiceHolder.getService().getDefaultCategory(document);
					}
				}
				cbCategories.setSelection(new StructuredSelection(cbSelection), true);				
			}
		}
	}

	public void setCategoryByName(String category) {
		ICategory _category = findComboElementByName(category);
		if (_category == null) {
			addAndSelectCategory(category);
		} else {
			cbCategories.setSelection(new StructuredSelection(_category), true);
		}
	}

	private ICategory findComboElementByName(String name) {
		List<?> items = (List<?>) cbCategories.getInput();
		if (items != null) {
			for (Object o : items) {
				if (o instanceof ICategory) {
					if (((ICategory) o).getName().equals(name)) {
						return (ICategory) o;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void addAndSelectCategory(String categoryName) {
		if (storeAvailable) {
			if (categoryName == null) {
				cbCategories.setSelection(new StructuredSelection());
			} else {
				document.setCategory(DocumentStoreServiceHolder.getService().createCategory(document, categoryName));
				if (!((List<?>) cbCategories.getInput()).contains(document.getCategory())) {
					List<ICategory> input = ((List<ICategory>) (List<?>) cbCategories.getInput());
					if (input != null) {
						input.add(document.getCategory());
						input.sort((l, r) -> {
							return l.getName().compareTo(r.getName());
						});
					} else {
						cbCategories.setInput(DocumentStoreServiceHolder.getService().getCategories(document));
					}
					cbCategories.refresh();
				}
				cbCategories.setSelection(new StructuredSelection(document.getCategory()), true);
			}
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void addSelectionChangeListener(ISelectionChangedListener listener) {
		cbCategories.addSelectionChangedListener(listener);
	}

	public @Nullable ICategory getSelection() {
		StructuredSelection comboSelection = (StructuredSelection) cbCategories.getSelection();
		if (comboSelection != null) {
			return (ICategory) comboSelection.getFirstElement();
		}
		return null;
	}

	/**
	 * This allows to manage categories in a store without effectively providing a
	 * document
	 */
	private static class TransientOmnivoreDocument implements IDocument {

		private ICategory category;
		private String storeId;

		public TransientOmnivoreDocument(String storeId) {
			this.storeId = storeId;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getLabel() {
			return null;
		}

		@Override
		public boolean addXid(String domain, String id, boolean updateIfExists) {
			return false;
		}

		@Override
		public IXid getXid(String domain) {
			return null;
		}

		@Override
		public Long getLastupdate() {
			return null;
		}

		@Override
		public boolean isDeleted() {
			return false;
		}

		@Override
		public void setDeleted(boolean value) {
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public void setTitle(String value) {
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public void setDescription(String value) {
		}

		@Override
		public List<DocumentStatus> getStatus() {
			return null;
		}

		@Override
		public void setStatus(DocumentStatus status, boolean active) {
		}

		@Override
		public Date getCreated() {
			return null;
		}

		@Override
		public void setCreated(Date value) {
		}

		@Override
		public Date getLastchanged() {
			return null;
		}

		@Override
		public void setLastchanged(Date value) {
		}

		@Override
		public String getMimeType() {
			return null;
		}

		@Override
		public void setMimeType(String value) {
		}

		@Override
		public ICategory getCategory() {
			return category;
		}

		@Override
		public void setCategory(ICategory value) {
			this.category = value;
		}

		@Override
		public List<IHistory> getHistory() {
			return null;
		}

		@Override
		public String getStoreId() {
			return storeId;
		}

		@Override
		public void setStoreId(String value) {
		}

		@Override
		public String getExtension() {
			return null;
		}

		@Override
		public void setExtension(String value) {
		}

		@Override
		public String getKeywords() {
			return null;
		}

		@Override
		public void setKeywords(String value) {
		}

		@Override
		public IPatient getPatient() {
			return null;
		}

		@Override
		public void setPatient(IPatient value) {
		}

		@Override
		public IContact getAuthor() {
			return null;
		}

		@Override
		public void setAuthor(IContact value) {
		}

		@Override
		public InputStream getContent() {
			return null;
		}

		@Override
		public void setContent(InputStream content) {
		}

		@Override
		public long getContentLength() {
			return 0;
		}

	}

}
