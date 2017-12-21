package ch.elexis.core.ui.views.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.dialogs.ArticleDefaultSignatureTitleAreaDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;
import ch.elexis.data.ArticleDefaultSignature;
import ch.elexis.data.ArticleDefaultSignature.ArticleSignature;
import ch.elexis.data.Artikel;
import ch.rgw.tools.TimeTool;

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
	private StackLayout stackLayoutDosage;
	private Composite compositeDayTimeDosage;
	private Text txtFreeTextDosage;
	private Composite compositeFreeTextDosage;
	private Composite stackCompositeDosage;
	private Composite compositeMedicationTypeDetail;
	
	private Label lblCalcEndDate;
	
	private List<SavingTargetToModelStrategy> targetToModelStrategies;
	
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
		btnRadioOnAtcCode.setText(Messages.ArticleDefaultSignatureComposite_onAtc);
		btnRadioOnAtcCode.addSelectionListener(new SavingSelectionAdapter());
		
		btnRadioOnArticle = new Button(signatureType, SWT.RADIO);
		btnRadioOnArticle.setText(Messages.ArticleDefaultSignatureComposite_onArticle);
		btnRadioOnArticle.addSelectionListener(new SavingSelectionAdapter());
		
		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddDefaultSignatureAction());
		toolbarManager.add(new RemoveDefaultSignatureAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		stackCompositeDosage = new Composite(this, SWT.NONE);
		stackCompositeDosage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));
		stackLayoutDosage = new StackLayout();
		stackCompositeDosage.setLayout(stackLayoutDosage);
		
		compositeDayTimeDosage = new Composite(stackCompositeDosage, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		compositeDayTimeDosage.setLayout(layout);
		txtSignatureMorning = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtSignatureMorning.setMessage(Messages.ArticleDefaultSignatureComposite_morning);
		txtSignatureMorning.setToolTipText(""); //$NON-NLS-1$
		txtSignatureMorning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label = new Label(compositeDayTimeDosage, SWT.None);
		label.setText("-"); //$NON-NLS-1$
		
		txtSignatureNoon = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtSignatureNoon.setMessage(Messages.ArticleDefaultSignatureComposite_noon);
		txtSignatureNoon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(compositeDayTimeDosage, SWT.None);
		label.setText("-"); //$NON-NLS-1$
		
		txtSignatureEvening = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtSignatureEvening.setMessage(Messages.ArticleDefaultSignatureComposite_evening);
		txtSignatureEvening.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(compositeDayTimeDosage, SWT.None);
		label.setText("-"); //$NON-NLS-1$
		
		txtSignatureNight = new Text(compositeDayTimeDosage, SWT.BORDER);
		txtSignatureNight.setMessage(Messages.ArticleDefaultSignatureComposite_night);
		txtSignatureNight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		compositeFreeTextDosage = new Composite(stackCompositeDosage, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		compositeFreeTextDosage.setLayout(layout);
		txtFreeTextDosage = new Text(compositeFreeTextDosage, SWT.BORDER);
		txtFreeTextDosage.setMessage(Messages.ArticleDefaultSignatureComposite_dosage);
		txtFreeTextDosage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// set initial control to day time dosage
		stackLayoutDosage.topControl = compositeDayTimeDosage;
		
		Button btnDoseSwitch = new Button(this, SWT.NONE);
		btnDoseSwitch.setImage(Images.IMG_SYNC.getImage());
		btnDoseSwitch.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e){
				if (stackLayoutDosage.topControl == compositeDayTimeDosage) {
					stackLayoutDosage.topControl = compositeFreeTextDosage;
				} else {
					stackLayoutDosage.topControl = compositeDayTimeDosage;
					txtFreeTextDosage.setText(""); //$NON-NLS-1$
				}
				stackCompositeDosage.layout();
			};
		});
		
		txtSignatureComment = new Text(this, SWT.BORDER);
		txtSignatureComment.setMessage(Messages.ArticleDefaultSignatureComposite_applicationInstruction);
		txtSignatureComment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		medicationType = new Composite(this, SWT.NONE);
		medicationType.setLayout(new RowLayout());
		medicationType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		btnSymtomatic = new Button(medicationType, SWT.RADIO);
		btnSymtomatic.setText(Messages.ArticleDefaultSignatureComposite_sympomatic);
		btnSymtomatic.addSelectionListener(new SavingSelectionAdapter());
		
		btnReserve = new Button(medicationType, SWT.RADIO);
		btnReserve.setText(Messages.ArticleDefaultSignatureComposite_reserve);
		btnReserve.addSelectionListener(new SavingSelectionAdapter());
		
		btnFix = new Button(medicationType, SWT.RADIO);
		btnFix.setText(Messages.ArticleDefaultSignatureComposite_fix);
		btnFix.addSelectionListener(new SavingSelectionAdapter());
		
		createMedicationTypeDetails(this);

		disposalType = new Composite(this, SWT.NONE);
		disposalType.setLayout(new RowLayout());
		disposalType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		btnNoDisposal = new Button(disposalType, SWT.RADIO);
		btnNoDisposal.setText(Messages.ArticleDefaultSignatureComposite_recipe);
		btnNoDisposal.addSelectionListener(new SavingSelectionAdapter());
		
		btnDispensation = new Button(disposalType, SWT.RADIO);
		btnDispensation.setText(Messages.ArticleDefaultSignatureComposite_dispensation);
		btnDispensation.addSelectionListener(new SavingSelectionAdapter());
	}

	private void createMedicationTypeDetails(Composite parent){
		compositeMedicationTypeDetail = new Composite(parent, SWT.NONE);
		compositeMedicationTypeDetail.setLayout(new GridLayout(4, false));
		compositeMedicationTypeDetail
			.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Label lblEnddate = new Label(compositeMedicationTypeDetail, SWT.NONE);
		lblEnddate.setText("Stoppdatum:");
		
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		lblCalcEndDate = new Label(compositeMedicationTypeDetail, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = 80;
		lblCalcEndDate.setLayoutData(gd);
		lblCalcEndDate.setText("(" + Messages.ArticleDefaultSignatureComposite_date_none + ")");
		lblCalcEndDate.setData(null);
		Text txtEnddate = new Text(compositeMedicationTypeDetail, SWT.BORDER | SWT.CENTER);
		txtEnddate.setText("");
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = 30;
		txtEnddate.setLayoutData(gd);
		
		Label lblDays = new Label(compositeMedicationTypeDetail, SWT.NONE);
		lblDays.setText("Tage");
		
		txtEnddate.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e){
				try {
					int days = Integer.parseInt(txtEnddate.getText());
					TimeTool t = new TimeTool();
					// maximum reached
					if (days > (365 * 500)) {
						days = 365 * 500;
					}
					// minimum reached
					if (days < 0) {
						days = 0;
					}
					t.addDays(days);
					lblCalcEndDate.setText("(" + t.toString(TimeTool.DATE_GER) + ")");
					lblCalcEndDate.setData(t);
					return;
					
				} catch (NumberFormatException ex) {
				}
				lblCalcEndDate
					.setText("(" + Messages.ArticleDefaultSignatureComposite_date_none + ")");
				lblCalcEndDate.setData(null);
			}
		});
		
		updateMedicationTypeDetails();
	}
	
	public void updateMedicationTypeDetails(){
		if (compositeMedicationTypeDetail != null) {
			boolean visible = btnSymtomatic != null && btnSymtomatic.getSelection();
			compositeMedicationTypeDetail.setVisible(visible);
			GridData data = (GridData) compositeMedicationTypeDetail.getLayoutData();
			data.exclude = !visible;
			compositeMedicationTypeDetail.getParent().layout();
		}
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
	
	public void setMedicationTypeFix(){
		medicationType.setVisible(false);
		btnFix.setSelection(true);
		btnSymtomatic.setSelection(false);
		btnReserve.setSelection(false);
		medicationType.getParent().layout();
		updateMedicationTypeDetails();
	}
	
	public DataBindingContext initDataBindings(DataBindingContext dbc){
		if (dbc == null) {
			databindingContext = new DataBindingContext();
		} else {
			databindingContext = dbc;
		}
		
		targetToModelStrategies = new ArrayList<>();
		
		IObservableValue observeTextTextSignatureMorningObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureMorning);
		IObservableValue itemSignatureMorningObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "morning", String.class) //$NON-NLS-1$
				.observeDetail(signatureItem);
		SavingTargetToModelStrategy targetToModelStrategy = new SavingTargetToModelStrategy(this);
		targetToModelStrategies.add(targetToModelStrategy);
		databindingContext.bindValue(observeTextTextSignatureMorningObserveWidget,
			itemSignatureMorningObserveDetailValue, targetToModelStrategy, null);
		
		IObservableValue observeTextTextSignatureNoonObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureNoon);
		IObservableValue itemSignatureNoonObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "noon", String.class) //$NON-NLS-1$
				.observeDetail(signatureItem);
		targetToModelStrategy = new SavingTargetToModelStrategy(this);
		targetToModelStrategies.add(targetToModelStrategy);
		databindingContext.bindValue(observeTextTextSignatureNoonObserveWidget,
			itemSignatureNoonObserveDetailValue, targetToModelStrategy, null);
		
		IObservableValue observeTextTextSignatureEveningObserveWidget =
				WidgetProperties.text(new int[] {
					SWT.Modify, SWT.FocusOut
				}).observeDelayed(100, txtSignatureEvening);
			IObservableValue itemSignatureEveningObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "evening", String.class) //$NON-NLS-1$
					.observeDetail(signatureItem);
		targetToModelStrategy = new SavingTargetToModelStrategy(this);
		targetToModelStrategies.add(targetToModelStrategy);
			databindingContext.bindValue(observeTextTextSignatureEveningObserveWidget,
			itemSignatureEveningObserveDetailValue, targetToModelStrategy, null);
		
		IObservableValue observeTextTextSignatureNightObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureNight);
		IObservableValue itemSignatureNightObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "night", String.class) //$NON-NLS-1$
				.observeDetail(signatureItem);
		targetToModelStrategy = new SavingTargetToModelStrategy(this);
		targetToModelStrategies.add(targetToModelStrategy);
		databindingContext.bindValue(observeTextTextSignatureNightObserveWidget,
			itemSignatureNightObserveDetailValue, targetToModelStrategy, null);
		
		IObservableValue observeTextFreeTextDosageObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtFreeTextDosage);
		IObservableValue itemSignatureFreeTextDosageObserveDetailValue = PojoProperties
			.value(ArticleSignature.class, "freeText", String.class).observeDetail(signatureItem); //$NON-NLS-1$
		targetToModelStrategy = new SavingTargetToModelStrategy(this);
		targetToModelStrategies.add(targetToModelStrategy);
		databindingContext.bindValue(observeTextFreeTextDosageObserveWidget,
			itemSignatureFreeTextDosageObserveDetailValue, targetToModelStrategy, null);
		
		IObservableValue observeTextTextSignatureCommentObserveWidget =
			WidgetProperties.text(new int[] {
				SWT.Modify, SWT.FocusOut
			}).observeDelayed(100, txtSignatureComment);
		IObservableValue itemSignatureCommentObserveDetailValue =
			PojoProperties.value(ArticleSignature.class, "comment", String.class) //$NON-NLS-1$
				.observeDetail(signatureItem);
		targetToModelStrategy = new SavingTargetToModelStrategy(this);
		targetToModelStrategies.add(targetToModelStrategy);
		databindingContext.bindValue(observeTextTextSignatureCommentObserveWidget,
			itemSignatureCommentObserveDetailValue, targetToModelStrategy, null);
		
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
			if (lblCalcEndDate != null) {
				signature.setEndDate(lblCalcEndDate.getData() instanceof TimeTool
						? (TimeTool) lblCalcEndDate.getData() : null);
			}
		}
		updateMedicationTypeDetails();
	}
	
	public void updateTargetNonDatabinding(){
		ArticleSignature signature = getSignature();
		
		String freeText = signature.getFreeText();
		if (freeText != null && !freeText.isEmpty()) {
			stackLayoutDosage.topControl = compositeFreeTextDosage;
		} else {
			stackLayoutDosage.topControl = compositeDayTimeDosage;
		}
		stackCompositeDosage.layout();
		
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
				if(CoreHub.userCfg
						.get(CreatePrescriptionHelper.MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION, false)) {
					btnDispensation.setSelection(true);
				} else {
					btnNoDisposal.setSelection(true);
				}
			}
			if (signature.isAtc()) {
				btnRadioOnAtcCode.setSelection(true);
			} else {
				btnRadioOnArticle.setSelection(true);
			}
		}
		updateMedicationTypeDetails();
	}
	
	public void safeToDefault(){
		ArticleSignature signature = getSignature();
		
		// dont save if no medication type is selected
		if (!btnFix.getSelection() && !btnReserve.getSelection() && !btnSymtomatic.getSelection()) {
			return;
		}
		
		if (signature != null) {
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
	
	public void setSignature(ArticleSignature signature){
		signatureItem.setValue(signature);
		updateTargetNonDatabinding();
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
	
	public void setAutoSave(boolean value){
		if (targetToModelStrategies != null) {
			for (SavingTargetToModelStrategy savingTargetToModelStrategy : targetToModelStrategies) {
				savingTargetToModelStrategy.setAutoSave(value);
			}
		}
	}
	
	private static class SavingTargetToModelStrategy extends UpdateValueStrategy {
		private boolean autoSave;
		private ArticleDefaultSignatureComposite composite;
		
		public SavingTargetToModelStrategy(ArticleDefaultSignatureComposite composite){
			this.composite = composite;
		}
		
		public void setAutoSave(boolean value){
			autoSave = value;
		}
		
		@Override
		protected IStatus doSet(IObservableValue observableValue, Object value){
			IStatus ret = super.doSet(observableValue, value);
			if (autoSave) {
				composite.safeToDefault();
			}
			return ret;
		}
	}
}
