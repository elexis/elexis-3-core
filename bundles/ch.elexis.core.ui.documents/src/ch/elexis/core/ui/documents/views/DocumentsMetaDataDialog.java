/*******************************************************************************
 * Copyright (c) 2006-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.documents.views;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.util.FindingsServiceHolder;
import ch.elexis.core.findings.util.ValueSetServiceHolder;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.IDocumentStore.Capability;
import ch.elexis.core.ui.documents.Messages;
import ch.elexis.core.ui.documents.provider.CodingElementComparer;
import ch.elexis.core.ui.documents.provider.CodingLabelProvider;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class DocumentsMetaDataDialog extends TitleAreaDialog {
	
	private static Logger logger = LoggerFactory.getLogger(DocumentsMetaDataDialog.class);
	
	String file;
	IDocument document;
	IDocumentReference documentReference;
	Text tTitle;
	Text tKeywords;
	Text tAuthor;
	ComboViewer cbPracticeSetting;
	ComboViewer cbDocumentClass;
	
	ComboViewer cbCategories;
	public String title;
	public String keywords;
	public String category;
	
	private final boolean categoryCrudAllowed;
	private final boolean keywordsCrudAllowed;
	
	public DocumentsMetaDataDialog(IDocument document, Shell parent){
		super(parent);
		this.document = document;
		this.documentReference = getDocumentReference(document).orElse(null);
		
		categoryCrudAllowed =
			DocumentStoreServiceHolder.getService().isAllowed(document, Capability.CATEGORY);
		keywordsCrudAllowed =
			DocumentStoreServiceHolder.getService().isAllowed(document, Capability.KEYWORDS);
	}
	
	@Override
	protected boolean isResizable(){
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		new Label(ret, SWT.None).setText(Messages.DocumentView_categoryColumn);
		Composite cCats = new Composite(ret, SWT.NONE);
		cCats.setFocus();
		cCats.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cCats.setLayout(new RowLayout(SWT.HORIZONTAL));
		cbCategories = new ComboViewer(cCats, SWT.SINGLE | SWT.READ_ONLY);
		cbCategories.setContentProvider(ArrayContentProvider.getInstance());
		cbCategories.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof ICategory) {
					ICategory iCategory = (ICategory) element;
					return iCategory.getName();
				}
				return super.getText(element);
			}
		});
		List<ICategory> categories =
			DocumentStoreServiceHolder.getService().getCategories(document);
		cbCategories.setInput(categories);
		
		Button bNewCat = new Button(cCats, SWT.PUSH);
		bNewCat.setVisible(categoryCrudAllowed);
		bNewCat.setImage(Images.IMG_NEW.getImage());
		bNewCat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				InputDialog id =
					new InputDialog(getShell(), Messages.DocumentMetaDataDialog_newCategory,
						Messages.DocumentMetaDataDialog_newCategory, null, null);
				if (id.open() == Dialog.OK) {
					document.setCategory(DocumentStoreServiceHolder.getService()
						.createCategory(document, id.getValue()));
					if (!((List<?>) cbCategories.getInput()).contains(document.getCategory()))
					{
						cbCategories.add(document.getCategory());
					}
					cbCategories.setSelection(new StructuredSelection(document.getCategory()),
						true);
				}
			}
		});
		Button bEditCat = new Button(cCats, SWT.PUSH);
		bEditCat.setImage(Images.IMG_EDIT.getImage());
		bEditCat.setVisible(categoryCrudAllowed);
		bEditCat.setToolTipText(Messages.DocumentMetaDataDialog_renameCategory);
		bEditCat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ISelection old = cbCategories.getSelection();
				Object selection = ((StructuredSelection) old).getFirstElement();
				if (selection instanceof ICategory) {
					InputDialog id = new InputDialog(getShell(),
						MessageFormat.format(Messages.DocumentMetaDataDialog_renameCategoryConfirm,
							((ICategory) selection).getName()),
						Messages.DocumentMetaDataDialog_renameCategoryText,
						((ICategory) selection).getName(), null);
					if (id.open() == Dialog.OK) {
						DocumentStoreServiceHolder.getService()
							.renameCategory(document, id.getValue());
						((ICategory) selection).setName(id.getValue());
						cbCategories.refresh();
					}
				}
				
			}
		});
		
		Button bDeleteCat = new Button(cCats, SWT.PUSH);
		bDeleteCat.setVisible(categoryCrudAllowed);
		bDeleteCat.setImage(Images.IMG_DELETE.getImage());
		bDeleteCat.setToolTipText(Messages.DocumentMetaDataDialog_deleteCategory);
		bDeleteCat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev){
				ISelection old = cbCategories.getSelection();
				Object selection = ((StructuredSelection) old).getFirstElement();
				InputDialog id =
					new InputDialog(getShell(),
						MessageFormat.format(Messages.DocumentMetaDataDialog_deleteCategoryConfirm,
						((ICategory) selection).getName()),
						Messages.DocumentMetaDataDialog_deleteCategoryText,
						"", null);
				if (id.open() == Dialog.OK) {
					try {
						document.setCategory((ICategory) selection);
						document.setCategory(DocumentStoreServiceHolder.getService()
							.removeCategory(document, id.getValue()));
						if (findComboElementByName(document.getCategory().getName()) == null) {
							cbCategories.add(document.getCategory());
						}
						cbCategories.remove(selection);
						cbCategories.setSelection(new StructuredSelection(document.getCategory()),
							true);
					} catch (ElexisException e) {
						logger.warn("existing document references", e);
						SWTHelper.showError(Messages.DocumentMetaDataDialog_deleteCategoryError,
							Messages.DocumentMetaDataDialog_deleteCategoryErrorText);
					}
				}
			}
		});
		new Label(ret, SWT.NONE).setText(Messages.DocumentsView_Title);
		tTitle = SWTHelper.createText(ret, 1, SWT.NONE);
		new Label(ret, SWT.NONE).setText(Messages.DocumentView_keywordsColumn);
		tKeywords = SWTHelper.createText(ret, 4, SWT.NONE);
		tKeywords.setEnabled(keywordsCrudAllowed);
		tTitle.setText(document.getTitle());
		
		tKeywords.setText(Objects.toString(document.getKeywords(), ""));
		Object cbSelection =
			document.getCategory() != null ? document.getCategory() : cbCategories.getElementAt(0);
		if (cbSelection != null) {
			if (!categories.contains(cbSelection)) {
				cbSelection = DocumentStoreServiceHolder.getService().getDefaultCategory(document);
			}
			cbCategories.setSelection(new StructuredSelection(cbSelection), true);
			
		}
		
		new Label(ret, SWT.NONE).setText(Messages.DocumentsView_Author);
		tAuthor = SWTHelper.createText(ret, 1, SWT.NONE);
		tAuthor.setText(Optional.ofNullable(document.getAuthor()).map(IContact::getLabel).orElse(""));
		
		createUIDocumentReferences(ret);
		return ret;
	}

	private void createUIDocumentReferences(Composite ret){
		if (ValueSetServiceHolder.getIValueSetService() != null) {
			new Label(ret, SWT.NONE).setText(Messages.DocumentsView_DocumentClass);
			cbDocumentClass = new ComboViewer(ret, SWT.SINGLE | SWT.READ_ONLY);
			cbDocumentClass.setContentProvider(ArrayContentProvider.getInstance());
			cbDocumentClass.setComparer(new CodingElementComparer());
			cbDocumentClass.setLabelProvider(new CodingLabelProvider());
			cbDocumentClass.setInput(ValueSetServiceHolder.getIValueSetService()
				.getValueSetByName("EprDocumentClassCode"));
			cbDocumentClass.getControl()
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			new Label(ret, SWT.NONE).setText(Messages.DocumentsView_PracticeSetting);
			cbPracticeSetting = new ComboViewer(ret, SWT.SINGLE | SWT.READ_ONLY);
			cbPracticeSetting.setContentProvider(ArrayContentProvider.getInstance());
			cbPracticeSetting.setLabelProvider(new CodingLabelProvider());
			cbPracticeSetting.setComparer(new CodingElementComparer());
			cbPracticeSetting.setInput(ValueSetServiceHolder.getIValueSetService()
				.getValueSetByName("EprDocumentPracticeSettingCode"));
			cbPracticeSetting.getControl()
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			// load selections
			if (documentReference != null) {
				Optional.ofNullable(documentReference.getDocumentClass())
					.ifPresent(o -> cbDocumentClass.setSelection(new StructuredSelection(o), true));
				Optional.ofNullable(documentReference.getPracticeSetting()).ifPresent(
					o -> cbPracticeSetting.setSelection(new StructuredSelection(o), true));
			}
		}
	}
	
	public Optional<IDocumentReference> getDocumentReference(IDocument document){
		if (FindingsServiceHolder.getiFindingsService() != null && document != null
			&& document.getId() != null) {
			List<IDocumentReference> documentReferences =
				FindingsServiceHolder.getiFindingsService().getDocumentFindings(document.getId(),
					IDocumentReference.class);
			if (documentReferences.size() > 1) {
				LoggerFactory.getLogger(getClass())
					.warn("Got more than one DocumentReferences for document id ["
						+ document.getId() + "] using first");
			}
			return documentReferences.stream().findFirst();
		}
		return Optional.empty();
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(document.getTitle());
		getShell().setText(Messages.DocumentMetaDataDialog_title);
		setMessage(
			Messages.DocumentMetaDataDialog_titleMessage);
	}
	
	@Override
	protected void okPressed(){
		title = tTitle.getText();
		StructuredSelection comboSelection = (StructuredSelection) cbCategories.getSelection();
		if (document != null) {
			if (comboSelection != null)
			{
				document.setCategory((ICategory) comboSelection.getFirstElement());
			}
			document.setTitle(title);
			
			if (keywordsCrudAllowed) {
				keywords = tKeywords.getText();
				document.setKeywords(keywords);
			}
			updateDocumentReferences();
		}
		super.okPressed();
	}

	private void updateDocumentReferences(){
		if (ValueSetServiceHolder.getIValueSetService() != null
			&& FindingsServiceHolder.getiFindingsService() != null) {
			if (documentReference == null) {
				documentReference =
					FindingsServiceHolder.getiFindingsService().create(IDocumentReference.class);
			}
			ICoding codingDocClass =
				(ICoding) ((StructuredSelection) cbDocumentClass.getSelection()).getFirstElement();
			ICoding codingPracSetting =
				(ICoding) ((StructuredSelection) cbPracticeSetting.getSelection())
					.getFirstElement();
			if (codingDocClass != null) {
				documentReference.setDocumentClass(codingDocClass);
			}
			if (codingPracSetting != null) {
				documentReference.setPracticeSetting(codingPracSetting);
			}
			documentReference.setPatientId(document.getPatient().getId());
			documentReference.setDocument(document);
			FindingsServiceHolder.getiFindingsService().saveFinding(documentReference);
		}
	}
	
	private ICategory findComboElementByName(String name){
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
	
}
