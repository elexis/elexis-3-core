package ch.elexis.core.ui.views.controls;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.dialogs.ArticleDefaultSignatureTitleAreaDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.ArticleDefaultSignature.ArticleSignature;
import ch.elexis.data.Artikel;

public class ArticleDefaultSignatureComposite extends Composite {
	
	private WritableValue signatureItem = new WritableValue(null, ArticleSignature.class);
	private DataBindingContext databindingContext;
	
	private ToolBarManager toolbarManager;
	
	private Text txtSignatureMorning;
	private Text txtSignatureNoon;
	private Text txtSignatureEvening;
	private Text txtSignatureNight;
	private Text txtSignatureComment;
	
	private Composite medicationType;
	private Button btnSymtomatic;
	private Button btnReserve;
	private Button btnFix;
	
	private Composite disposalType;
	private Button btnNoDisposal;
	private Button btnDispensation;
	
	private Composite signatureType;
	private Button btnRadioOnAtcCode;
	private Button btnRadioOnArticle;
	
	private Artikel article;
	
	/**
	 * Create the composite.
	 * 
	 * @param this
	 * @param style
	 */
	public ArticleDefaultSignatureComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(7, false));
		
		signatureType = new Composite(this, SWT.NONE);
		signatureType.setLayout(new RowLayout());
		signatureType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		
		btnRadioOnAtcCode = new Button(signatureType, SWT.RADIO);
		btnRadioOnAtcCode.setText("auf ATC Code hinterlegen");
		btnRadioOnAtcCode.addSelectionListener(new SavingSelectionAdapter());
		
		btnRadioOnArticle = new Button(signatureType, SWT.RADIO);
		btnRadioOnArticle.setText("auf Artikel hinterlegen");
		btnRadioOnArticle.addSelectionListener(new SavingSelectionAdapter());
		
		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddDefaultSignatureAction());
		toolbarManager.add(new RemoveDefaultSignatureAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		txtSignatureMorning = new Text(this, SWT.BORDER);
		txtSignatureMorning.setMessage("morgens");
		txtSignatureMorning.setToolTipText("");
		txtSignatureMorning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label = new Label(this, SWT.None);
		label.setText("-");
		
		txtSignatureNoon = new Text(this, SWT.BORDER);
		txtSignatureNoon.setMessage("mittags");
		txtSignatureNoon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(this, SWT.None);
		label.setText("-");
		
		txtSignatureEvening = new Text(this, SWT.BORDER);
		txtSignatureEvening.setMessage("abends");
		txtSignatureEvening.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(this, SWT.None);
		label.setText("-");
		
		txtSignatureNight = new Text(this, SWT.BORDER);
		txtSignatureNight.setMessage("nachts");
		txtSignatureNight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtSignatureComment = new Text(this, SWT.BORDER);
		txtSignatureComment.setMessage("Kommentar");
		txtSignatureComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		medicationType = new Composite(this, SWT.NONE);
		medicationType.setLayout(new RowLayout());
		medicationType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		btnSymtomatic = new Button(medicationType, SWT.RADIO);
		btnSymtomatic.setText("Symptom");
		btnSymtomatic.addSelectionListener(new SavingSelectionAdapter());
		
		btnReserve = new Button(medicationType, SWT.RADIO);
		btnReserve.setText("Reserve");
		btnReserve.addSelectionListener(new SavingSelectionAdapter());
		
		btnFix = new Button(medicationType, SWT.RADIO);
		btnFix.setText("Fix");
		btnFix.addSelectionListener(new SavingSelectionAdapter());
		
		disposalType = new Composite(this, SWT.NONE);
		disposalType.setLayout(new RowLayout());
		disposalType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		btnNoDisposal = new Button(disposalType, SWT.RADIO);
		btnNoDisposal.setText("Keine Abgabe (Rezept)");
		btnNoDisposal.addSelectionListener(new SavingSelectionAdapter());
		
		btnDispensation = new Button(disposalType, SWT.RADIO);
		btnDispensation.setText("Abgabe");
		btnDispensation.addSelectionListener(new SavingSelectionAdapter());
	}
	
	public void setToolbarVisible(boolean value){
		ToolBar toolbar = toolbarManager.getControl();
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.setVisible(value);
		}
	}
	
	public void setOnLocationEnabled(boolean value){
		btnRadioOnArticle.setEnabled(value);
		btnRadioOnAtcCode.setEnabled(value);
	}
	
	public DataBindingContext initDataBindings(DataBindingContext dbc){
		if (dbc == null) {
			databindingContext = new DataBindingContext();
		} else {
			databindingContext = dbc;
		}
		
		IObservableValue observeTextTextSignatureMorningObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureMorning);
		IObservableValue itemSignatureMorningObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "morning", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureMorningObserveWidget,
			itemSignatureMorningObserveDetailValue);
		
		IObservableValue observeTextTextSignatureNoonObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureNoon);
		IObservableValue itemSignatureNoonObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "noon", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureNoonObserveWidget,
			itemSignatureNoonObserveDetailValue);
		
		IObservableValue observeTextTextSignatureEveningObserveWidget =
				WidgetProperties.text(new int[] {
					SWT.Modify, SWT.FocusOut
				}).observeDelayed(100, txtSignatureEvening);
			IObservableValue itemSignatureEveningObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "evening", String.class)
					.observeDetail(signatureItem);
			databindingContext.bindValue(observeTextTextSignatureEveningObserveWidget,
				itemSignatureEveningObserveDetailValue);
		
		IObservableValue observeTextTextSignatureNightObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureNight);
		IObservableValue itemSignatureNightObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "night", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureNightObserveWidget,
			itemSignatureNightObserveDetailValue);
		
		IObservableValue observeTextTextSignatureCommentObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureComment);
		IObservableValue itemSignatureCommentObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "comment", String.class)
				.observeDetail(signatureItem);
		databindingContext.bindValue(observeTextTextSignatureCommentObserveWidget,
			itemSignatureCommentObserveDetailValue);
		
		return databindingContext;
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void setArticleToBind(Artikel article, boolean lookup){
		if (!isDisposed()) {
			ArticleSignature signature = getSignature();
			if (signature != null && signature.isPersistent()) {
				signature.toDefault();
			}
			// update with new article signature
			this.article = article;
			if (lookup) {
				ArticleDefaultSignature defSignature =
					ArticleDefaultSignature.getDefaultsignatureForArticle(article);
				if (defSignature != null) {
					signatureItem.setValue(ArticleSignature.fromDefault(defSignature));
				} else {
					signatureItem.setValue(new ArticleSignature(article, null));
				}
			} else {
				signatureItem.setValue(new ArticleSignature(article, null));
			}
			updateTargetNonDatabinding();
			// update the toolbar
			for (IContributionItem item : toolbarManager.getItems()) {
				item.update();
			}
		}
	}
	
	public void setArticleToBind(Artikel article){
		setArticleToBind(article, true);
	}
	
	public ArticleSignature getSignature(){
		Object value = signatureItem.getValue();
		if (value instanceof ArticleSignature) {
			return (ArticleSignature) value;
		}
		return null;
	}
	
	public void updateModelNonDatabinding(){
		ArticleSignature signature = getSignature();
		if (signature != null) {
			if (btnRadioOnAtcCode.getSelection()) {
				signature.setAtcCode(article.getATC_code());
			} else if (btnRadioOnArticle.getSelection()) {
				signature.setAtcCode(null);
				signature.setArticle(article);
			}
			if (btnSymtomatic.getSelection()) {
				signature.setMedicationType(EntryType.SYMPTOMATIC_MEDICATION);
			} else if (btnReserve.getSelection()) {
				signature.setMedicationType(EntryType.RESERVE_MEDICATION);
			} else if (btnFix.getSelection()) {
				signature.setMedicationType(EntryType.FIXED_MEDICATION);
			}
			if (btnNoDisposal.getSelection()) {
				signature.setDisposalType(EntryType.RECIPE);
			} else if (btnDispensation.getSelection()) {
				signature.setDisposalType(EntryType.SELF_DISPENSED);
			}
		}
	}
	
	public void updateTargetNonDatabinding(){
		ArticleSignature signature = getSignature();
		
		btnFix.setSelection(false);
		btnReserve.setSelection(false);
		btnSymtomatic.setSelection(false);
		
		btnNoDisposal.setSelection(false);
		btnDispensation.setSelection(false);
		
		btnRadioOnArticle.setSelection(false);
		btnRadioOnAtcCode.setSelection(false);
		
		if (signature != null) {
			EntryType modelMedicationType = signature.getMedicationType();
			if (modelMedicationType == EntryType.FIXED_MEDICATION) {
				btnFix.setSelection(true);
			} else if (modelMedicationType == EntryType.RESERVE_MEDICATION) {
				btnReserve.setSelection(true);
			} else if (modelMedicationType == EntryType.SYMPTOMATIC_MEDICATION) {
				btnSymtomatic.setSelection(true);
			} else {
				// default
				btnSymtomatic.setSelection(true);
			}
			EntryType modelDisposalType = signature.getDisposalType();
			if (modelDisposalType == EntryType.RECIPE) {
				btnNoDisposal.setSelection(true);
			} else if (modelDisposalType == EntryType.SELF_DISPENSED) {
				btnDispensation.setSelection(true);
			} else {
				// default
				btnNoDisposal.setSelection(true);
			}
			if (signature.isAtc()) {
				btnRadioOnAtcCode.setSelection(true);
			} else {
				btnRadioOnArticle.setSelection(true);
			}
		}
	}
	
	public void safeToDefault(){
		ArticleSignature signature = getSignature();
		
		// dont save if no signature set
		String morningTxt = txtSignatureMorning.getText();
		String noonTxt = txtSignatureNoon.getText();
		String eveningTxt = txtSignatureEvening.getText();
		String nightTxt = txtSignatureNight.getText();
		if ((morningTxt == null || morningTxt.isEmpty()) && (noonTxt == null || noonTxt.isEmpty())
			&& (eveningTxt == null || eveningTxt.isEmpty())
			&& (nightTxt == null || nightTxt.isEmpty())) {
			return;
		}

		// dont save if no medication type is selected
		if (!btnFix.getSelection() && !btnReserve.getSelection() && !btnSymtomatic.getSelection()) {
			return;
		}
		
		if (signature != null) {
			signature.setMorning(morningTxt);
			signature.setNoon(noonTxt);
			signature.setEvening(eveningTxt);
			signature.setNight(nightTxt);
			signature.setComment(txtSignatureComment.getText());
			signature.toDefault();
		}
	}
	
	public void createPersistent(){
		ArticleSignature signature = getSignature();
		if (!signature.isPersistent()) {
			signature.createPersistent();
		}
	}
	
	public String getSignatureMorning(){
		return txtSignatureMorning.getText();
	}
	
	public void setSignatureMorning(String signatureMorning){
		txtSignatureMorning.setText(signatureMorning);
	}
	
	public String getSignatureNoon(){
		return txtSignatureNoon.getText();
	}
	
	public void setSignatureNoon(String signatureNoon){
		txtSignatureNoon.setText(signatureNoon);
	}
	
	public String getSignatureEvening(){
		return txtSignatureEvening.getText();
	}
	
	public void setSignatureEvening(String signatureEvening){
		txtSignatureEvening.setText(signatureEvening);
	}
	
	public String getSignatureNight(){
		return txtSignatureNight.getText();
	}
	
	public void setSignatureNight(String signatureNight){
		txtSignatureNight.setText(signatureNight);
	}
	
	public String getSignatureComment(){
		return txtSignatureComment.getText();
	}
	
	public void setSignatureComment(String signatureComment){
		txtSignatureComment.setText(signatureComment);
	}
	
	private class SavingSelectionAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e){
			updateModelNonDatabinding();
			safeToDefault();
		}
	}
	
	private class AddDefaultSignatureAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
		
		@Override
		public void run(){
			ArticleDefaultSignatureTitleAreaDialog dialog =
				new ArticleDefaultSignatureTitleAreaDialog(getShell(), article);
			dialog.open();
			// update the content
			setArticleToBind(article);
		}
		
		@Override
		public boolean isEnabled(){
			ArticleSignature signature = getSignature();
			// not enabled if already signature on article
			if (signature != null && signature.isPersistent() && !signature.isAtc()) {
				return false;
			}
			return true;
		}
	}
	
	private class RemoveDefaultSignatureAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DELETE.getImageDescriptor();
		}
		
		@Override
		public void run(){
			ArticleSignature signature = getSignature();
			if (signature != null && signature.isPersistent()) {
				signature.delete();
			}
			// update the content
			setArticleToBind(article);
		}
		
		@Override
		public boolean isEnabled(){
			ArticleSignature signature = getSignature();
			if (signature == null || !signature.isPersistent()) {
				return false;
			}
			return true;
		}
	}
}
