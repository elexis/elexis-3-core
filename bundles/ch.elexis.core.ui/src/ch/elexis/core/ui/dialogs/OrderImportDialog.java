/*******************************************************************************
 * Copyright (c) 2005-2023, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    MEDEVIT - refactoring after multi-stock adaptations
 *******************************************************************************/
package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.actions.ScannerEvents;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.text.ElexisText;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.BestellungEntry;

/*
 * @author Daniel Lutz
 *
 */
public class OrderImportDialog extends TitleAreaDialog {

	private static final String ALLE_MARKIEREN = " Alle markieren ";
	private static final int DIFF_SPINNER_MIN = 1;
	private static final int DIFF_SPINNER_DEFAULT = 1;

	public static int ACTION_MODE_REGISTER = 0; // Einbuchungsmodus
	public static int ACTION_MODE_INVENTORY = 1; // Inventurmodus

	@Inject
	private IContextService contextService;

	private String previousBarcodeInputConsumer;

	/**
	 * Order to act on
	 */
	private List<OrderElement> orderElements;
	private IOrder order;

	private Spinner diffSpinner;
	private ElexisText eanText;

	private TableViewer viewer;

	private Color verifiedColor;
	private Font boldFont;

	private int actionMode;

	private TableColumnLayout tcLayout;

	/**
	 * @wbp.parser.constructor
	 */
	public OrderImportDialog(Shell parentShell, IOrder order) {
		this(parentShell, order, 0);
	}

	public OrderImportDialog(Shell parentShell, IOrder order, int actionMode) {
		super(parentShell);
		this.order = order;
		this.actionMode = actionMode;
		setShellStyle(getShellStyle() | SWT.SHELL_TRIM);

		CoreUiUtil.injectServices(this);
		previousBarcodeInputConsumer = (String) contextService.getNamed("barcodeInputConsumer").orElse(null);
		contextService.getRootContext().setNamed("barcodeInputConsumer", OrderImportDialog.class.getName());

		orderElements = new ArrayList<>();
		List<IOrderEntry> items = order != null ? order.getEntries() : Collections.emptyList();
		for (IOrderEntry entry : items) {
			// only show entries which are not done
			if (entry.getState() != OrderEntryState.DONE) {
				IStock stock = entry.getStock();
				if (stock != null) {
					IStockEntry stockEntry = StockServiceHolder.get().findStockEntryForArticleInStock(stock,
							StoreToStringServiceHolder.getStoreToString(entry.getArticle()));
					if (stockEntry != null) {
						OrderElement orderElement = new OrderElement(entry, stockEntry, entry.getAmount());
						orderElements.add(orderElement);
					}
				} else {
					// check if a stock entry was created since the order was created
					IStockEntry stockEntry = StockServiceHolder.get().findPreferredStockEntryForArticle(
							StoreToStringServiceHolder.getStoreToString(entry.getArticle()),
							ElexisEventDispatcher.getSelectedMandator().getId());
					if (stockEntry != null) {
						OrderElement orderElement = new OrderElement(entry, stockEntry, entry.getAmount());
						orderElements.add(orderElement);
					} else {
						IStockEntry transienStockEntry = new TransientStockEntry(entry.getArticle());
						OrderElement orderElement = new OrderElement(entry, transienStockEntry, entry.getAmount());
						orderElements.add(orderElement);
					}
				}
			}
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(700, 500);
	};

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite mainArea = new Composite(parent, SWT.NONE);
		mainArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		mainArea.setLayout(new GridLayout());

		Composite scannerArea = new Composite(mainArea, SWT.NONE);
		scannerArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		scannerArea.setLayout(new GridLayout());

		Group scannerGroup = new Group(scannerArea, SWT.NONE);
		scannerGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		scannerGroup.setLayout(new GridLayout(4, false));
		scannerGroup.setText("Einlesen mit Barcode-Scanner");

		diffSpinner = new Spinner(scannerGroup, SWT.NONE);
		diffSpinner.setMinimum(DIFF_SPINNER_MIN);
		diffSpinner.setSelection(DIFF_SPINNER_DEFAULT);

		Label eanLabel = new Label(scannerGroup, SWT.NONE);
		eanLabel.setText("EAN:"); //$NON-NLS-1$
		eanText = new ElexisText(scannerGroup, SWT.NONE);
		eanText.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		eanText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					applyScanner();
				}
			}
		});

		Button button = new Button(scannerGroup, SWT.PUSH);
		button.setText("Übernehmen");
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				applyScanner();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
		});

		Composite tableArea = new Composite(mainArea, SWT.NONE);
		tableArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tableArea.setLayout(new GridLayout());

		viewer = new TableViewer(tableArea, SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tcLayout = new TableColumnLayout();
		tableArea.setLayout(tcLayout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		verifiedColor = table.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
		boldFont = createBoldFont(table.getFont());

		final TableViewerFocusCellManager mgr = new TableViewerFocusCellManager(viewer,
				new FocusCellOwnerDrawHighlighter(viewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
								&& (event.keyCode == SWT.CR || event.character == ' '))
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(viewer, mgr, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		createViewerColumns();

		viewer.setContentProvider(new ViewerContentProvider());
		viewer.setInput(this);
		if (actionMode == ACTION_MODE_REGISTER) {
			// in inventory mode we want to stay with the scan order
			viewer.setComparator(new ViewerComparator() {
				public int compare(Viewer viewer, Object e1, Object e2) {
					IArticle a1 = ((OrderElement) e1).getArticle();
					IArticle a2 = ((OrderElement) e2).getArticle();

					if (a1 != null && a2 != null) {
						return a1.getName().compareTo(a2.getName());
					}
					return 0;
				};
			});
		}

		Composite cButtons = new Composite(mainArea, SWT.NONE);
		cButtons.setLayout(new GridLayout(2, false));
		final Button clickAllButton = new Button(cButtons, SWT.PUSH);
		clickAllButton.setText(ALLE_MARKIEREN);
		clickAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean bv = true;
				if (clickAllButton.getText().equals(ALLE_MARKIEREN)) {
					bv = true;
					clickAllButton.setText("Alle demarkieren");
				} else {
					bv = false;
					clickAllButton.setText(ALLE_MARKIEREN);
				}

				for (OrderElement oe : orderElements) {
					oe.setVerified(bv);
				}
				viewer.refresh(true);
			}

		});
		Button importButton = new Button(cButtons, SWT.PUSH);
		importButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		importButton.setText("Lagerbestände markierte anpassen");
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (actionMode == ACTION_MODE_REGISTER) {
					doImport();
				} else if (actionMode == ACTION_MODE_INVENTORY) {
					doFixInventory();
				}
			}
		});
		cButtons.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return mainArea;
	}

	private Font createBoldFont(Font baseFont) {
		FontData fd = baseFont.getFontData()[0];
		Font font = new Font(baseFont.getDevice(), fd.getName(), fd.getHeight(), fd.getStyle() | SWT.BOLD);
		return font;
	}

	private void createViewerColumns() {
		TableViewerColumn column;

		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(viewer.getTable());
		final TextCellEditor textCellEditor = new TextCellEditor(viewer.getTable());

		/* OK (checkbox column) */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.Core_ok);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(50, true, true));
		column.setLabelProvider(new CheckboxLabelProvider());
		column.setEditingSupport(new EditingSupport(viewer) {
			public boolean canEdit(Object element) {
				return true;
			}

			public CellEditor getCellEditor(Object element) {
				return checkboxCellEditor;
			}

			public Object getValue(Object element) {
				if (element instanceof OrderElement) {
					OrderElement orderElement = (OrderElement) element;
					return Boolean.valueOf(orderElement.isVerified());
				} else {
					return null;
				}
			}

			public void setValue(Object element, Object value) {
				if (element instanceof OrderElement) {
					OrderElement orderElement = (OrderElement) element;
					if (value instanceof Boolean) {
						Boolean bValue = (Boolean) value;
						orderElement.setVerified(bValue.booleanValue());
					}
					viewer.update(orderElement, null);
				}
			}
		});

		/* Amount delivered */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.BestellView_delivered);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(60, true, true));
		column.setLabelProvider(new AmountLabelProvider());
		column.setEditingSupport(new EditingSupport(viewer) {
			public boolean canEdit(Object element) {
				return true;
			}

			public CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			public Object getValue(Object element) {
				if (element instanceof OrderElement) {
					OrderElement orderElement = (OrderElement) element;
					return orderElement.getAmountAsString();
				} else {
					return null;
				}
			}

			public void setValue(Object element, Object value) {
				if (element instanceof OrderElement) {
					OrderElement orderElement = (OrderElement) element;
					if (value instanceof String) {
						String text = (String) value;
						try {
							int amount = Integer.parseInt(text);
							orderElement.setAmount(amount);
							orderElement.setVerified(true);
						} catch (NumberFormatException ex) {
							// ignore invalid value
						}
					}
					viewer.update(orderElement, null);
				}
			}
		});

		/* Amount on stock */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.BestellView_inventory);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(60, true, true));
		column.setLabelProvider(new StockLabelProvider());

		/* Pharamcode */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.Core_Phamacode);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(70, true, true));
		column.setLabelProvider(new PharamcodeLabelProvider());

		/* EAN */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.Core_EAN);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(70, true, true));
		column.setLabelProvider(new EANLabelProvider());

		/* Description */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.UI_description);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(200, true, true));
		column.setLabelProvider(new DescriptionLabelProvider());

		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(Messages.Core_Stock);
		tcLayout.setColumnData(column.getColumn(), new ColumnPixelData(100, true, true));
		column.setLabelProvider(new StockNameLabelProvider());

	}

	@Override
	public void create() {
		super.create();

		if (actionMode == ACTION_MODE_REGISTER) {
			if (order != null) {
				setTitle("Bestellung " + order.getLabel() + " im Lager einbuchen");
			} else {
				setTitle("Bestellung im Lager einbuchen");
			}

			setMessage("Bitte überprüfen Sie alle bestellten Artikel. Überprüfte Artikel werden grün angezeigt."
					+ " Bei der Anpassung der Lagerbestände werden nur jene Artikel"
					+ " berücksichtigt, bei denen unter \"OK\" ein Haken gesetzt ist.");

			getShell().setText("Bestellung im Lager einbuchen");

		} else if (actionMode == ACTION_MODE_INVENTORY) {
			setTitle("Inventurmodus");
			getShell().setText("Inventurmodus");
			setMessage(
					"Scannen Sie die Medikamente zur Inventur. Mehrfach-Scans möglich."
							+ " Gescannte Medikamente werden unten angezeigt und können als IST-Bestand übernommen werden.");
		}
	}

	// Replace OK/Cancel buttons by a close button
	protected void createButtonsForButtonBar(Composite parent) {
		// Create Close button
		createButton(parent, IDialogConstants.OK_ID, "Schliessen", false);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	boolean subscribed = true;

	@Override
	public boolean close() {
		contextService.getRootContext().setNamed("barcodeInputConsumer", previousBarcodeInputConsumer);
		CoreUiUtil.uninjectServices(this);
		return super.close();
	}

	// update the table according to the input from the scanner
	private void applyScanner() {
		int diff = diffSpinner.getSelection();
		String ean = eanText.getText().trim();
		applyScanner(ean, diff, null);
	}

	private void applyScanner(String gtin, int diff, IArticle article) {
		gtin = gtin.replaceAll(Character.valueOf(SWT.CR).toString(), StringUtils.EMPTY);
		gtin = gtin.replaceAll(Character.valueOf(SWT.LF).toString(), StringUtils.EMPTY);
		gtin = gtin.replaceAll(Character.valueOf((char) 0).toString(), StringUtils.EMPTY);

		eanText.setText(StringUtils.EMPTY);
		diffSpinner.setSelection(DIFF_SPINNER_DEFAULT);

		OrderElement orderElement = findOrderElementByEAN(gtin);
		if (orderElement != null) {
			int newAmount = orderElement.getAmount() + diff;
			updateOrderElement(orderElement, newAmount);
		} else {
			if (actionMode == ACTION_MODE_INVENTORY) {
				String mandatorId = ContextServiceHolder.get().getActiveMandator().get().getId();
				IStock mandatorDefaultStock = StockServiceHolder.get().getMandatorDefaultStock(mandatorId);
				IStockEntry transientStockEntry = new TransientStockEntry(article, mandatorDefaultStock);
				OrderElement _orderElement = new OrderElement(TransientOrderEntry.getInstance(), transientStockEntry,
						diff);
				orderElements.add(_orderElement);
			} else {
				ScannerEvents.beep();
				SWTHelper.alert("Artikel nicht bestellt",
						"Dieser Artikel wurde nicht bestellt. Der Bestand kann nicht automatisch angepasst werden.");
			}
		}
		viewer.refresh();
	}

	@Inject
	public void barcodeEvent(@org.eclipse.e4.core.di.annotations.Optional @UIEventTopic(ElexisEventTopics.BASE_EVENT
			+ "barcodeinput") Object object, IContextService contextService) {
		if (object instanceof IArticle && StringUtils.equals(OrderImportDialog.class.getName(),
				(String) contextService.getNamed("barcodeInputConsumer").orElse(null))) {
			IArticle article = ((IArticle) object);
			applyScanner(article.getGtin(), 1, article);
		}
	}

	private OrderElement findOrderElementByEAN(String ean) {
		if (ean == null) {
			return null;
		}

		for (OrderElement orderElement : orderElements) {
			if (orderElement.getArticle().getGtin().equals(ean)) {
				return orderElement;
			}
		}

		// not found
		return null;
	}

	private void updateOrderElement(OrderElement orderElement, int newAmount) {
		orderElement.setAmount(newAmount);
		if (ACTION_MODE_INVENTORY != actionMode) {
			orderElement.setVerified(true);
		}
		viewer.update(orderElement, null);
	}

	private void doFixInventory() {
		for (OrderElement orderElement : orderElements) {
			if (orderElement.isVerified()) {
				IStock stock = orderElement.getStockEntry().getStock();
				IStockEntry realEntry = StockServiceHolder.get().findStockEntryForArticleInStock(stock,
						orderElement.getStockEntry().getArticle());
				if (realEntry != null) {
					LockResponse lockResponse = LocalLockServiceHolder.get().acquireLockBlocking(realEntry, 1,
							new NullProgressMonitor());
					if (lockResponse.isOk()) {
						realEntry.setCurrentStock(orderElement.getAmount());
						orderElement.setVerified(false);
						CoreModelServiceHolder.get().save(realEntry);
						LocalLockServiceHolder.get().releaseLock(lockResponse.getLockInfo());
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, realEntry);
					} else {
						throw new IllegalStateException(
								"Could not acquire lock for stockEntry [" + realEntry.getArticle().getLabel() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					}

				}
			}
		}
		viewer.refresh();
	}

	// read in verified order
	public void doImport() {
		try {
			for (OrderElement orderElement : orderElements) {
				if (orderElement.isVerified()) {
					IStockEntry stockEntry = orderElement.getStockEntry();
					if (stockEntry instanceof TransientStockEntry) {
						// create a non transient stock entry for the article
						stockEntry = ((TransientStockEntry) stockEntry).create(orderElement);
					}
					LockResponse lockResponse = LocalLockServiceHolder.get().acquireLockBlocking(stockEntry, 1,
							new NullProgressMonitor());
					if (lockResponse.isOk()) {
						int diff = orderElement.getAmount();
						int oldAmount = stockEntry.getCurrentStock();
						int newAmount = oldAmount + diff;
						stockEntry.setCurrentStock(newAmount);

						// reset amount
						orderElement.setAmount(0);
						orderElement.setVerified(false);

						if (diff > 0) {
							// always close partial on second import ... TODO add a partial field for
							// counting
							if (orderElement.getOrderState() == OrderEntryState.PARTIAL_DELIVER) {
								orderElement.setOrderState(BestellungEntry.STATE_DONE);
							} else if (diff >= orderElement.getOrderAmount()) {
								orderElement.setOrderState(BestellungEntry.STATE_DONE);
							} else if (diff < orderElement.getOrderAmount()) {
								orderElement.setOrderState(BestellungEntry.STATE_PARTIAL_DELIVER);
							}
						}
						CoreModelServiceHolder.get().save(Arrays.asList(stockEntry, orderElement.getOrderEntry()));
						LocalLockServiceHolder.get().releaseLock(lockResponse.getLockInfo());
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, stockEntry);
					} else {
						throw new IllegalStateException(
								"Could not acquire lock for stockEntry [" + stockEntry.getArticle().getLabel() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		} catch (Exception ex) {
			SWTHelper.showError("Fehler bei Anpassung der Bestände",
					"Bestände konnten teilweise nicht korrekt angepasst werden: " + ex.getMessage());
		}

		viewer.refresh();
	}

	private class ViewerContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return orderElements.toArray();
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
	}

	private class BaseLabelProvider extends ColumnLabelProvider {
		public Color getForeground(Object element) {
			Color color = null;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				if (orderElement.isVerified()) {
					color = OrderImportDialog.this.verifiedColor;
				}
			}

			return color;
		}

		public Font getFont(Object element) {
			Font font = null;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				if (orderElement.isVerified() && orderElement.getAmount() > 0) {
					font = OrderImportDialog.this.boldFont;
				}
			}

			return font;
		}
	}

	private class CheckboxLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			String text = StringUtils.EMPTY;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				if (orderElement.isVerified()) {
					text = "X"; //$NON-NLS-1$
				}
			}

			return text;
		}
	}

	private class AmountLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			String text = StringUtils.EMPTY;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getAmountAsString();
			}

			return text;
		}
	}

	private class StockLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			String text = StringUtils.EMPTY;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				IStockEntry stockEntry = orderElement.getStockEntry();

				if (stockEntry instanceof TransientStockEntry) {
					if (((TransientStockEntry) stockEntry).isCreated()) {
						IStockEntry created = ((TransientStockEntry) stockEntry).getCreated();
						text = Integer.valueOf(created.getCurrentStock()).toString();
					} else {
						if (ACTION_MODE_INVENTORY == actionMode) {
							text = stockEntry.getStock().getCode();
						} else {
							text = "bisher kein Lagerartikel";
						}
					}
				} else {
					text = Integer.valueOf(stockEntry.getCurrentStock()).toString();
				}
			}

			return text;
		}
	}

	private class PharamcodeLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			String text = StringUtils.EMPTY;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getArticle().getCode();
			}

			return text;
		}
	}

	private class EANLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			String text = StringUtils.EMPTY;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getArticle().getGtin();
			}

			return text;
		}
	}

	private class DescriptionLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			String text = StringUtils.EMPTY;

			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getArticle().getName();
			}

			return text;
		}
	}

	private class StockNameLabelProvider extends BaseLabelProvider {
		public String getText(Object element) {
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				IStock stock = orderElement.getStockEntry().getStock();
				return stock.getId().contains("PatientStock-") ? stock.getDescription() : stock.getCode();
			}
			return null;
		}
	}

	private class TransientStockEntry implements IStockEntry {

		private IArticle article;
		private IStock stock;

		private IStockEntry created = null;

		public TransientStockEntry(IArticle article) {
			this(article, null);
		}

		public TransientStockEntry(IArticle article, IStock stock) {
			this.article = article;
			this.stock = stock;
		}

		public IStockEntry create(OrderElement orderElement) {
			IStock stock = StockServiceHolder.get().getDefaultStock();
			created = StockServiceHolder.get().storeArticleInStock(stock,
					StoreToStringServiceHolder.getStoreToString(article));
			created.setMinimumStock(0);
			created.setCurrentStock(0);
			created.setMaximumStock(0);
			CoreModelServiceHolder.get().save(created);
			return created;
		}

		public boolean isCreated() {
			return created != null;
		}

		public IStockEntry getCreated() {
			return created;
		}

		@Override
		public IArticle getArticle() {
			return article;
		}

		@Override
		public int getMinimumStock() {
			return 0;
		}

		@Override
		public void setMinimumStock(int minStock) {
		}

		@Override
		public int getCurrentStock() {
			return 0;
		}

		@Override
		public void setCurrentStock(int currentStock) {
		}

		@Override
		public int getMaximumStock() {
			return 0;
		}

		@Override
		public void setMaximumStock(int maxStock) {
		}

		@Override
		public int getFractionUnits() {
			return 0;
		}

		@Override
		public void setFractionUnits(int rest) {
		}

		@Override
		public IContact getProvider() {
			String providerId = ConfigServiceHolder.getGlobal(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
			if (providerId != null) {
				Optional<IContact> defProvider = CoreModelServiceHolder.get().load(providerId, IContact.class);
				return defProvider.orElse(null);
			}
			return null;
		}

		@Override
		public void setProvider(IContact provider) {
		}

		@Override
		public IStock getStock() {
			if (stock == null) {
				return StockServiceHolder.get().getDefaultStock();
			}
			return stock;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getLabel() {
			return article.getLabel();
		}

		@Override
		public boolean addXid(String domain, String id, boolean updateIfExists) {
			return false;
		}

		@Override
		public IXid getXid(String domain) {
			return null;
		}

		@Override
		public boolean isDeleted() {
			return false;
		}

		@Override
		public void setDeleted(boolean value) {
		}

		@Override
		public void setStock(IStock value) {
		}

		@Override
		public void setArticle(IArticle value) {
		}

		@Override
		public Long getLastupdate() {
			return null;
		}
	}

	private static class TransientOrderEntry implements IOrderEntry {

		private static TransientOrderEntry instance;

		@Override
		public String getId() {
			return null;
		}

		public static IOrderEntry getInstance() {
			if (TransientOrderEntry.instance == null) {
				TransientOrderEntry.instance = new TransientOrderEntry();
			}
			return TransientOrderEntry.instance;
		}

		@Override
		public String getLabel() {
			return null;
		}

		@Override
		public boolean addXid(String domain, String id, boolean updateIfExists) {
			return false;
		}

		@Override
		public IXid getXid(String domain) {
			return null;
		}

		@Override
		public Long getLastupdate() {
			return null;
		}

		@Override
		public boolean isDeleted() {
			return false;
		}

		@Override
		public void setDeleted(boolean value) {
		}

		@Override
		public IOrder getOrder() {
			return null;
		}

		@Override
		public void setOrder(IOrder value) {
		}

		@Override
		public IStock getStock() {
			return null;
		}

		@Override
		public void setStock(IStock value) {
		}

		@Override
		public int getAmount() {
			return 0;
		}

		@Override
		public void setAmount(int value) {
		}

		@Override
		public IArticle getArticle() {
			return null;
		}

		@Override
		public void setArticle(IArticle value) {

		}

		@Override
		public IContact getProvider() {
			return null;
		}

		@Override
		public void setProvider(IContact value) {

		}

		@Override
		public OrderEntryState getState() {
			return null;
		}

		@Override
		public void setState(OrderEntryState value) {
		}

	}

	private class OrderElement {
		private boolean verified = false;

		private final IOrderEntry orderEntry;
		private final IStockEntry stockEntry;
		private int amount;

		OrderElement(IOrderEntry orderEntry, IStockEntry stockEntry, int amount) {
			this.orderEntry = orderEntry;
			this.stockEntry = stockEntry;
			this.amount = amount;
		}

		int getAmount() {
			return amount;
		}

		int getOrderAmount() {
			return orderEntry.getAmount();
		}

		void setOrderState(int state) {
			orderEntry.setState(OrderEntryState.ofValue(state));
		}

		OrderEntryState getOrderState() {
			return orderEntry.getState();
		}

		/**
		 * Set new amount. Sets verified to true.
		 *
		 * @param amount the new amount
		 */
		void setAmount(int amount) {
			this.amount = amount;
		}

		public IArticle getArticle() {
			return (IArticle) stockEntry.getArticle();
		}

		String getAmountAsString() {
			return Integer.valueOf(amount).toString();
		}

		public IStockEntry getStockEntry() {
			return stockEntry;
		}

		boolean isVerified() {
			return verified;
		}

		void setVerified(boolean verified) {
			this.verified = verified;
		}

		IOrderEntry getOrderEntry() {
			return orderEntry;
		}
	}
}
