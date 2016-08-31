package ch.elexis.core.ui.eigenartikel;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.model.eigenartikel.EigenartikelTyp;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.data.Kontakt;

public class EigenartikelComposite extends Composite implements IUnlockable {
	private DataBindingContext m_bindingContext;
	
	private WritableValue productEigenartikel = new WritableValue(null, Eigenartikel.class);
	private WritableValue drugPackageEigenartikel = new WritableValue(null, Eigenartikel.class);
	
	private Button btnAddDrugPackage;
	private Button btnDeleteDrugPackage;
	private Text txtProductName;
	private Text txtAtcCode;
	private Text txtPackageSizeString;
	private Text txtGtin;
	private Text txtPharmacode;
	private Text txtPackageSizeInt;
	private Text txtExfPrice;
	private Text txtpubPrice;
	private Text txtMinOnStock;
	private Text txtMaxOnStock;
	private Text txtCurrOnStock;
	private Combo comboDpSelector;
	private ComboViewer comboViewerDpSelector;
	private ComboViewer comboViewerProductType;
	private Combo comboProductType;
	private Label lblAtcCode;
	private Label lblProvider;
	private Label lblMeasurementUnit;
	private Text txtMeasurementUnit;
	private Group grpDrugPackages;
	private Button btnHiCostAbsorption;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public EigenartikelComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label lblProductName = new Label(this, SWT.NONE);
		lblProductName.setAlignment(SWT.RIGHT);
		lblProductName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProductName.setText(Messages.EigenartikelDisplay_productName);
		
		txtProductName = new Text(this, SWT.BORDER);
		txtProductName.setTextLimit(127);
		txtProductName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblProductType = new Label(this, SWT.NONE);
		lblProductType.setAlignment(SWT.RIGHT);
		lblProductType.setText(Messages.EigenartikelComposite_lblProductType_text);
		
		comboViewerProductType = new ComboViewer(this, SWT.NONE);
		comboProductType = comboViewerProductType.getCombo();
		comboProductType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Eigenartikel.copyProductAttributesToArticleSetAsChild(getProductArtikel(), null);
				ElexisEventDispatcher.update(getProductArtikel());
			}
		});
		GridData gd_comboProductType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comboProductType.widthHint = 150;
		comboProductType.setLayoutData(gd_comboProductType);
		comboViewerProductType.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerProductType.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((EigenartikelTyp) element).getLocaleText();
			}
		});
		comboViewerProductType.setInput(EigenartikelTyp.values());
		
		lblAtcCode = new Label(this, SWT.NONE);
		lblAtcCode.setAlignment(SWT.RIGHT);
		lblAtcCode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAtcCode.setText(Messages.EigenartikelDisplay_atcCode);
		
		Composite compAtcCode = new Composite(this, SWT.NONE);
		GridLayout gl_compAtcCode = new GridLayout(2, false);
		gl_compAtcCode.marginWidth = 0;
		gl_compAtcCode.marginHeight = 0;
		compAtcCode.setLayout(gl_compAtcCode);
		compAtcCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		txtAtcCode = new Text(compAtcCode, SWT.BORDER);
		GridData gd_txtAtcCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtAtcCode.widthHint = 80;
		txtAtcCode.setLayoutData(gd_txtAtcCode);
		txtAtcCode.setTextLimit(8);

		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		grpDrugPackages = new Group(this, SWT.NONE);
		grpDrugPackages.setLayout(new GridLayout(1, false));
		grpDrugPackages.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		grpDrugPackages.setText(Messages.EigenartikelComposite_grpDrugPackages_text);
		
		Composite compDpSelector = new Composite(grpDrugPackages, SWT.NONE);
		compDpSelector.setLayout(new GridLayout(3, false));
		compDpSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnAddDrugPackage = new Button(compDpSelector, SWT.FLAT);
		btnAddDrugPackage.setImage(Images.IMG_NEW.getImage());
		btnAddDrugPackage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Eigenartikel product = (Eigenartikel) productEigenartikel.getValue();
				if (product != null) {
					Eigenartikel articleNew =
						new Eigenartikel(product.getName(), product.getInternalName());
					Eigenartikel.copyProductAttributesToArticleSetAsChild(product, articleNew);
					comboViewerDpSelector.add(articleNew);
					comboViewerDpSelector.setSelection(new StructuredSelection(articleNew));
					ElexisEventDispatcher.reload(Eigenartikel.class);
				}
			}
			
		});
		
		btnDeleteDrugPackage = new Button(compDpSelector, SWT.FLAT);
		btnDeleteDrugPackage.setImage(Images.IMG_DELETE.getImage());
		btnDeleteDrugPackage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				StructuredSelection ss = (StructuredSelection) comboViewerDpSelector.getSelection();
				if (ss.isEmpty()) {
					return;
				}
				
				Eigenartikel selection = (Eigenartikel) ss.getFirstElement();
				selection.delete();
				comboViewerDpSelector.remove(selection);
			}
		});
		
		comboViewerDpSelector = new ComboViewer(compDpSelector, SWT.NONE);
		comboDpSelector = comboViewerDpSelector.getCombo();
		comboDpSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewerDpSelector.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerDpSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				Eigenartikel ea = (Eigenartikel) element;
				String id = "EAN: " + ea.getEAN() + " Pharmacode: " + ea.getPharmaCode();
				return ea.getPackageSizeLabel() + " (" + id + ")";
			}
		});
		comboViewerDpSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				if (ss.isEmpty()) {
					drugPackageEigenartikel.setValue(null);
				} else {
					drugPackageEigenartikel.setValue((Eigenartikel) ss.getFirstElement());
				}
			}
		});
		
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
		lblPharmacode.setText(Messages.EigenartikelDisplay_Pharmacode);
		
		txtPharmacode = new Text(compDpDetail, SWT.BORDER);
		txtPharmacode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPackagesint = new Label(compDpDetail, SWT.NONE);
		lblPackagesint.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPackagesint.setText(Messages.EigenartikelDisplay_PiecesPerPack);
		
		txtPackageSizeInt = new Text(compDpDetail, SWT.BORDER);
		txtPackageSizeInt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lblMeasurementUnit = new Label(compDpDetail, SWT.NONE);
		lblMeasurementUnit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMeasurementUnit.setText(Messages.EigenartikelComposite_lblMeasurementUnit_text);
		
		txtMeasurementUnit = new Text(compDpDetail, SWT.BORDER);
		txtMeasurementUnit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPackagesstring = new Label(compDpDetail, SWT.NONE);
		lblPackagesstring.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPackagesstring.setText(Messages.EigenartikelComposite_lblPackagesstring_text);
		
		txtPackageSizeString = new Text(compDpDetail, SWT.BORDER);
		txtPackageSizeString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(compDpDetail, SWT.NONE);
		new Label(compDpDetail, SWT.NONE);
		
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
		
		Label lblMinOnStock = new Label(compDpDetail, SWT.NONE);
		lblMinOnStock.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMinOnStock.setText(Messages.EigenartikelDisplay_minOnStock);
		
		txtMinOnStock = new Text(compDpDetail, SWT.BORDER);
		txtMinOnStock.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMaxOnStock = new Label(compDpDetail, SWT.NONE);
		lblMaxOnStock.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaxOnStock.setText(Messages.EigenartikelDisplay_maxOnStock);
		
		txtMaxOnStock = new Text(compDpDetail, SWT.BORDER);
		txtMaxOnStock.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblCurrOnStock = new Label(compDpDetail, SWT.NONE);
		lblCurrOnStock.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCurrOnStock.setText(Messages.EigenartikelDisplay_actualOnStockPacks);
		
		txtCurrOnStock = new Text(compDpDetail, SWT.BORDER);
		txtCurrOnStock.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(compDpDetail, SWT.NONE);
		new Label(compDpDetail, SWT.NONE);
		
		Link linkProvider = new Link(compDpDetail, SWT.NONE);
		linkProvider.setText(Messages.EigenartikelComposite_linkProvider_text);
		linkProvider.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		linkProvider.addListener(SWT.Selection, e -> {
			KontaktSelektor ksl = new KontaktSelektor(Hub.getActiveShell(), Kontakt.class,
				Messages.EigenartikelDisplay_dealer,
				Messages.EigenartikelDisplay_pleaseChooseDealer, Kontakt.DEFAULT_SORT);
			if (ksl.open() == Dialog.OK) {
				Kontakt k = (Kontakt) ksl.getSelection();
				Eigenartikel value = (Eigenartikel) drugPackageEigenartikel.getValue();
				value.setLieferant(k);
				lblProvider.setText(value.getLieferant().getLabel());
			}
		});
		
		lblProvider = new Label(compDpDetail, SWT.NONE);
		lblProvider.setText("no provider set");
		lblProvider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		m_bindingContext = initDataBindings();
		
	}
	
	public void setProductEigenartikel(Eigenartikel productEigenartikel){
		this.productEigenartikel.setValue(productEigenartikel);
		this.drugPackageEigenartikel.setValue(null);
		
		if (productEigenartikel != null && productEigenartikel.isProduct()) {
			grpDrugPackages.setVisible(true);
			List<Eigenartikel> packages = productEigenartikel.getPackages();
			comboViewerDpSelector.setInput(productEigenartikel.getPackages());
			if (packages.size() > 0) {
				comboViewerDpSelector.setSelection(new StructuredSelection(packages.get(0)));
			}
		} else {
			comboViewerDpSelector.setInput(null);
			grpDrugPackages.setVisible(false);
		}
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public void setUnlocked(boolean unlocked){
		txtProductName.setEditable(unlocked);
		txtAtcCode.setEditable(unlocked);
		comboProductType.setEnabled(unlocked);
		btnAddDrugPackage.setEnabled(unlocked);
		btnDeleteDrugPackage.setEnabled(unlocked);
		btnHiCostAbsorption.setEnabled(unlocked);
		txtGtin.setEditable(unlocked);
		txtPharmacode.setEditable(unlocked);
		txtPackageSizeInt.setEditable(unlocked);
		txtMeasurementUnit.setEditable(unlocked);
		txtPackageSizeString.setEditable(unlocked);
		txtExfPrice.setEditable(unlocked);
		txtpubPrice.setEditable(unlocked);
	}
	
	public Eigenartikel getProductArtikel(){
		return (Eigenartikel) productEigenartikel.getValue();
	}
	
	protected DataBindingContext initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		
		UpdateValueStrategy strategyUpdateProductChilds = new UpdateValueStrategy() {
			@Override
			protected IStatus doSet(IObservableValue observableValue, Object value){
				IStatus status = super.doSet(observableValue, value);
				Eigenartikel.copyProductAttributesToArticleSetAsChild(getProductArtikel(), null);
				ElexisEventDispatcher.update(getProductArtikel());
				return status;
			}
		};
		
		//
		IObservableValue observeTextTxtProductNameObserveWidget =
			WidgetProperties.text(SWT.Modify).observeDelayed(300, txtProductName);
		IObservableValue productEigenartikelNameObserveDetailValue = PojoProperties
			.value(Eigenartikel.class, "name", String.class).observeDetail(productEigenartikel);
		bindingContext.bindValue(observeTextTxtProductNameObserveWidget,
			productEigenartikelNameObserveDetailValue, strategyUpdateProductChilds, null);
		//
		IObservableValue observeSingleSelectionComboViewerProductType =
			ViewerProperties.singleSelection().observe(comboViewerProductType);
		IObservableValue productEigenartikelTypObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "typ", EigenartikelTyp.class)
				.observeDetail(productEigenartikel);
		bindingContext.bindValue(observeSingleSelectionComboViewerProductType,
			productEigenartikelTypObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtAtcCodeObserveWidget =
			WidgetProperties.text(SWT.Modify).observeDelayed(300, txtAtcCode);
		IObservableValue productEigenartikelATC_codeObserveDetailValue = PojoProperties
			.value(Eigenartikel.class, "ATC_code", String.class).observeDetail(productEigenartikel);
		bindingContext.bindValue(observeTextTxtAtcCodeObserveWidget,
			productEigenartikelATC_codeObserveDetailValue, strategyUpdateProductChilds, null);
		//
		IObservableValue observeTextTxtGtinObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtGtin);
		IObservableValue drugPackageEigenartikelEANObserveDetailValue = PojoProperties
			.value(Eigenartikel.class, "EAN", String.class).observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtGtinObserveWidget,
			drugPackageEigenartikelEANObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtPackageSizeIntObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtPackageSizeInt);
		IObservableValue drugPackageEigenartikelPackungsGroesseObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "packageSize", Integer.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtPackageSizeIntObserveWidget,
			drugPackageEigenartikelPackungsGroesseObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtExfPriceObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtExfPrice);
		IObservableValue drugPackageEigenartikelEKPreisObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "exfPrice", String.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtExfPriceObserveWidget,
			drugPackageEigenartikelEKPreisObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtpubPriceObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtpubPrice);
		IObservableValue drugPackageEigenartikelVKPreisObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "pubPrice", String.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtpubPriceObserveWidget,
			drugPackageEigenartikelVKPreisObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtMaxOnStockObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtMaxOnStock);
		IObservableValue drugPackageEigenartikelMaxbestandObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "maxOnStock", Integer.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtMaxOnStockObserveWidget,
			drugPackageEigenartikelMaxbestandObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtMinOnStockObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtMinOnStock);
		IObservableValue drugPackageEigenartikelMinbestandObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "minOnStock", Integer.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtMinOnStockObserveWidget,
			drugPackageEigenartikelMinbestandObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtCurrOnStockObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtCurrOnStock);
		IObservableValue drugPackageEigenartikelIstbestandObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "currentOnStock", Integer.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtCurrOnStockObserveWidget,
			drugPackageEigenartikelIstbestandObserveDetailValue, null, null);
		//
		IObservableValue observeTextLblProviderObserveWidget =
			WidgetProperties.text().observe(lblProvider);
		IObservableValue drugPackageEigenartikelLieferantlabelObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "lieferant.label", String.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextLblProviderObserveWidget,
			drugPackageEigenartikelLieferantlabelObserveDetailValue,
			new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
		//
		IObservableValue observeTextTxtMeasurementUnitObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtMeasurementUnit);
		IObservableValue drugPackageEigenartikelMeasurementUnitObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "measurementUnit", String.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtMeasurementUnitObserveWidget,
			drugPackageEigenartikelMeasurementUnitObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtPharmacodeObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtPharmacode);
		IObservableValue drugPackageEigenartikelPharmaCodeObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "pharmaCode", String.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTextTxtPharmacodeObserveWidget,
			drugPackageEigenartikelPharmaCodeObserveDetailValue, null, null);
		//
		IObservableValue observeTooltipTextTxtPackageSizeStringObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtPackageSizeString);
		IObservableValue drugPackageEigenartikelPackageSizeStringObserveDetailValue =
			PojoProperties.value(Eigenartikel.class, "packageSizeString", String.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeTooltipTextTxtPackageSizeStringObserveWidget,
			drugPackageEigenartikelPackageSizeStringObserveDetailValue, null, null);
		//
		org.eclipse.core.databinding.observable.value.IObservableValue observeSelectionBtnHiCostAbsorptionObserveWidget =
			org.eclipse.jface.databinding.swt.WidgetProperties.selection()
				.observe(btnHiCostAbsorption);
		org.eclipse.core.databinding.observable.value.IObservableValue drugPackageEigenartikelHealthInsuranceCostAbsorptionObserveDetailValue =
			org.eclipse.core.databinding.beans.PojoProperties
				.value(ch.elexis.core.eigenartikel.Eigenartikel.class,
					"healthInsuranceCostAbsorption", boolean.class)
				.observeDetail(drugPackageEigenartikel);
		bindingContext.bindValue(observeSelectionBtnHiCostAbsorptionObserveWidget,
			drugPackageEigenartikelHealthInsuranceCostAbsorptionObserveDetailValue, null, null);
		
		return bindingContext;
	}
}
