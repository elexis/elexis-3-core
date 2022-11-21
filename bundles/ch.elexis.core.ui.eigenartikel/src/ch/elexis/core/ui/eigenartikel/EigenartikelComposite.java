package ch.elexis.core.ui.eigenartikel;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.databinding.SavingUpdateValueStrategy;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.views.controls.StockDetailComposite;
import ch.rgw.tools.Money;

public class EigenartikelComposite extends Composite implements IUnlockable {

	private WritableValue<IArticle> drugPackageEigenartikel = new WritableValue<>(null, IArticle.class);

	private final boolean includeDeleteOption;

	private Button btnDeleteDrugPackage;
	private Text txtPackageSizeString;
	private Text txtGtin;
	private Text txtPharmacode;
	private Text txtPackageSizeInt;
	private Text txtExfPrice;
	private Text txtpubPrice;
	private Label lblMeasurementUnit;
	private Text txtMeasurementUnit;
	private Group grpDrugPackages;
	private Button btnHiCostAbsorption;
	private Text txtSellUnit;
	private Label lblVerkaufseinheit;
	private StockDetailComposite stockDetailComposite;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 */
	public EigenartikelComposite(Composite parent, int style, IArticle eigenartikel) {
		this(parent, style, true, eigenartikel);
	}

	public EigenartikelComposite(Composite parent, int style, boolean includeDeleteOption, IArticle eigenartikel) {
		super(parent, style);
		this.includeDeleteOption = includeDeleteOption;
		this.drugPackageEigenartikel.setValue(eigenartikel);

		setLayout(new GridLayout(2, false));
		createArticlePart();
	}

	public void setEigenartikel(IArticle eigenartikel) {
		this.drugPackageEigenartikel.setValue(eigenartikel);
	}

	private void createArticlePart() {
		grpDrugPackages = new Group(this, SWT.NONE);
		grpDrugPackages.setLayout(new GridLayout(1, false));
		grpDrugPackages.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		grpDrugPackages.setText(StringUtils.EMPTY);

		if (includeDeleteOption) {
			Composite compDpSelector = new Composite(grpDrugPackages, SWT.NONE);
			compDpSelector.setLayout(new GridLayout(3, false));
			compDpSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			btnDeleteDrugPackage = new Button(compDpSelector, SWT.FLAT);
			btnDeleteDrugPackage.setText(Messages.EigenartikelComposite_deleteArticle_text);
			btnDeleteDrugPackage.setToolTipText(Messages.EigenartikelComposite_deleteArticle_text);
			btnDeleteDrugPackage.setImage(Images.IMG_DELETE.getImage());
			btnDeleteDrugPackage.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (drugPackageEigenartikel.getValue() != null) {
						CoreModelServiceHolder.get().delete(drugPackageEigenartikel.getValue());
						Composite p = getParent();
						dispose();

						if (p.getParent() instanceof ScrolledComposite) {
							ScrolledComposite sc = (ScrolledComposite) p.getParent();
							sc.setMinSize(p.computeSize(SWT.DEFAULT, SWT.DEFAULT));
							sc.layout(true, true);
						}
					}
				}
			});
		}

		Composite compDpDetail = new Composite(grpDrugPackages, SWT.BORDER);
		compDpDetail.setLayout(new GridLayout(4, false));
		compDpDetail.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		Label lblGtin = new Label(compDpDetail, SWT.NONE);
		lblGtin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblGtin.setText(Messages.EigenartikelDisplay_gtin);

		txtGtin = new Text(compDpDetail, SWT.BORDER);
		txtGtin.setTextLimit(20);
		txtGtin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPharmacode = new Label(compDpDetail, SWT.NONE);
		lblPharmacode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPharmacode.setText(Messages.Core_Phamacode);

		txtPharmacode = new Text(compDpDetail, SWT.BORDER);
		txtPharmacode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPackagesint = new Label(compDpDetail, SWT.NONE);
		lblPackagesint.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPackagesint.setText(Messages.Core_Pieces_per_pack);

		txtPackageSizeInt = new Text(compDpDetail, SWT.BORDER);
		txtPackageSizeInt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblMeasurementUnit = new Label(compDpDetail, SWT.NONE);
		lblMeasurementUnit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMeasurementUnit.setText(Messages.EigenartikelComposite_lblMeasurementUnit_text);

		txtMeasurementUnit = new Text(compDpDetail, SWT.BORDER);
		txtMeasurementUnit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPackagesstring = new Label(compDpDetail, SWT.NONE);
		lblPackagesstring.setToolTipText(Messages.EigenartikelComposite_lblPackagesstring_toolTipText);
		lblPackagesstring.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPackagesstring.setText(Messages.EigenartikelComposite_lblPackagesstring_text);

		txtPackageSizeString = new Text(compDpDetail, SWT.BORDER);
		txtPackageSizeString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblVerkaufseinheit = new Label(compDpDetail, SWT.NONE);
		lblVerkaufseinheit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVerkaufseinheit.setText(Messages.EigenartikelComposite_lblVerkaufseinheit_text);

		txtSellUnit = new Text(compDpDetail, SWT.BORDER);
		txtSellUnit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblExFactoryPrice = new Label(compDpDetail, SWT.NONE);
		lblExFactoryPrice.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblExFactoryPrice.setText(Messages.EigenartikelDisplay_buyPrice);

		txtExfPrice = new Text(compDpDetail, SWT.BORDER);
		txtExfPrice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPublicPrice = new Label(compDpDetail, SWT.NONE);
		lblPublicPrice.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPublicPrice.setText(Messages.EigenartikelDisplay_sellPrice);

		txtpubPrice = new Text(compDpDetail, SWT.BORDER);
		txtpubPrice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(compDpDetail, SWT.NONE);

		btnHiCostAbsorption = new Button(compDpDetail, SWT.CHECK);
		btnHiCostAbsorption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		btnHiCostAbsorption.setText(Messages.EigenartikelComposite_btnCheckButton_text);

		Group stockGroup = new Group(grpDrugPackages, SWT.NONE);
		stockGroup.setText(Messages.Core_Inventory_control);
		stockGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_stockGroup = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_stockGroup.heightHint = 80;
		stockGroup.setLayoutData(gd_stockGroup);

		stockDetailComposite = new StockDetailComposite(stockGroup, SWT.NONE);
		stockDetailComposite.setArticle(drugPackageEigenartikel.getValue());

		initDataBindings();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		if (includeDeleteOption) {
			btnDeleteDrugPackage.setEnabled(unlocked);
		}
		btnHiCostAbsorption.setEnabled(unlocked);
		txtGtin.setEditable(unlocked);
		txtPharmacode.setEditable(unlocked);
		txtPackageSizeInt.setEditable(unlocked);
		txtMeasurementUnit.setEditable(unlocked);
		txtPackageSizeString.setEditable(unlocked);
		txtExfPrice.setEditable(unlocked);
		txtpubPrice.setEditable(unlocked);
		txtSellUnit.setEditable(unlocked);
		stockDetailComposite.setEnabled(unlocked);
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		ISWTObservableValue observeTextTxtGtinObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtGtin);
		IObservableValue<String> drugPackageEigenartikelEANObserveDetailValue = PojoProperties
				.value(IArticle.class, "gtin", String.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtGtinObserveWidget, drugPackageEigenartikelEANObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel), null);
		//
		ISWTObservableValue observeTextTxtPackageSizeIntObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtPackageSizeInt);
		IObservableValue<Integer> drugPackageEigenartikelPackungsGroesseObserveDetailValue = PojoProperties
				.value(IArticle.class, "packageSize", Integer.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtPackageSizeIntObserveWidget,
				drugPackageEigenartikelPackungsGroesseObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel), null);
		observeTextTxtPackageSizeIntObserveWidget.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (drugPackageEigenartikel.getValue() != null) {
					if (event.diff.getOldValue() != null && !event.diff.getOldValue().toString().isEmpty()) {
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE,
								drugPackageEigenartikel.getValue());
					}
				}
			}
		});

		// //
		ISWTObservableValue observeTextTxtExfPriceObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtExfPrice);
		IObservableValue<String> drugPackageEigenartikelEKPreisObserveDetailValue = PojoProperties
				.value(IArticle.class, "purchasePrice", Money.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		SavingUpdateValueStrategy target2ModelStrategy = new SavingUpdateValueStrategy(CoreModelServiceHolder.get(),
				drugPackageEigenartikel);
		target2ModelStrategy.setConverter(new String2MoneyConverter());
		UpdateValueStrategy model2TargetStrategy = new UpdateValueStrategy<>();
		model2TargetStrategy.setConverter(new Money2StringConverter());
		bindingContext.bindValue(observeTextTxtExfPriceObserveWidget, drugPackageEigenartikelEKPreisObserveDetailValue,
				target2ModelStrategy, model2TargetStrategy);
		// //
		ISWTObservableValue observeTextTxtpubPriceObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtpubPrice);
		IObservableValue<Money> drugPackageEigenartikelVKPreisObserveDetailValue = PojoProperties
				.value(IArticle.class, "sellingPrice", Money.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		target2ModelStrategy = new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel);
		target2ModelStrategy.setConverter(new String2MoneyConverter());
		model2TargetStrategy = new UpdateValueStrategy<>();
		model2TargetStrategy.setConverter(new Money2StringConverter());
		bindingContext.bindValue(observeTextTxtpubPriceObserveWidget, drugPackageEigenartikelVKPreisObserveDetailValue,
				target2ModelStrategy, model2TargetStrategy);
		// //
		ISWTObservableValue observeTextTxtMeasurementUnitObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtMeasurementUnit);
		IObservableValue<String> drugPackageEigenartikelMeasurementUnitObserveDetailValue = PojoProperties
				.value(IArticle.class, "packageUnit", String.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtMeasurementUnitObserveWidget,
				drugPackageEigenartikelMeasurementUnitObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel), null);
		observeTextTxtMeasurementUnitObserveWidget.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (drugPackageEigenartikel.getValue() != null) {
					if (event.diff.getOldValue() != null && !event.diff.getOldValue().toString().isEmpty()) {
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE,
								drugPackageEigenartikel.getValue());
					}
				}
			}
		});
		//
		// //
		UpdateValueStrategy noIdCodeUpdateValuStrategy = new UpdateValueStrategy();
		noIdCodeUpdateValuStrategy.setBeforeSetValidator(new IValidator() {
			@Override
			public IStatus validate(Object value) {
				if (value instanceof String && drugPackageEigenartikel.getValue() != null) {
					if (drugPackageEigenartikel.getValue().getId().equals(value)) {
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		});

		ISWTObservableValue observeTextTxtPharmacodeObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtPharmacode);
		IObservableValue<String> drugPackageEigenartikelPharmaCodeObserveDetailValue = PojoProperties
				.value(ICodeElement.class, "code", String.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtPharmacodeObserveWidget,
				drugPackageEigenartikelPharmaCodeObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel),
				noIdCodeUpdateValuStrategy);

		ISWTObservableValue observeTextTxtSellUnitObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtSellUnit);
		IObservableValue<Integer> drugPackageEigenartikelSellUnitObserveDetailValue = PojoProperties
				.value(IArticle.class, "sellingSize", Integer.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtSellUnitObserveWidget, drugPackageEigenartikelSellUnitObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel), null);
		observeTextTxtSellUnitObserveWidget.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (drugPackageEigenartikel.getValue() != null) {
					if (event.diff.getOldValue() != null && !event.diff.getOldValue().toString().isEmpty()) {
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE,
								drugPackageEigenartikel.getValue());
					}
				}
			}
		});

		//
		ISWTObservableValue observeTooltipTextTxtPackageSizeStringObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtPackageSizeString);
		IObservableValue<String> drugPackageEigenartikelPackageSizeStringObserveDetailValue = PojoProperties
				.value(IArticle.class, "packageSizeString", String.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeTooltipTextTxtPackageSizeStringObserveWidget,
				drugPackageEigenartikelPackageSizeStringObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel), null);
		//
		ISWTObservableValue observeSelectionBtnHiCostAbsorptionObserveWidget = WidgetProperties.selection()
				.observe(btnHiCostAbsorption);
		IObservableValue<Boolean> drugPackageEigenartikelHealthInsuranceCostAbsorptionObserveDetailValue = PojoProperties
				.value(IArticle.class, "obligation", Boolean.class).observeDetail(drugPackageEigenartikel); //$NON-NLS-1$
		bindingContext.bindValue(observeSelectionBtnHiCostAbsorptionObserveWidget,
				drugPackageEigenartikelHealthInsuranceCostAbsorptionObserveDetailValue,
				new SavingUpdateValueStrategy(CoreModelServiceHolder.get(), drugPackageEigenartikel), null);
		return bindingContext;
	}
}
