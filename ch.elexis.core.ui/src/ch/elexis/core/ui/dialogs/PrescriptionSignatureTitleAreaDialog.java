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

import ch.elexis.core.ui.views.controls.ArticleDefaultSignatureComposite;
import ch.elexis.data.ArticleDefaultSignature.ArticleSignature;
import ch.elexis.data.Artikel;

public class PrescriptionSignatureTitleAreaDialog extends TitleAreaDialog {
	
	private Artikel article;
	private ArticleDefaultSignatureComposite adsc;
	private Button btnAsDefault;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public PrescriptionSignatureTitleAreaDialog(Shell parentShell, Artikel article){
		super(parentShell);
		this.article = article;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle("Verordnungs Signatur");
		setMessage("Die Signatur der Verordnung von " + article.getLabel());
		Composite area = (Composite) super.createDialogArea(parent);
		
		btnAsDefault = new Button(area, SWT.CHECK);
		btnAsDefault.setText("Als standard Signatur hinterlegen.");
		btnAsDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
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
		adsc.setOnLocationEnabled(false);
		adsc.setArticleToBind(article, false);
		
		return area;
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed(){
		if (btnAsDefault.getSelection()) {
			adsc.createPersistent();
			adsc.updateModelNonDatabinding();
			adsc.safeToDefault();
		}
		
		super.okPressed();
	}
	
	public ArticleSignature getSignature(){
		return adsc.getSignature();
	}
}
