package ch.elexis.core.ui.preferences;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListDialog;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.elexis.data.Stock;

public class StockManagementPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	private DataBindingContext m_bindingContext;
	
	private Table tableStocks;
	private Text txtCode;
	private Text txtDescription;
	private Text txtLocation;
	private Text txtPrio;
	
	private Label lblOwnerText;
	private Label lblResponsibleText;
	
	private Button btnChkStoreInvalidNumbers;
	
	private WritableValue stockDetail = new WritableValue(null, Stock.class);
	private TableViewer tableViewer;
	private Text txtMachineConfig;
	private Label lblMachineuuid;
	
	private Label lblDefaultArticleProvider;
	private Button btnMachineOutlayPartialPackages;
	
	/**
	 * Create the preference page.
	 */
	public StockManagementPreferencePage(){
		setTitle(Messages.LagerverwaltungPrefs_storageManagement);
	}
	
	/**
	 * Create contents of the preference page.
	 * 
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent){
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		Group group = new Group(container, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		group.setText(Messages.StockManagementPreferencePage_group_text);
		
		Composite composite = new Composite(group, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.heightHint = 150;
		composite.setLayoutData(gd_composite);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				Stock s1 = (Stock) e1;
				int s1I = (s1.getPriority() != null) ? s1.getPriority() : Integer.MAX_VALUE;
				Stock s2 = (Stock) e2;
				int s2I = (s2.getPriority() != null) ? s2.getPriority() : Integer.MAX_VALUE;
				return Integer.compare(s1I, s2I);
			}
		});
		tableViewer.addSelectionChangedListener(e -> {
			StructuredSelection ss = (StructuredSelection) e.getSelection();
			if (ss.isEmpty()) {
				stockDetail.setValue(null);
			} else {
				stockDetail.setValue((Stock) ss.getFirstElement());
			}
		});
		tableStocks = tableViewer.getTable();
		tableStocks.setHeaderVisible(true);
		tableStocks.setLinesVisible(true);
		
		Menu menu = new Menu(tableStocks);
		tableStocks.setMenu(menu);
		
		MenuItem mntmAddStock = new MenuItem(menu, SWT.NONE);
		mntmAddStock.setText(Messages.StockManagementPreferencePage_mntmNewItem_text);
		mntmAddStock.setImage(Images.IMG_NEW.getImage());
		mntmAddStock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Stock stock = new Stock("", Integer.MAX_VALUE);
				tableViewer.add(stock);
				tableViewer.setSelection(new StructuredSelection(stock));
			}
		});
		
		MenuItem mntmRemoveStock = new MenuItem(menu, SWT.NONE);
		mntmRemoveStock.setText(Messages.StockManagementPreferencePage_mntmNewItem_text_1);
		mntmRemoveStock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Stock stock = (Stock) stockDetail.getValue();
				if (stock != null) {
					boolean ret = MessageDialog.openQuestion(UiDesk.getTopShell(), "Lager löschen",
						"Das Löschen dieses Lagers löscht alle darauf verzeichneten Lagerbestände. Sind Sie sicher?");
					if (ret) {
						stock.removeFromDatabase();
						tableViewer.remove(stock);
					}
				}
			}
		});
		
		TableViewerColumn tvcPriority = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmPriority = tvcPriority.getColumn();
		tcl_composite.setColumnData(tblclmPriority, new ColumnPixelData(30, true, true));
		tblclmPriority.setText(Messages.StockManagementPreferencePage_tblclmnNewColumn_text);
		tvcPriority.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				Stock s = (Stock) cell.getElement();
				if (s == null)
					return;
				cell.setText(Integer.toString(s.getPriority()));
			}
		});
		
		TableViewerColumn tvcCode = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnCode = tvcCode.getColumn();
		tcl_composite.setColumnData(tblclmnCode, new ColumnPixelData(40));
		tblclmnCode.setText(Messages.StockManagementPreferencePage_tblclmnNewColumn_text_1);
		tvcCode.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				Stock s = (Stock) cell.getElement();
				if (s == null)
					return;
				cell.setText(s.getCode());
			}
		});
		
		TableViewerColumn tvcDescription = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDescription = tvcDescription.getColumn();
		tcl_composite.setColumnData(tblclmnDescription, new ColumnWeightData(50));
		tblclmnDescription.setText(Messages.StockManagementPreferencePage_tblclmnNewColumn_text_3);
		tvcDescription.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				Stock s = (Stock) cell.getElement();
				if (s == null)
					return;
				cell.setText(s.getDescription());
			}
		});
		
		TableViewerColumn tvcOwner = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnOwner = tvcOwner.getColumn();
		tcl_composite.setColumnData(tblclmnOwner, new ColumnWeightData(40));
		tblclmnOwner.setText(Messages.StockManagementPreferencePage_tblclmnNewColumn_text_2);
		tvcOwner.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				Stock s = (Stock) cell.getElement();
				if (s != null) {
					Mandant owner = s.getOwner();
					if (owner != null) {
						cell.setText(owner.getLabel());
					}
				}
			}
		});
		
		Composite compositeDetail = new Composite(group, SWT.NONE);
		compositeDetail.setLayout(new GridLayout(4, false));
		compositeDetail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		
		Label lblPrio = new Label(compositeDetail, SWT.NONE);
		lblPrio.setText(Messages.StockManagementPreferencePage_lblPrio_text);
		
		txtPrio = new Text(compositeDetail, SWT.BORDER);
		GridData gd_txtPrio = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtPrio.widthHint = 150;
		txtPrio.setLayoutData(gd_txtPrio);
		
		Label lblCode = new Label(compositeDetail, SWT.NONE);
		lblCode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCode.setToolTipText(Messages.StockManagementPreferencePage_lblCode_toolTipText);
		lblCode.setText(Messages.StockManagementPreferencePage_lblCode_text);
		
		txtCode = new Text(compositeDetail, SWT.BORDER);
		GridData gd_txtCode = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtCode.widthHint = 100;
		txtCode.setLayoutData(gd_txtCode);
		txtCode.setTextLimit(3);
		
		Label lblDescription = new Label(compositeDetail, SWT.NONE);
		lblDescription.setText(Messages.StockManagementPreferencePage_lblDescription_text);
		
		txtDescription = new Text(compositeDetail, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblLocation = new Label(compositeDetail, SWT.NONE);
		lblLocation.setToolTipText(Messages.StockManagementPreferencePage_lblLocation_toolTipText);
		lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLocation.setText(Messages.StockManagementPreferencePage_lblLocation_text);
		
		txtLocation = new Text(compositeDetail, SWT.BORDER);
		txtLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Link lblOwner = new Link(compositeDetail, SWT.NONE);
		lblOwner.setToolTipText(Messages.StockManagementPreferencePage_lblOwner_toolTipText);
		lblOwner.setText(Messages.StockManagementPreferencePage_lblOwner_text);
		lblOwner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Stock s = (Stock) stockDetail.getValue();
				if (s == null)
					return;
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Mandant.class,
					"Mandant auswählen", "Bitte selektieren Sie den Eigentümer", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Mandant p = (Mandant) ks.getSelection();
					s.setOwner(p);
					String label = (p != null) ? p.getLabel() : "";
					lblOwnerText.setText(label);
				} else {
					s.setOwner(null);
					lblOwnerText.setText(StringConstants.EMPTY);
				}
				tableViewer.update(s, null);
			}
		});
		
		lblOwnerText = new Label(compositeDetail, SWT.NONE);
		lblOwnerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Link lblResponsible = new Link(compositeDetail, SWT.NONE);
		lblResponsible
			.setToolTipText(Messages.StockManagementPreferencePage_lblResponsible_toolTipText);
		lblResponsible.setText(Messages.StockManagementPreferencePage_lblResonsible_text);
		lblResponsible.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Stock s = (Stock) stockDetail.getValue();
				if (s == null)
					return;
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Kontakt.class,
					"Lagerverantwortlichen auswählen",
					"Bitte selektieren Sie den Lagerverantwortlichen", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Kontakt p = (Kontakt) ks.getSelection();
					s.setResponsible(p);
					String label = (p != null) ? p.getLabel() : "";
					lblResponsibleText.setText(label);
				} else {
					s.setResponsible(null);
					lblResponsibleText.setText("");
				}
			}
		});
		
		lblResponsibleText = new Label(compositeDetail, SWT.NONE);
		lblResponsibleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblNewLabel = new Label(compositeDetail, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		lblNewLabel.setText(Messages.StockManagementPreferencePage_lblNewLabel_text);
		
		Link lblMachine = new Link(compositeDetail, SWT.NONE);
		lblMachine.setToolTipText(Messages.StockManagementPreferencePage_lblMachine_toolTipText);
		lblMachine.setText(Messages.StockManagementPreferencePage_lblMachine_text);
		lblMachine.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Stock s = (Stock) stockDetail.getValue();
				if (s == null) {
					return;
				}
				List<UUID> allDrivers =
					CoreHub.getStockCommissioningSystemService().listAllAvailableDrivers();
				if (allDrivers.size() == 0) {
					MessageDialog.openInformation(UiDesk.getTopShell(), "No drivers found",
						"There are no stock commissioning system drivers available.");
					return;
				}
				
				ListDialog ld = new ListDialog(UiDesk.getTopShell());
				ld.setTitle("Driver selection");
				ld.setMessage("Please select a commissioning system driver");
				ld.setContentProvider(ArrayContentProvider.getInstance());
				ld.setAddCancelButton(true);
				ld.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element){
						return CoreHub.getStockCommissioningSystemService()
							.getInfoStringForDriver((UUID) element, true);
					}
				});
				ld.setInput(allDrivers);
				ld.setWidthInChars(80);
				ld.setHeightInChars(5);
				int retVal = ld.open();
				UUID ics = null;
				if (Dialog.OK == retVal) {
					Object[] result = ld.getResult();
					if (result.length > 0) {
						ics = (UUID) result[0];
					}
				} else if (Dialog.CANCEL == retVal) {
					ics = null;
				}
				if (ics != null) {
					s.setDriverUuid(ics.toString());
					lblMachineuuid.setText(CoreHub.getStockCommissioningSystemService()
						.getInfoStringForDriver(ics, false));
				} else {
					s.setDriverUuid(null);
					lblMachineuuid.setText(StringConstants.EMPTY);
				}
			}
		});
		
		lblMachineuuid = new Label(compositeDetail, SWT.NONE);
		lblMachineuuid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblMachineConfig = new Label(compositeDetail, SWT.NONE);
		lblMachineConfig.setText(Messages.StockManagementPreferencePage_lblMachineConfig_text);
		txtMachineConfig = new Text(compositeDetail, SWT.BORDER);
		txtMachineConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		btnMachineOutlayPartialPackages = new Button(compositeDetail, SWT.CHECK);
		btnMachineOutlayPartialPackages
			.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnMachineOutlayPartialPackages
			.setText(Messages.StockManagementPreferencePage_btnMachineOutlayPartialPackages_text);
		boolean outlayPartialPackages =
			CoreHub.globalCfg.get(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
				Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT);
		btnMachineOutlayPartialPackages.setSelection(outlayPartialPackages);
		
		btnChkStoreInvalidNumbers = new Button(container, SWT.CHECK);
		btnChkStoreInvalidNumbers.setText(Messages.LagerverwaltungPrefs_checkForInvalid);
		
		Composite compDefaultProvider = new Composite(container, SWT.NONE);
		compDefaultProvider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compDefaultProvider = new GridLayout(3, false);
		gl_compDefaultProvider.marginWidth = 0;
		gl_compDefaultProvider.marginHeight = 0;
		compDefaultProvider.setLayout(gl_compDefaultProvider);
		
		Link linkDefaultArticleProvider = new Link(compDefaultProvider, SWT.NONE);
		linkDefaultArticleProvider.setToolTipText(
			Messages.StockManagementPreferencePage_linkDefaultArticleProvider_toolTipText);
		linkDefaultArticleProvider
			.setText(Messages.StockManagementPreferencePage_linkDefaultArticleProvider_text);
		linkDefaultArticleProvider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Kontakt.class,
					"Standard-Lieferant auswählen",
					"Bitte selektieren Sie den Standard-Lieferanten", new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Kontakt p = (Kontakt) ks.getSelection();
					if (p != null) {
						CoreHub.globalCfg.set(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER,
							p.getId());
						lblDefaultArticleProvider.setText(p.getLabel());
					}
				} else {
					CoreHub.globalCfg.remove(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER);
					lblDefaultArticleProvider.setText("");
				}
			}
		});
		
		lblDefaultArticleProvider = new Label(compDefaultProvider, SWT.NONE);
		lblDefaultArticleProvider
			.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		String id = CoreHub.globalCfg.get(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
		lblDefaultArticleProvider.setText("");
		new Label(compDefaultProvider, SWT.NONE);
		if (id != null) {
			Kontakt load = Kontakt.load(id);
			if (load.exists()) {
				lblDefaultArticleProvider.setText(load.getLabel());
			}
		}
		
		tableViewer.setInput(new Query<Stock>(Stock.class).execute());
		m_bindingContext = initDataBindings();
		
		stockDetail.addChangeListener(e -> {
			Stock stock = (Stock) stockDetail.getValue();
			if (stock != null) {
				Mandant owner = stock.getOwner();
				if (owner != null) {
					lblOwnerText.setText(owner.getLabel());
				} else {
					lblOwnerText.setText("");
				}
				Kontakt responsible = stock.getResponsible();
				if (responsible != null) {
					lblResponsibleText.setText(responsible.getLabel());
				} else {
					lblResponsibleText.setText("");
				}
				String machineUuid = stock.getDriverUuid();
				if (machineUuid != null && !machineUuid.isEmpty()) {
					String info = CoreHub.getStockCommissioningSystemService()
						.getInfoStringForDriver(UUID.fromString(machineUuid), false);
					lblMachineuuid.setText(info);
				} else {
					lblMachineuuid.setText("");
				}
			} else {
				lblOwnerText.setText("");
				lblResponsibleText.setText("");
			}
		});
		
		return container;
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		getPreferenceStore().setDefault(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES,
			Preferences.INVENTORY_CHECK_ILLEGAL_VALUES_DEFAULT);
	}
	
	@Override
	protected void performApply(){
		tableViewer.setInput(new Query<Stock>(Stock.class).execute());
		
		setErrorMessage(null);
		
		super.performApply();
	}
	
	@Override
	public boolean performOk(){
		getPreferenceStore().setValue(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES,
			btnChkStoreInvalidNumbers.getSelection());
		getPreferenceStore().setValue(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
			btnMachineOutlayPartialPackages.getSelection());
		
		((SettingsPreferenceStore) getPreferenceStore()).flush();
		
		return super.performOk();
	}
	
	protected DataBindingContext initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeTextTxtCodeObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtCode);
		IObservableValue stockDetailCodeObserveDetailValue =
			PojoProperties.value(Stock.class, "code", String.class).observeDetail(stockDetail);
		bindingContext.bindValue(observeTextTxtCodeObserveWidget, stockDetailCodeObserveDetailValue,
			null, null);
		//
		IObservableValue observeTextTxtDescriptionObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtDescription);
		IObservableValue stockDetailDescriptionObserveDetailValue = PojoProperties
			.value(Stock.class, "description", String.class).observeDetail(stockDetail);
		bindingContext.bindValue(observeTextTxtDescriptionObserveWidget,
			stockDetailDescriptionObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtLocationObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtLocation);
		IObservableValue stockDetailLocationObserveDetailValue =
			PojoProperties.value(Stock.class, "location", String.class).observeDetail(stockDetail);
		bindingContext.bindValue(observeTextTxtLocationObserveWidget,
			stockDetailLocationObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtPrioObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtPrio);
		IObservableValue stockDetailGlobalPreferenceObserveDetailValue =
			PojoProperties.value(Stock.class, "priority", Integer.class).observeDetail(stockDetail);
		bindingContext.bindValue(observeTextTxtPrioObserveWidget,
			stockDetailGlobalPreferenceObserveDetailValue, null, null);
		//
		IObservableValue observeTextTxtMachineConfigObserveWidget =
			WidgetProperties.text(SWT.Modify).observe(txtMachineConfig);
		IObservableValue stockDetailMachineConfigObserveDetailValue = PojoProperties
			.value(Stock.class, "driverConfig", String.class).observeDetail(stockDetail);
		bindingContext.bindValue(observeTextTxtMachineConfigObserveWidget,
			stockDetailMachineConfigObserveDetailValue, null, null);
		//
		return bindingContext;
	}
}
