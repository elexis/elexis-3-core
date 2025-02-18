package ch.elexis.core.ui.views.controls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.editors.ContactSelectionDialogCellEditor;
import jakarta.inject.Inject;

public class StockDetailComposite extends Composite {

	private WritableValue<IArticle> wvArtikel = new WritableValue<>(null, IArticle.class);

	private Logger log = LoggerFactory.getLogger(StockDetailComposite.class);

	private Table table;

	private Map<IStock, IStockEntry> stockEntries = new HashMap<>();
	private CheckboxTableViewer checkboxTableViewer;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 */
	public StockDetailComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(this, SWT.NONE);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		checkboxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);
		checkboxTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setEnabled(false);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				if (stock == null) {
					return null;
				}
				return stock.getCode();
			}
		});
		TableColumn tblclmnStock = tableViewerColumn.getColumn();
		tblclmnStock.setResizable(true);
		tblclmnStock.setAlignment(SWT.CENTER);
		tcl_composite.setColumnData(tblclmnStock, new ColumnPixelData(100, false, false));
		tblclmnStock.setText("Verfügbar in Lager");

		TableViewerColumn tvcMin = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		StockEntryEditingSupport sees = new StockEntryEditingSupport(checkboxTableViewer, StockEntryEditingSupport.MIN);
		tvcMin.setEditingSupport(sees);
		tvcMin.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return null;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se != null) {
					return Integer.toString(se.getMinimumStock());
				}
				return null;
			}
		});
		TableColumn tblclmnMin = tvcMin.getColumn();
		tblclmnMin.setResizable(true);
		tblclmnMin.setAlignment(SWT.CENTER);
		tcl_composite.setColumnData(tblclmnMin, new ColumnPixelData(30, false, true));
		tblclmnMin.setText("Min");

		TableViewerColumn tvcIst = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		StockEntryEditingSupport seesIst = new StockEntryEditingSupport(checkboxTableViewer,
				StockEntryEditingSupport.CURR);
		tvcIst.setEditingSupport(seesIst);
		tvcIst.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return null;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se != null) {
					return Integer.toString(se.getCurrentStock());
				}
				return null;
			}
		});
		TableColumn tblclmnNewColumn = tvcIst.getColumn();
		tblclmnNewColumn.setResizable(true);
		tcl_composite.setColumnData(tblclmnNewColumn, new ColumnPixelData(30, false, true));
		tblclmnNewColumn.setText("Ist");

		TableViewerColumn tvcMax = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		StockEntryEditingSupport seesMax = new StockEntryEditingSupport(checkboxTableViewer,
				StockEntryEditingSupport.MAX);
		tvcMax.setEditingSupport(seesMax);
		tvcMax.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return null;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se != null) {
					return Integer.toString(se.getMaximumStock());
				}
				return null;
			}
		});
		TableColumn tblclmnMax = tvcMax.getColumn();
		tblclmnMax.setResizable(true);
		tcl_composite.setColumnData(tblclmnMax, new ColumnPixelData(30, true, true));
		tblclmnMax.setText("Max");

		TableViewerColumn tvcFraction = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		StockEntryEditingSupport seesFrac = new StockEntryEditingSupport(checkboxTableViewer,
				StockEntryEditingSupport.FRAC);
		tvcFraction.setEditingSupport(seesFrac);
		tvcFraction.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return null;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se != null) {
					return Integer.toString(se.getFractionUnits());
				}
				return null;
			}
		});
		TableColumn tblclmnFraction = tvcFraction.getColumn();
		tblclmnFraction.setResizable(true);
		tcl_composite.setColumnData(tblclmnFraction, new ColumnPixelData(30, false, true));
		tblclmnFraction.setText("Anbruch");

		TableViewerColumn tvcProvider = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		tvcProvider.setEditingSupport(new EditingSupport(checkboxTableViewer) {
			@Override
			protected void setValue(Object element, Object value) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se == null) {
					return;
				}
				se.setProvider((IContact) value);
				CoreModelServiceHolder.get().save(se);
				getViewer().refresh();
			}

			@Override
			protected Object getValue(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return null;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se == null) {
					return null;
				}
				return se.getProvider();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ContactSelectionDialogCellEditor(((CheckboxTableViewer) getViewer()).getTable(),
						"Lieferant auswählen", "Bitte selektieren Sie den Lieferant");
			}

			@Override
			protected boolean canEdit(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return false;
				}
				return (stockEntries.get(stock) != null);
			}
		});
		tvcProvider.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				IArticle art = wvArtikel.getValue();
				if (stock == null || art == null) {
					return null;
				}
				IStockEntry se = stockEntries.get(stock);
				if (se == null) {
					return null;
				}
				IContact provider = se.getProvider();
				if (provider == null) {
					return null;
				}
				return provider.getLabel();
			}
		});
		TableColumn tblclmnProvider = tvcProvider.getColumn();
		tcl_composite.setColumnData(tblclmnProvider, new ColumnPixelData(200, true, true));
		tblclmnProvider.setText("Lieferant");

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (wvArtikel.getValue() instanceof IArticle) {
					IStock stock = (IStock) event.getElement();
					IArticle art = wvArtikel.getValue();
					if (stock != null && art != null) {
						boolean blockModification = stock.isCommissioningSystem() && !ConfigServiceHolder.getGlobal(
								Preferences.INVENTORY_MACHINE_STORE_ONLY_STOCKED_ARTICLES,
								Preferences.INVENTORY_MACHINE_STORE_ONLY_STOCKED_ARTICLES_DEFAULT);
						if (!blockModification) {
							if (event.getChecked()) {
								StockServiceHolder.get().storeArticleInStock(stock,
										StoreToStringServiceHolder.getStoreToString(art));
							} else {
								StockServiceHolder.get().unstoreArticleFromStock(stock,
										StoreToStringServiceHolder.getStoreToString(art));
							}
						}
					}
				}
				refreshData();
			}
		});

		checkboxTableViewer.setCheckStateProvider(new ICheckStateProvider() {

			@Override
			public boolean isGrayed(Object element) {
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				IStock stock = (IStock) element;
				if (stock == null || wvArtikel.getValue() == null) {
					return false;
				}
				return (stockEntries.get(stock) != null);
			}
		});

		List<IStock> stocks = StockServiceHolder.get().getAllStocks(true, false);
		for (IStock stock : stocks) {
			stockEntries.put(stock, null);
		}

		checkboxTableViewer.setInput(StockServiceHolder.get().getAllStocks(true, false));

		TableViewer ret = new TableViewer(table);
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(ret,
				new FocusCellHighlighter(ret) {
				});
		ColumnViewerEditorActivationStrategy editorActivationStrategy = new ColumnViewerEditorActivationStrategy(ret) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				ViewerCell cell = (ViewerCell) event.getSource();
				return cell.getColumnIndex() > 0 && cell.getColumnIndex() < 5;
			}
		};
		TableViewerEditor.create(ret, focusCellManager, editorActivationStrategy, TableViewerEditor.TABBING_HORIZONTAL);

		CoreUiUtil.injectServices(this);
	}

	@Optional
	@Inject
	public void udpate(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IStockEntry entry) {
		if (entry != null) {
			if (checkboxTableViewer != null && stockEntries != null && !checkboxTableViewer.getControl().isDisposed()) {
				for (IStock key : stockEntries.keySet()) {
					if (stockEntries.get(key) != null && stockEntries.get(key).equals(entry)) {
						refreshData();
					}
				}
			}
		}
	}

	private void refreshData() {
		boolean enabled = false;
		String articleString = wvArtikel.getValue() != null
				? StoreToStringServiceHolder.getStoreToString(wvArtikel.getValue())
				: null;
		if (wvArtikel.getValue() instanceof IArticle) {
			enabled = (!wvArtikel.getValue().isProduct());
		}

		table.setEnabled(enabled);
		stockEntries.replaceAll((k, v) -> null);
		if (articleString != null) {
			stockEntries.keySet().forEach(k -> {
				stockEntries.put(k, StockServiceHolder.get().findStockEntryForArticleInStock(k, articleString));
			});
		}
		checkboxTableViewer.refresh(true);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setArticle(IArticle article) {
		wvArtikel.setValue(article);
		refreshData();
	}

	public class StockEntryEditingSupport extends EditingSupport {

		public static final int MIN = 0;
		public static final int CURR = 1;
		public static final int MAX = 2;
		public static final int FRAC = 3;

		private final int editorFor;
		private final CellEditor editor;

		public StockEntryEditingSupport(CheckboxTableViewer columnViewer, int editorFor) {
			super(columnViewer);
			this.editorFor = editorFor;
			this.editor = new TextCellEditor(columnViewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			IStock stock = (IStock) element;
			if (stock == null || wvArtikel.getValue() == null || (stockEntries.get(stock) == null)) {
				return false;
			}

			if ((CURR == editorFor || FRAC == editorFor) && stock.isCommissioningSystem()) {
				// the current stock and fraction count of a stock commissioning system
				// must not be directly changed
				return false;
			}

			return true;
		}

		@Override
		protected Object getValue(Object element) {
			IStock stock = (IStock) element;
			if (stock == null || wvArtikel.getValue() == null) {
				return StringUtils.EMPTY;
			}
			IStockEntry se = stockEntries.get(stock);
			if (se == null) {
				return StringUtils.EMPTY;
			}

			int value = 0;
			switch (editorFor) {
			case MIN:
				value = se.getMinimumStock();
				break;
			case CURR:
				value = se.getCurrentStock();
				break;
			case MAX:
				value = se.getMaximumStock();
				break;
			case FRAC:
				value = se.getFractionUnits();
				break;
			default:
			}
			return Integer.toString(value);
		}

		@Override
		protected void setValue(Object element, Object value) {
			IStock stock = (IStock) element;
			if (stock == null || wvArtikel.getValue() == null) {
				return;
			}
			IStockEntry stockEntry = stockEntries.get(stock);
			if (stockEntry == null) {
				return;
			}

			LockResponse lr = LocalLockServiceHolder.get().acquireLock(stockEntry);
			if (!lr.isOk()) {
				return;
			}

			int val = 0;
			try {
				val = Integer.valueOf((String) value);
			} catch (NumberFormatException nfe) {
			}

			switch (editorFor) {
			case MIN:
				stockEntry.setMinimumStock(val);
				break;
			case MAX:
				stockEntry.setMaximumStock(val);
				break;
			case CURR:
				stockEntry.setCurrentStock(val);
				break;
			case FRAC:
				stockEntry.setFractionUnits(val);
				break;
			default:
			}
			CoreModelServiceHolder.get().save(stockEntry);
			lr = LocalLockServiceHolder.get().releaseLock(stockEntry);
			if (!lr.isOk()) {
				log.warn("Error releasing lock for [{}]: {}", stockEntry.getId(), lr.getStatus()); //$NON-NLS-1$
			}
			getViewer().refresh();
		}

	}
}
