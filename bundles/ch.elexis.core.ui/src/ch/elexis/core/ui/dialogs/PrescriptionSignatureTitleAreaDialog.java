package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.views.controls.ArticleDefaultSignatureComposite;

public class PrescriptionSignatureTitleAreaDialog extends TitleAreaDialog {

	private IArticle article;
	private ArticleDefaultSignatureComposite adsc;
	private Button btnAsDefault;
	private IArticleDefaultSignature signature;
	private boolean medicationTypeFix;
	private boolean performLookup;
	private boolean isFromBillingDialog;
	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public PrescriptionSignatureTitleAreaDialog(Shell parentShell, IArticle article) {
		super(parentShell);
		this.article = article;
	}

	public PrescriptionSignatureTitleAreaDialog(Shell parentShell, IArticle article, boolean isFromBillingDialog) {
		super(parentShell);
		this.article = article;
		this.isFromBillingDialog = isFromBillingDialog;
	}
	
	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Verordnungssignatur");
		setMessage("Die Signatur der Verordnung von " + article.getLabel());
		Composite area = (Composite) super.createDialogArea(parent);

		btnAsDefault = new Button(area, SWT.CHECK);
		btnAsDefault.setText("Als Standardsignatur hinterlegen.");
		btnAsDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				adsc.setOnLocationEnabled(btnAsDefault.getSelection());
			}
		});
		GridData gd = new GridData();
		gd.horizontalIndent = 5;
		gd.verticalIndent = 5;
		btnAsDefault.setLayoutData(gd);

		adsc = new ArticleDefaultSignatureComposite(area, SWT.None);
		adsc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		adsc.initDataBindings(null);
		adsc.setToolbarVisible(false);
		adsc.setStartVisible(true);
		adsc.setOnLocationEnabled(false);
		adsc.setArticleToBind(article, false);
		if (performLookup) {
			adsc.setArticleToBind(article, true);
		}
		if (signature != null) {
			adsc.setSignature(signature);
		}
		if (medicationTypeFix) {
			adsc.setMedicationTypeFix();
		}
		if (isFromBillingDialog) {
			adsc.setMedicationTypeDischarge();
			boolean defaultSymptomsSetting = ConfigServiceHolder
					.getUser(Preferences.MEDICATION_SETTINGS_DEFAULT_SYMPTOMS, false);
			if (!defaultSymptomsSetting) {
				setDefaultValues();
			}
		}

		return area;
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

	@Override
	protected void okPressed() {
		adsc.updateModelNonDatabinding();
		if (btnAsDefault.getSelection()) {
			adsc.save();
		}

		super.okPressed();
	}

	public void setMedicationTypeFix(boolean value) {
		this.medicationTypeFix = value;
	}

	/**
	 * Perform lookup for existing signature, and set its content if found.
	 */
	public void lookup() {
		if (adsc != null && !adsc.isDisposed()) {
			adsc.setArticleToBind(article, true);
		} else {
			performLookup = true;
		}
	}

	/**
	 * Set a signature that will be displayed and edited on the dialog.
	 *
	 * @param s
	 */
	public void setSignature(IArticleDefaultSignature s) {
		this.signature = s;
	}

	public IArticleDefaultSignature getSignature() {
		return adsc.getSignature();
	}

	public void setDefaultValues() {
		adsc.setEndDateDays(0);
		adsc.setFocusOnMorning();
	}

}
