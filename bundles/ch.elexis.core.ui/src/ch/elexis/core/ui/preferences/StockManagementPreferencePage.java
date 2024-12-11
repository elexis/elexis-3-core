package ch.elexis.core.ui.preferences;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.StockCommissioningServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.e4.providers.IdentifiableLabelProvider;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;

public class StockManagementPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Table tableStocks;
	private Text txtCode;
	private Text txtDescription;
	private Text txtLocation;
	private Text txtPrio;

	private Label lblOwnerText;
	private Label lblResponsibleText;

	private Button btnChkStoreInvalidNumbers;
	private Button btnIgnoreOrderedArticlesOnNextOrder;

	private WritableValue<IStock> stockDetail = new WritableValue<>(null, IStock.class);
	private TableViewer tableViewer;
	private Text txtMachineConfig;
	private Label lblMachineuuid;

	private Label lblDefaultArticleProvider;
	private Button btnMachineOutlayPartialPackages;
	private Button btnMachineStoreOnlyStockArticles;

	private Button btnStoreBelow;
	private Button btnStoreAtMin;

	private ComboViewer comboViewer;

	/**
	 * Create the preference page.
	 */
	public StockManagementPreferencePage() {
		setTitle(Messages.Core_Inventory_control);
	}

	/**
	 * Create contents of the preference page.
	 *
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
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
		tableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				IStock s1 = (IStock) e1;
				int s1I = s1.getPriority();
				IStock s2 = (IStock) e2;
				int s2I = s2.getPriority();
				return Integer.compare(s1I, s2I);
			}
		});
		tableViewer.addSelectionChangedListener(e -> {
			StructuredSelection ss = (StructuredSelection) e.getSelection();
			if (ss.isEmpty()) {
				stockDetail.setValue(null);
			} else {
				stockDetail.setValue((IStock) ss.getFirstElement());
			}
		});
		tableStocks = tableViewer.getTable();
		tableStocks.setHeaderVisible(true);
		tableStocks.setLinesVisible(true);

		Menu menu = new Menu(tableStocks);
		tableStocks.setMenu(menu);

		MenuItem mntmAddStock = new MenuItem(menu, SWT.NONE);
		mntmAddStock.setText(Messages.Core_Add);
		mntmAddStock.setImage(Images.IMG_NEW.getImage());
		mntmAddStock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStock stock = CoreModelServiceHolder.get().create(IStock.class);
				stock.setCode(StringUtils.EMPTY);
				stock.setPriority(Integer.MAX_VALUE);
				CoreModelServiceHolder.get().save(stock);
				tableViewer.add(stock);
				tableViewer.setSelection(new StructuredSelection(stock));
			}
		});

		MenuItem mntmRemoveStock = new MenuItem(menu, SWT.NONE);
		mntmRemoveStock.setText(Messages.Core_Remove);
		mntmRemoveStock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStock stock = stockDetail.getValue();
				if (stock != null) {
					boolean ret = MessageDialog.openQuestion(UiDesk.getTopShell(),
							Messages.StockManagementPreferencePage_delete_stock,
							Messages.StockManagementPreferencePage_delete_stock_dsc);
					if (ret) {
						List<IStockEntry> entries = StockServiceHolder.get().findAllStockEntriesForStock(stock);
						for (IStockEntry entry : entries) {
							entry.setDeleted(true);
						}
						CoreModelServiceHolder.get().save(entries);
						stock.setDeleted(true);
						CoreModelServiceHolder.get().save(stock);
						tableViewer.remove(stock);
					}
				}
			}
		});

		TableViewerColumn tvcPriority = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmPriority = tvcPriority.getColumn();
		tcl_composite.setColumnData(tblclmPriority, new ColumnPixelData(30, true, true));
		tblclmPriority.setText(Messages.Core_Priority);
		tvcPriority.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				IStock s = (IStock) cell.getElement();
				if (s == null)
					return;
				cell.setText(Integer.toString(s.getPriority()));
			}
		});

		TableViewerColumn tvcCode = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnCode = tvcCode.getColumn();
		tcl_composite.setColumnData(tblclmnCode, new ColumnPixelData(40));
		tblclmnCode.setText(Messages.Core_Code);
		tvcCode.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				IStock s = (IStock) cell.getElement();
				if (s == null)
					return;
				cell.setText(s.getCode());
			}
		});

		TableViewerColumn tvcDescription = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDescription = tvcDescription.getColumn();
		tcl_composite.setColumnData(tblclmnDescription, new ColumnWeightData(50));
		tblclmnDescription.setText(Messages.Core_Description);
		tvcDescription.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				IStock s = (IStock) cell.getElement();
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
			public void update(ViewerCell cell) {
				IStock s = (IStock) cell.getElement();
				if (s != null) {
					IPerson owner = s.getOwner();
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
		lblPrio.setText(Messages.Core_Priority);

		txtPrio = new Text(compositeDetail, SWT.BORDER);
		GridData gd_txtPrio = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtPrio.widthHint = 150;
		txtPrio.setLayoutData(gd_txtPrio);

		Label lblCode = new Label(compositeDetail, SWT.NONE);
		lblCode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCode.setToolTipText(Messages.StockManagementPreferencePage_lblCode_toolTipText);
		lblCode.setText(Messages.Core_Code);

		txtCode = new Text(compositeDetail, SWT.BORDER);
		GridData gd_txtCode = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtCode.widthHint = 100;
		txtCode.setLayoutData(gd_txtCode);
		txtCode.setTextLimit(3);

		Label lblDescription = new Label(compositeDetail, SWT.NONE);
		lblDescription.setText(Messages.Core_Description);

		txtDescription = new Text(compositeDetail, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblLocation = new Label(compositeDetail, SWT.NONE);
		lblLocation.setToolTipText(Messages.StockManagementPreferencePage_lblLocation_toolTipText);
		lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLocation.setText(Messages.Core_City);

		txtLocation = new Text(compositeDetail, SWT.BORDER);
		txtLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Link lblOwner = new Link(compositeDetail, SWT.NONE);
		lblOwner.setToolTipText(Messages.StockManagementPreferencePage_lblOwner_toolTipText);
		lblOwner.setText(Messages.StockManagementPreferencePage_lblOwner_text);
		lblOwner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStock s = stockDetail.getValue();
				if (s == null)
					return;
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Mandant.class,
						ch.elexis.core.l10n.Messages.Core_Select_Mandator,
						Messages.StockManagementPreference_select_stock_owner, new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Mandant p = (Mandant) ks.getSelection();
					s.setOwner(p.toIContact().asIPerson());
					String label = (p != null) ? p.getLabel() : StringUtils.EMPTY;
					lblOwnerText.setText(label);
				} else {
					s.setOwner(null);
					lblOwnerText.setText(StringConstants.EMPTY);
				}
				tableViewer.update(s, null);
				CoreModelServiceHolder.get().save(s);
			}
		});

		lblOwnerText = new Label(compositeDetail, SWT.NONE);
		lblOwnerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Link lblResponsible = new Link(compositeDetail, SWT.NONE);
		lblResponsible.setToolTipText(Messages.StockManagementPreferencePage_lblResponsible_toolTipText);
		lblResponsible.setText(Messages.StockManagementPreferencePage_lblResonsible_text);
		lblResponsible.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStock s = stockDetail.getValue();
				if (s == null)
					return;
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Kontakt.class,
						Messages.StockManagementPreference_select_stock_owner,
						Messages.StockManagementPreference_select_stock_owner_dsc,
						new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Kontakt p = (Kontakt) ks.getSelection();
					s.setResponsible(p.toIContact());
					String label = (p != null) ? p.getLabel() : StringUtils.EMPTY;
					lblResponsibleText.setText(label);
				} else {
					s.setResponsible(null);
					lblResponsibleText.setText(StringUtils.EMPTY);
				}
				CoreModelServiceHolder.get().save(s);
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
			public void widgetSelected(SelectionEvent e) {
				IStock s = stockDetail.getValue();
				if (s == null) {
					return;
				}
				List<UUID> allDrivers = StockCommissioningServiceHolder.get().listAllAvailableDrivers();
				if (allDrivers.isEmpty()) {
					MessageDialog.openInformation(UiDesk.getTopShell(), Messages.StockManagementPreference_no_drivers,
							Messages.StockManagementPreference_no_drivers_dsc);
					return;
				}

				ListDialog ld = new ListDialog(UiDesk.getTopShell());
				ld.setTitle(Messages.StockManagementPreference_select_driver);
				ld.setMessage(Messages.StockManagementPreference_select_driver_dsc);
				ld.setContentProvider(ArrayContentProvider.getInstance());
				ld.setAddCancelButton(true);
				ld.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element) {
						return StockCommissioningServiceHolder.get().getInfoStringForDriver((UUID) element, true);
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
					lblMachineuuid.setText(StockCommissioningServiceHolder.get().getInfoStringForDriver(ics, false));
				} else {
					s.setDriverUuid(null);
					lblMachineuuid.setText(StringConstants.EMPTY);
				}
			}
		});

		lblMachineuuid = new Label(compositeDetail, SWT.NONE);
		lblMachineuuid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblMachineConfig = new Label(compositeDetail, SWT.NONE);
		lblMachineConfig.setText(Messages.Core_Configuration);
		txtMachineConfig = new Text(compositeDetail, SWT.BORDER);
		txtMachineConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		btnMachineOutlayPartialPackages = new Button(compositeDetail, SWT.CHECK);
		btnMachineOutlayPartialPackages.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnMachineOutlayPartialPackages
				.setText(Messages.StockManagementPreferencePage_btnMachineOutlayPartialPackages_text);
		boolean outlayPartialPackages = ConfigServiceHolder.getGlobal(
				Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
				Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT);
		btnMachineOutlayPartialPackages.setSelection(outlayPartialPackages);

		btnMachineStoreOnlyStockArticles = new Button(compositeDetail, SWT.CHECK);
		btnMachineStoreOnlyStockArticles.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		btnMachineStoreOnlyStockArticles
				.setText(Messages.StockManagementPreferencePage_btnStoreOnlyStockeArticles_text);
		boolean storeOnlyStockedArticles = ConfigServiceHolder.getGlobal(
				Preferences.INVENTORY_MACHINE_STORE_ONLY_STOCKED_ARTICLES,
				Preferences.INVENTORY_MACHINE_STORE_ONLY_STOCKED_ARTICLES_DEFAULT);
		btnMachineStoreOnlyStockArticles.setSelection(storeOnlyStockedArticles);

		btnIgnoreOrderedArticlesOnNextOrder = new Button(container, SWT.CHECK);
		btnIgnoreOrderedArticlesOnNextOrder.setText(Messages.LagerverwaltungPrefs_ignoreOrderedArticleOnNextOrder);
		btnIgnoreOrderedArticlesOnNextOrder.setSelection(getPreferenceStore()
				.getBoolean(Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER));

		btnChkStoreInvalidNumbers = new Button(container, SWT.CHECK);
		btnChkStoreInvalidNumbers.setText(Messages.LagerverwaltungPrefs_checkForInvalid);
		btnChkStoreInvalidNumbers
				.setSelection(getPreferenceStore().getBoolean(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES));

		Group group1 = new Group(container, SWT.SHADOW_IN);
		group1.setText(Messages.LagerverwaltungPrefs_orderCriteria);
		group1.setLayout(new RowLayout(SWT.VERTICAL));
		btnStoreBelow = new Button(group1, SWT.RADIO);
		btnStoreBelow.setText(Messages.LagerverwaltungPrefs_orderWhenBelowMi);
		btnStoreAtMin = new Button(group1, SWT.RADIO);
		btnStoreAtMin.setText(Messages.LagerverwaltungPrefs_orderWhenAtMin);

		int valInventoryOrderTrigger = ConfigServiceHolder.getGlobal(Preferences.INVENTORY_ORDER_TRIGGER,
				Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
		boolean isInventoryOrderEqualValue = Preferences.INVENTORY_ORDER_TRIGGER_EQUAL == valInventoryOrderTrigger;
		btnStoreAtMin.setSelection(isInventoryOrderEqualValue);
		btnStoreBelow.setSelection(!isInventoryOrderEqualValue);

		Composite compDefaultProvider = new Composite(container, SWT.NONE);
		compDefaultProvider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_compDefaultProvider = new GridLayout(3, false);
		gl_compDefaultProvider.marginWidth = 0;
		gl_compDefaultProvider.marginHeight = 0;
		compDefaultProvider.setLayout(gl_compDefaultProvider);

		Link linkDefaultArticleProvider = new Link(compDefaultProvider, SWT.NONE);
		linkDefaultArticleProvider
				.setToolTipText(Messages.StockManagementPreferencePage_linkDefaultArticleProvider_toolTipText);
		linkDefaultArticleProvider.setText(Messages.StockManagementPreferencePage_linkDefaultArticleProvider_text);
		linkDefaultArticleProvider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Kontakt.class,
						Messages.StockManagementPreference_select_stock_select_default_supplier,
						Messages.StockManagementPreference_select_stock_select_default_supplier_dsc, new String[] {});
				int ret = ks.open();
				if (ret == Window.OK) {
					Kontakt p = (Kontakt) ks.getSelection();
					if (p != null) {
						ConfigServiceHolder.get().set(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, p.getId());
						lblDefaultArticleProvider.setText(p.getLabel());
					}
				} else {
					ConfigServiceHolder.get().set(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
					lblDefaultArticleProvider.setText(StringUtils.EMPTY);
				}
			}
		});

		lblDefaultArticleProvider = new Label(compDefaultProvider, SWT.NONE);
		lblDefaultArticleProvider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		String id = ConfigServiceHolder.getGlobal(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
		lblDefaultArticleProvider.setText(StringUtils.EMPTY);
		new Label(compDefaultProvider, SWT.NONE);
		if (id != null) {
			Kontakt load = Kontakt.load(id);
			if (load.exists()) {
				lblDefaultArticleProvider.setText(load.getLabel());
			}
		}

		tableViewer.setInput(refresh());
		initDataBindings();

		stockDetail.addChangeListener(e -> {
			IStock stock = stockDetail.getValue();
			if (stock != null) {
				IPerson owner = stock.getOwner();
				if (owner != null) {
					lblOwnerText.setText(owner.getLabel());
				} else {
					lblOwnerText.setText(StringUtils.EMPTY);
				}
				IContact responsible = stock.getResponsible();
				if (responsible != null) {
					lblResponsibleText.setText(responsible.getLabel());
				} else {
					lblResponsibleText.setText(StringUtils.EMPTY);
				}
				String machineUuid = stock.getDriverUuid();
				if (machineUuid != null && !machineUuid.isEmpty()) {
					String info = StockCommissioningServiceHolder.get()
							.getInfoStringForDriver(UUID.fromString(machineUuid), false);
					lblMachineuuid.setText(info);
				} else {
					lblMachineuuid.setText(StringUtils.EMPTY);
				}
			} else {
				lblOwnerText.setText(StringUtils.EMPTY);
				lblResponsibleText.setText(StringUtils.EMPTY);
			}
		});

		Group grp_mediorder = new Group(container, SWT.NONE);
		grp_mediorder.setText(Messages.Mediorder);
		grp_mediorder.setLayout(new GridLayout(2, false));
		grp_mediorder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMediorderDefaultStock = new Label(grp_mediorder, SWT.NONE);
		lblMediorderDefaultStock.setText(Messages.Core_Stock);

		comboViewer = new ComboViewer(grp_mediorder, SWT.READ_ONLY);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(IdentifiableLabelProvider.getInstance());
		comboViewer.setInput(StockServiceHolder.get().getAllStocks(true, false));

		String value = ConfigServiceHolder.get().get(Preferences.INVENTORY_MEDIORDER_DEFAULT_STOCK, null);
		IStock stock = (value == null) ? StockServiceHolder.get().getDefaultStock()
				: CoreModelServiceHolder.get().load(value, IStock.class).orElse(null);
		comboViewer.setSelection(new StructuredSelection(stock));

		return container;
	}

	/**
	 * Initialize the preference page.
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ConfigServicePreferenceStore(Scope.GLOBAL));
		getPreferenceStore().setDefault(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES,
				Preferences.INVENTORY_CHECK_ILLEGAL_VALUES_DEFAULT);
		getPreferenceStore().setDefault(Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER_DEFAULT);
	}

	@Override
	protected void performApply() {
		tableViewer.setInput(refresh());
		setErrorMessage(null);
		super.performApply();
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES,
				btnChkStoreInvalidNumbers.getSelection());
		getPreferenceStore().setValue(Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
				btnIgnoreOrderedArticlesOnNextOrder.getSelection());
		getPreferenceStore().setValue(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES,
				btnMachineOutlayPartialPackages.getSelection());
		getPreferenceStore().setValue(Preferences.INVENTORY_MACHINE_STORE_ONLY_STOCKED_ARTICLES,
				btnMachineStoreOnlyStockArticles.getSelection());
		getPreferenceStore().setValue(Preferences.INVENTORY_ORDER_TRIGGER,
				btnStoreBelow.getSelection() ? Preferences.INVENTORY_ORDER_TRIGGER_BELOW
						: Preferences.INVENTORY_ORDER_TRIGGER_EQUAL);
		getPreferenceStore().setValue(Preferences.INVENTORY_MEDIORDER_DEFAULT_STOCK,
				CoreModelServiceHolder.get()
						.load(((IStock) comboViewer.getStructuredSelection().getFirstElement()).getId(), IStock.class)
						.get().getId());

		return super.performOk();
	}

	private List<IStock> refresh() {
		return StockServiceHolder.get().getAllStocks(true, false);
	}

	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		IChangeListener saveStockListener = event -> {
			IStock stock = stockDetail.getValue();
			if (stock != null) {
				CoreModelServiceHolder.get().save(stock);
			}
		};

		IObservableValue<?> observeTextTxtCodeObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtCode);
		IObservableValue<String> stockDetailCodeObserveDetailValue = PojoProperties
				.value(IStock.class, "code", String.class) //$NON-NLS-1$
				.observeDetail(stockDetail);
		bindingContext.bindValue(observeTextTxtCodeObserveWidget, stockDetailCodeObserveDetailValue, null, null);
		stockDetailCodeObserveDetailValue.addChangeListener(saveStockListener);

		IObservableValue<?> observeTextTxtDescriptionObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtDescription);
		IObservableValue<String> stockDetailDescriptionObserveDetailValue = PojoProperties
				.value(IStock.class, "description", String.class).observeDetail(stockDetail); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtDescriptionObserveWidget, stockDetailDescriptionObserveDetailValue, null,
				null);
		stockDetailDescriptionObserveDetailValue.addChangeListener(saveStockListener);

		IObservableValue<?> observeTextTxtLocationObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtLocation);
		IObservableValue<String> stockDetailLocationObserveDetailValue = PojoProperties
				.value(IStock.class, "location", String.class).observeDetail(stockDetail); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtLocationObserveWidget, stockDetailLocationObserveDetailValue, null,
				null);
		stockDetailLocationObserveDetailValue.addChangeListener(saveStockListener);

		IObservableValue<?> observeTextTxtPrioObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtPrio);
		IObservableValue<Integer> stockDetailGlobalPreferenceObserveDetailValue = PojoProperties
				.value(IStock.class, "priority", Integer.class).observeDetail(stockDetail); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtPrioObserveWidget, stockDetailGlobalPreferenceObserveDetailValue, null,
				null);
		stockDetailGlobalPreferenceObserveDetailValue.addChangeListener(saveStockListener);

		IObservableValue<?> observeTextTxtMachineConfigObserveWidget = WidgetProperties.text(SWT.Modify)
				.observe(txtMachineConfig);
		IObservableValue<String> stockDetailMachineConfigObserveDetailValue = PojoProperties
				.value(IStock.class, "driverConfig", String.class).observeDetail(stockDetail); //$NON-NLS-1$
		bindingContext.bindValue(observeTextTxtMachineConfigObserveWidget, stockDetailMachineConfigObserveDetailValue,
				null, null);
		stockDetailMachineConfigObserveDetailValue.addChangeListener(saveStockListener);

		return bindingContext;
	}
}
