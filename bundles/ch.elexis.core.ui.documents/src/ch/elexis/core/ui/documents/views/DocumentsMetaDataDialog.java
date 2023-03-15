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

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.util.FindingsServiceHolder;
import ch.elexis.core.findings.util.ValueSetServiceHolder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.IDocumentStore.Capability;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktErfassenDialog;
import ch.elexis.core.ui.documents.composites.CategorySelectionEditComposite;
import ch.elexis.core.ui.documents.provider.AuthorContentProposalProvider;
import ch.elexis.core.ui.documents.provider.ValueSetProposalProvider;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.documents.util.AutoCompleteTextUtil;
import ch.elexis.core.ui.documents.util.ContactLabelUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class DocumentsMetaDataDialog extends TitleAreaDialog {

	private static Logger logger = LoggerFactory.getLogger(DocumentsMetaDataDialog.class);

	String file;
	final IDocument document;
	final IDocumentReference documentReference;
	Text tTitle;
	Text tKeywords;
	Text tAuthor;
	Text tPracticeSetting;
	Text tDocumentClass;
	private CategorySelectionEditComposite csec;

	public String title;
	public String keywords;
	public String category;

	private final boolean categoryCrudAllowed;

	private CDateTime creationDate;
	private CDateTime lastchangedDate;

	public DocumentsMetaDataDialog(IDocument document, Shell parent) {
		super(parent);

		Objects.requireNonNull(FindingsServiceHolder.getiFindingsService(), "Findings-Service not installed."); //$NON-NLS-1$
		Objects.requireNonNull(ValueSetServiceHolder.getIValueSetService(), "ValueSet-Service not installed."); //$NON-NLS-1$

		this.document = document;
		this.documentReference = findDocumentReference();

		categoryCrudAllowed = DocumentStoreServiceHolder.getService().isAllowed(document, Capability.CATEGORY);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(4, false));
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

		new Label(ret, SWT.None).setText(Messages.Core_Category);
		csec = new CategorySelectionEditComposite(ret, SWT.NONE, document, categoryCrudAllowed);
		csec.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.Core_Title);
		tTitle = SWTHelper.createText(ret, 1, SWT.NONE);
		tTitle.setText(document.getTitle());
		tTitle.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		new Label(ret, SWT.NONE).setText("Datum");
		lastchangedDate = new CDateTime(ret, CDT.DATE_SHORT | CDT.DROP_DOWN | SWT.BORDER | CDT.TAB_FIELDS);
		GridData gd_lastchangedDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lastchangedDate.widthHint = 150;
		lastchangedDate.setLayoutData(gd_lastchangedDate);
		if(document.getLastchanged() != null) {
			lastchangedDate.setSelection(document.getLastchanged());
		} else {
			lastchangedDate.setSelection(new Date());
		}
		
		new Label(ret, SWT.NONE).setText("Erstelldatum");
		creationDate = new CDateTime(ret, CDT.DATE_SHORT | CDT.DROP_DOWN | SWT.BORDER | CDT.TAB_FIELDS);
		GridData gd_archivingDate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_archivingDate.widthHint = 150;
		creationDate.setLayoutData(gd_archivingDate);
		if(document.getCreated() != null) {
			creationDate.setSelection(document.getCreated());
		} else {
			creationDate.setSelection(new Date());
		}

		createUIDocumentReferences(ret);
		return ret;
	}

	private void createUIDocumentReferences(Composite ret) {
		new Label(ret, SWT.NONE).setText(Messages.Core_Keywords);
		tKeywords = SWTHelper.createText(ret, 4, SWT.NONE);
		tKeywords.setText(Optional.ofNullable(Objects.toString(documentReference.getKeywords(), document.getKeywords()))
				.orElse(StringUtils.EMPTY));
		tKeywords.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.DocumentsView_Author);
		tAuthor = SWTHelper.createText(ret, 1, SWT.NONE);
		tAuthor.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		AutoCompleteTextUtil.addAutoCompleteSupport(tAuthor, new AuthorContentProposalProvider(),
				Optional.ofNullable(documentReference.getAuthorId())
						.map(o -> CoreModelServiceHolder.get().load(o, IContact.class).orElse(null))
						.orElse(document.getAuthor()));

		MenuManager menuManager = new MenuManager();

		menuManager.add(new Action(ch.elexis.core.l10n.Messages.KontaktDetailDialog_newContact) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			}

			@Override
			public void run() {
				KontaktErfassenDialog dlg = new KontaktErfassenDialog(UiDesk.getTopShell(),
						ContactLabelUtil.getContactHints(tAuthor.getText()));
				if (dlg.open() == Window.OK) {
					if (dlg.getContact().isPresent()) {
						AutoCompleteTextUtil.setValue(tAuthor, dlg.getContact().get());
					} else {
						MessageDialog.openWarning(getParentShell(), StringUtils.EMPTY,
								"Der Kontakt konnte nicht angelegt werden.");
					}
				}
			}

			@Override
			public boolean isEnabled() {
				return !(AutoCompleteTextUtil.getData(tAuthor) instanceof IContact);
			}
		});
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IContributionItem[] items = manager.getItems();
				for (IContributionItem iContributionItem : items) {
					iContributionItem.update();
				}
			}
		});
		tAuthor.setMenu(menuManager.createContextMenu(tAuthor));

		new Label(ret, SWT.NONE).setText(Messages.DocumentsView_DocumentClass);
		tDocumentClass = SWTHelper.createText(ret, 1, SWT.NONE);
		tDocumentClass.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		AutoCompleteTextUtil.addAutoCompleteSupport(tDocumentClass,
				new ValueSetProposalProvider(ValueSetProposalProvider.EPRDOCUMENT_CLASSCODE),
				documentReference.getDocumentClass());

		new Label(ret, SWT.NONE).setText(Messages.DocumentsView_PracticeSetting);
		tPracticeSetting = SWTHelper.createText(ret, 1, SWT.NONE);
		tPracticeSetting.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));

		AutoCompleteTextUtil.addAutoCompleteSupport(tPracticeSetting,
				new ValueSetProposalProvider(ValueSetProposalProvider.EPRDOCUMENT_PRACTICESETTINGCODE),
				documentReference.getPracticeSetting());
	}

	private IDocumentReference findDocumentReference() {
		List<IDocumentReference> documentReferences = FindingsServiceHolder.getiFindingsService()
				.getDocumentFindings(document.getId(), IDocumentReference.class);
		if (documentReferences.size() > 1) {
			LoggerFactory.getLogger(getClass()).warn(
					"Got more than one DocumentReferences for document id [" + document.getId() + "] using first"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (documentReferences.isEmpty()) {
			// no document reference found - create new entry
			return FindingsServiceHolder.getiFindingsService().create(IDocumentReference.class);
		} else {
			return documentReferences.get(0);
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle(document.getTitle());
		getShell().setText(Messages.DocumentMetaDataDialog_title);
		setMessage(Messages.Core_Enter_Title_and_tags_for_document);
	}

	@Override
	protected void okPressed() {
		title = tTitle.getText();
		ICategory category = csec.getSelection();
		if (category != null) {
			document.setCategory(category);
		}
		document.setTitle(title);
		keywords = tKeywords.getText();
		document.setKeywords(keywords);
		document.setAuthor((IContact) AutoCompleteTextUtil.getData(tAuthor));
		document.setCreated(creationDate.getSelection());
		document.setLastchanged(lastchangedDate.getSelection());

		saveDocumentReference();
		super.okPressed();
	}

	private void saveDocumentReference() {
		documentReference.setPatientId(document.getPatient().getId());
		documentReference.setDocument(document);
		documentReference.setKeywords(tKeywords.getText());
		documentReference.setAuthorId(Optional.ofNullable((IContact) AutoCompleteTextUtil.getData(tAuthor))
				.map(IContact::getId).orElse(null));

		Optional.ofNullable(AutoCompleteTextUtil.getData(tPracticeSetting))
				.ifPresent(o -> documentReference.setPracticeSetting((ICoding) o));

		Optional.ofNullable(AutoCompleteTextUtil.getData(tDocumentClass))
				.ifPresent(o -> documentReference.setDocumentClass((ICoding) o));
		FindingsServiceHolder.getiFindingsService().saveFinding(documentReference);
	}

}
