/*******************************************************************************
 * Copyright (c) 2005-2016, G. Weirich and Elexis
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
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.article.IArticle;
import ch.elexis.core.ui.actions.ScannerEvents;
import ch.elexis.core.ui.text.ElexisText;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.BestellungEntry;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Stock;
import ch.elexis.data.StockEntry;

/*
 * @author Daniel Lutz
 *
 */
public class OrderImportDialog extends TitleAreaDialog {
	private static final String ALLE_MARKIEREN = " Alle markieren ";
	private static final int DIFF_SPINNER_MIN = 1;
	private static final int DIFF_SPINNER_DEFAULT = 1;
	
	/**
	 * Order to act on
	 */
	private List<OrderElement> orderElements;
	
	private Spinner diffSpinner;
	private ElexisText eanText;
	
	private TableViewer viewer;
	
	private Color verifiedColor;
	private Font boldFont;
	
	public OrderImportDialog(Shell parentShell, Bestellung bestellung){
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.SHELL_TRIM);
		
		orderElements = new ArrayList<OrderElement>();
		List<BestellungEntry> items = bestellung.getEntries();
		for (BestellungEntry item : items) {
			
			Stock stock = item.getStock();
			if (stock != null) {
				IStockEntry stockEntry = CoreHub.getStockService()
					.findStockEntryForArticleInStock(stock, item.getArticle().storeToString());
				if (stockEntry != null) {
					OrderElement orderElement =
						new OrderElement(stockEntry, item.getCount());
					orderElements.add(orderElement);
				}
			} else {
				// check if a stock entry was created since the order was created
				IStockEntry stockEntry = CoreHub.getStockService()
					.findPreferredStockEntryForArticle(item.getArticle().storeToString(),
						ElexisEventDispatcher.getSelectedMandator().getId());
				if (stockEntry != null) {
					OrderElement orderElement = new OrderElement(stockEntry, item.getCount());
					orderElements.add(orderElement);
				} else {
					IStockEntry transienStockEntry = new TransientStockEntry(item.getArticle());
					OrderElement orderElement =
						new OrderElement(transienStockEntry, item.getCount());
					orderElements.add(orderElement);
				}
			}
		}
	}
	
	public OrderImportDialog(Shell parentShell, IStockEntry stockEntry){
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.SHELL_TRIM);
		
		orderElements = new ArrayList<OrderElement>();
		OrderElement orderElement = new OrderElement((StockEntry) stockEntry, 1);
		orderElements.add(orderElement);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
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
		eanLabel.setText("EAN:");
		eanText = new ElexisText(scannerGroup, SWT.NONE);
		eanText.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		eanText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if (e.character == SWT.CR) {
					applyScanner();
				}
			}
		});
		
		Button button = new Button(scannerGroup, SWT.PUSH);
		button.setText("Übernehmen");
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e){
				applyScanner();
			}
			
			public void widgetDefaultSelected(SelectionEvent e){
				// do nothing
			}
		});
		
		Composite tableArea = new Composite(mainArea, SWT.NONE);
		tableArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tableArea.setLayout(new GridLayout());
		
		viewer = new TableViewer(tableArea, SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		verifiedColor = table.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
		boldFont = createBoldFont(table.getFont());
		
		final TableViewerFocusCellManager mgr =
			new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		ColumnViewerEditorActivationStrategy actSupport =
			new ColumnViewerEditorActivationStrategy(viewer) {
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event){
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
		viewer.setComparator(new ViewerComparator() {
			public int compare(Viewer viewer, Object e1, Object e2){
				Artikel a1 = ((OrderElement) e1).getArticle();
				Artikel a2 = ((OrderElement) e2).getArticle();
				
				if (a1 != null && a2 != null) {
					return a1.getName().compareTo(a2.getName());
				}
				return 0;
			};
		});
		
		Composite cButtons = new Composite(mainArea, SWT.NONE);
		cButtons.setLayout(new GridLayout(2, false));
		final Button clickAllButton = new Button(cButtons, SWT.PUSH);
		clickAllButton.setText(ALLE_MARKIEREN);
		clickAllButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
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
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		importButton.setLayoutData(gd);
		importButton.setText("Lagerbestände anpassen");
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				doImport();
			}
		});
		cButtons.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return mainArea;
	}
	
	private Font createBoldFont(Font baseFont){
		FontData fd = baseFont.getFontData()[0];
		Font font =
			new Font(baseFont.getDevice(), fd.getName(), fd.getHeight(), fd.getStyle() | SWT.BOLD);
		return font;
	}
	
	private void createViewerColumns(){
		TableViewerColumn column;
		
		final CheckboxCellEditor checkboxCellEditor = new CheckboxCellEditor(viewer.getTable());
		final TextCellEditor textCellEditor = new TextCellEditor(viewer.getTable());
		
		/* OK (checkbox column) */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("OK");
		column.getColumn().setWidth(50);
		column.setLabelProvider(new CheckboxLabelProvider());
		column.setEditingSupport(new EditingSupport(viewer) {
			public boolean canEdit(Object element){
				return true;
			}
			
			public CellEditor getCellEditor(Object element){
				return checkboxCellEditor;
			}
			
			public Object getValue(Object element){
				if (element instanceof OrderElement) {
					OrderElement orderElement = (OrderElement) element;
					return new Boolean(orderElement.isVerified());
				} else {
					return null;
				}
			}
			
			public void setValue(Object element, Object value){
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
		
		/* Amount delivered*/
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("Geliefert");
		column.getColumn().setWidth(60);
		column.setLabelProvider(new AmountLabelProvider());
		column.setEditingSupport(new EditingSupport(viewer) {
			public boolean canEdit(Object element){
				return true;
			}
			
			public CellEditor getCellEditor(Object element){
				return textCellEditor;
			}
			
			public Object getValue(Object element){
				if (element instanceof OrderElement) {
					OrderElement orderElement = (OrderElement) element;
					return orderElement.getAmountAsString();
				} else {
					return null;
				}
			}
			
			public void setValue(Object element, Object value){
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
		
		/* Amount on stock*/
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("Lager");
		column.getColumn().setWidth(60);
		column.setLabelProvider(new StockLabelProvider());
		
		/* Pharamcode */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("Pharmacode");
		column.getColumn().setWidth(80);
		column.setLabelProvider(new PharamcodeLabelProvider());
		
		/* EAN */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("EAN");
		column.getColumn().setWidth(110);
		column.setLabelProvider(new EANLabelProvider());
		
		/* Description */
		column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText("Beschreibung");
		column.getColumn().setWidth(300);
		column.setLabelProvider(new DescriptionLabelProvider());
		
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Bestellung im Lager einbuchen");
		setMessage("Bitte überprüfen Sie alle bestellten Artikel."
			+ " Überprüfte Artikel werden grün angezeigt."
			+ " Bei der Anpassung der Lagerbestände werden nur jene Artikel"
			+ " berücksichtigt, bei denen unter \"OK\" ein Haken gesetzt ist.");
		// setTitleImage(...));
		getShell().setText("Bestellung im Lager einbuchen");
	}
	
	// Replace OK/Cancel buttons by a close button
	protected void createButtonsForButtonBar(Composite parent){
		// Create Close button
		createButton(parent, IDialogConstants.OK_ID, "Schliessen", false);
	}
	
	@Override
	protected void okPressed(){
		super.okPressed();
	}
	
	// update the table according to the input from the scanner
	private void applyScanner(){
		int diff = diffSpinner.getSelection();
		
		String ean = eanText.getText().trim();
		// remove silly characters from scanner
		ean = ean.replaceAll(new Character(SWT.CR).toString(), "");
		ean = ean.replaceAll(new Character(SWT.LF).toString(), "");
		ean = ean.replaceAll(new Character((char) 0).toString(), "");
		
		eanText.setText("");
		diffSpinner.setSelection(DIFF_SPINNER_DEFAULT);
		
		OrderElement orderElement = findOrderElementByEAN(ean);
		if (orderElement != null) {
			int newAmount = orderElement.getAmount() + diff;
			updateOrderElement(orderElement, newAmount);
		} else {
			ScannerEvents.beep();
			SWTHelper.alert("Artikel nicht bestellt",
				"Dieser Artikel wurde nicht bestellt. Der Bestand kann nicht automatisch angepasst werden.");
		}
	}
	
	private OrderElement findOrderElementByEAN(String ean){
		if (ean == null) {
			return null;
		}
		
		for (OrderElement orderElement : orderElements) {
			if (orderElement.getArticle().getEAN().equals(ean)) {
				return orderElement;
			}
		}
		
		// not found
		return null;
	}
	
	private void updateOrderElement(OrderElement orderElement, int newAmount){
		orderElement.setAmount(newAmount);
		orderElement.setVerified(true);
		viewer.update(orderElement, null);
	}
	
	// read in verified order
	public void doImport(){
		try {
			for (OrderElement orderElement : orderElements) {
				if (orderElement.isVerified()) {
					IStockEntry stockEntry = orderElement.getStockEntry();
					if (stockEntry instanceof TransientStockEntry) {
						// create a non transient stock entry for the article
						stockEntry = ((TransientStockEntry) stockEntry).create(orderElement);
					}
					LockResponse lockResponse = CoreHub.getLocalLockService()
						.acquireLockBlocking((StockEntry) stockEntry, 1, new NullProgressMonitor());
					if (lockResponse.isOk()) {
						int diff = orderElement.getAmount();
						int oldAmount = stockEntry.getCurrentStock();
						int newAmount = oldAmount + diff;
						stockEntry.setCurrentStock(newAmount);
						
						// reset amount
						orderElement.setAmount(0);
						orderElement.setVerified(false);
						
						CoreHub.getLocalLockService().releaseLock(lockResponse.getLockInfo());
					} else {
						throw new IllegalStateException(
							"Could not acquire lock for stockEntry [" + stockEntry.getArticle().getLabel() + "]");
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
		public Object[] getElements(Object inputElement){
			return orderElements.toArray();
		}
		
		public void dispose(){
			// do nothing
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			// do nothing
		}
	}
	
	private class BaseLabelProvider extends ColumnLabelProvider {
		public Color getForeground(Object element){
			Color color = null;
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				if (orderElement.isVerified()) {
					color = OrderImportDialog.this.verifiedColor;
				}
			}
			
			return color;
		}
		
		public Font getFont(Object element){
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
		public String getText(Object element){
			String text = "";
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				if (orderElement.isVerified()) {
					text = "X";
				}
			}
			
			return text;
		}
	}
	
	private class AmountLabelProvider extends BaseLabelProvider {
		public String getText(Object element){
			String text = "";
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getAmountAsString();
			}
			
			return text;
		}
	}
	
	private class StockLabelProvider extends BaseLabelProvider {
		public String getText(Object element){
			String text = "";
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				IStockEntry stockEntry = orderElement.getStockEntry();
				
				if (stockEntry instanceof TransientStockEntry) {
					if(((TransientStockEntry) stockEntry).isCreated()) {
						IStockEntry created = ((TransientStockEntry) stockEntry).getCreated();
						text = new Integer(created.getCurrentStock()).toString();
					} else {
						text = "bisher kein Lagerartikel";
					}
				} else {
					text = new Integer(stockEntry.getCurrentStock()).toString();
				}
			}
			
			return text;
		}
	}
	
	private class PharamcodeLabelProvider extends BaseLabelProvider {
		public String getText(Object element){
			String text = "";
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getArticle().getPharmaCode();
			}
			
			return text;
		}
	}
	
	private class EANLabelProvider extends BaseLabelProvider {
		public String getText(Object element){
			String text = "";
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getArticle().getEAN();
			}
			
			return text;
		}
	}
	
	private class DescriptionLabelProvider extends BaseLabelProvider {
		public String getText(Object element){
			String text = "";
			
			if (element instanceof OrderElement) {
				OrderElement orderElement = (OrderElement) element;
				text = orderElement.getArticle().getName();
			}
			
			return text;
		}
	}
	
	private class TransientStockEntry implements IStockEntry {
		
		private Artikel article;
		
		private IStockEntry created = null;
		
		public TransientStockEntry(Artikel article){
			this.article = article;
		}
		
		public IStockEntry create(OrderElement orderElement){
			Stock stock = Stock.load(Stock.DEFAULT_STOCK_ID);
			created = CoreHub.getStockService().storeArticleInStock(stock, article.storeToString());
			created.setMinimumStock(0);
			created.setMaximumStock(0);
			return created;
		}
		
		public boolean isCreated(){
			return created != null;
		}
		
		public IStockEntry getCreated(){
			return created;
		}
		
		@Override
		public IArticle getArticle(){
			return article;
		}
		
		@Override
		public int getMinimumStock(){
			return 0;
		}
		
		@Override
		public void setMinimumStock(int minStock){
		}
		
		@Override
		public int getCurrentStock(){
			return 0;
		}
		
		@Override
		public void setCurrentStock(int currentStock){
		}
		
		@Override
		public int getMaximumStock(){
			return 0;
		}
		
		@Override
		public void setMaximumStock(int maxStock){
		}
		
		@Override
		public int getFractionUnits(){
			return 0;
		}
		
		@Override
		public void setFractionUnits(int rest){
		}
		
		@Override
		public Object getProvider(){
			String providerId =
				CoreHub.globalCfg.get(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
			if (providerId != null) {
				Kontakt defProvider = Kontakt.load(providerId);
				if (defProvider.exists()) {
					return defProvider;
				}
			}
			return null;
		}
		
		@Override
		public void setProvider(Object provider){
		}
		
		@Override
		public IStock getStock(){
			return Stock.load(Stock.DEFAULT_STOCK_ID);
		}
	}
	
	private class OrderElement {
		private boolean verified = false;
		
		private final IStockEntry stockEntry;
		private int amount;
		
		OrderElement(IStockEntry stockEntry, int amount){
			this.stockEntry = stockEntry;
			this.amount = amount;
		}
		
		int getAmount(){
			return amount;
		}
		
		/**
		 * Set new amount. Sets verified to true.
		 * 
		 * @param amount
		 *            the new amount
		 */
		void setAmount(int amount){
			this.amount = amount;
		}
		
		public Artikel getArticle(){
			return (Artikel) stockEntry.getArticle();
		}
		
		String getAmountAsString(){
			return new Integer(amount).toString();
		}
		
		public IStockEntry getStockEntry(){
			return stockEntry;
		}
		
		boolean isVerified(){
			return verified;
		}
		
		void setVerified(boolean verified){
			this.verified = verified;
		}
	}
}
