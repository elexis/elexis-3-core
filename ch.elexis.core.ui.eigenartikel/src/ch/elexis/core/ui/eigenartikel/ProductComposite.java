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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.model.eigenartikel.EigenartikelTyp;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.SWTHelper;

public class ProductComposite extends Composite implements IUnlockable {
	
	private WritableValue productEigenartikel = new WritableValue(null, Eigenartikel.class);

	private Text txtProductName;
	private Text txtAtcCode;
	private ComboViewer comboViewerProductType;
	private Combo comboProductType;
	private Label lblAtcCode;
	private Button btnAddDrugPackage;
	private ScrolledComposite sc;
	private Composite compositeArticles;
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ProductComposite(Composite parent, int style){
		super(parent, SWT.BORDER_SOLID);
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
		new Label(compAtcCode, SWT.NONE);

		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		btnAddDrugPackage = new Button(this, SWT.FLAT);
		btnAddDrugPackage.setText(Messages.EigenartikelComposite_newArticle_text);
		btnAddDrugPackage.setImage(Images.IMG_NEW.getImage());
		btnAddDrugPackage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		sc = new ScrolledComposite(this, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		
		compositeArticles = new Composite(sc, SWT.BORDER_DASH);
		
		GridLayout gl_compArticles = new GridLayout(1, false);
		gl_compArticles.marginWidth = 0;
		gl_compArticles.marginHeight = 0;
		compositeArticles.setLayout(gl_compArticles);
		compositeArticles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		sc.setContent(compositeArticles);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		btnAddDrugPackage.setVisible(false);
		txtProductName.setEnabled(false);
		txtAtcCode.setEnabled(false);
		comboProductType.setEnabled(false);
		
		comboProductType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Eigenartikel.copyProductAttributesToArticleSetAsChild(getProductArtikel(), null);
				ElexisEventDispatcher.update(getProductArtikel());
			}
		});
		
		btnAddDrugPackage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Eigenartikel product = getProductArtikel();
				if (product != null) {
					Eigenartikel articleNew =
						new Eigenartikel(product.getName(), product.getInternalName());
					Eigenartikel.copyProductAttributesToArticleSetAsChild(product, articleNew);
					createEigenartikelComposite(articleNew);
					sc.setMinSize(compositeArticles.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					sc.layout();
					ElexisEventDispatcher.reload(Eigenartikel.class);
				}
			}
			
		});

		initDataBindings();
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
		
	}
	
	@Override
	public void setUnlocked(boolean unlocked){
		if (getProductArtikel() != null) {
			txtProductName.setEditable(unlocked);
			txtAtcCode.setEditable(unlocked);
			comboProductType.setEnabled(unlocked);
			btnAddDrugPackage.setEnabled(unlocked);
		}
		
	}
	
	public Eigenartikel getProductArtikel(){
		return (Eigenartikel) productEigenartikel.getValue();
	}
	
	public void setProductEigenartikel(Eigenartikel productEigenartikel){
		this.productEigenartikel.setValue(productEigenartikel);
		
		for (Control c : compositeArticles.getChildren()) {
			c.dispose();
		}
		if (productEigenartikel != null && productEigenartikel.isProduct()) {
			btnAddDrugPackage.setVisible(true);
			sc.setVisible(true);
			txtProductName.setEnabled(true);
			txtAtcCode.setEnabled(true);
			comboProductType.setEnabled(true);
			List<Eigenartikel> packages = productEigenartikel.getPackages();
			for (Eigenartikel eigenartikel : packages) {
				createEigenartikelComposite(eigenartikel);
			}
		} else {
			sc.setVisible(false);
			btnAddDrugPackage.setVisible(false);
			txtProductName.setEnabled(false);
			txtAtcCode.setEnabled(false);
			comboProductType.setEnabled(false);
			btnAddDrugPackage.setVisible(false);
		}
		sc.setMinSize(compositeArticles.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.layout(true, true);
	}
	
	public void initDataBindings()
	{
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
	}

	private void createEigenartikelComposite(Eigenartikel articleNew){
		new EigenartikelComposite(compositeArticles, SWT.NONE, articleNew);
	}
}
