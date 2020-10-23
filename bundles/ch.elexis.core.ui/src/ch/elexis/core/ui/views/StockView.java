/*******************************************************************************
 * Copyright (c) 2005-2016, MEDEVIT and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  <office@medevit.at> - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.views;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IStockService.Availability;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.services.holder.StockCommissioningServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.dialogs.StockSelectorDialog;
import ch.elexis.core.ui.editors.KontaktSelektorDialogCellEditor;
import ch.elexis.core.ui.editors.NumericCellEditorValidator;
import ch.elexis.core.ui.editors.ReflectiveEditingSupport;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.dnd.IdentifiableDragSource;
import ch.elexis.core.ui.util.dnd.IdentifiableDropTarget;
import ch.elexis.core.ui.views.provider.StockEntryLabelProvider;
import ch.elexis.core.ui.views.provider.StockEntryLabelProvider.ColumnStockEntryLabelProvider;
import ch.elexis.scripting.CSVWriter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;

public class StockView extends ViewPart implements ISaveablePart2, IActivationListener {
	public StockView(){}
	
	public static final String ID = "ch.elexis.core.ui.views.StockView"; //$NON-NLS-1$
	private Logger log = LoggerFactory.getLogger(StockView.class);
	
	private Text filterText;
	private StockEntryLoader loader;
	
	private TableViewer viewer;
	private StockEntryLabelProvider labelProvider;
	
	private ViewMenus viewMenus;
	private IAction refreshAction, exportAction;
	
	String[] columns = {
		Messages.LagerView_stock, Messages.LagerView_pharmacode, Messages.LagerView_gtin,
		Messages.LagerView_name, Messages.LagerView_vkPreis, Messages.LagerView_minBestand, Messages.LagerView_istBestand,
		Messages.LagerView_maxBestand, Messages.LagerView_dealer
	};
	int[] colwidth = {
		50, 75, 90, 250, 50, 35, 35, 35, 150
	};
	
	private void refreshConsiderFilter(){
		if (!refreshUseFilter()) {
			loader = new StockEntryLoader(viewer);
			loader.schedule();
		}
	}
	
	private boolean refreshUseFilter(){
		String search = filterText.getText();
		if (search != null && search.length() > 2) {
			if (loader != null) {
				loader.cancel();
			}
			loader = new StockEntryLoader(viewer, search);
			loader.schedule();
			return true;
		}
		return false;
	}
	
	@Override
	public void createPartControl(Composite parent){
		labelProvider = new StockEntryLabelProvider();
		parent.setLayout(new GridLayout(2, false));
		
		filterText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		filterText.setMessage("Filter");
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e){
				if (!refreshUseFilter()) {
					// reload all if empty
					if (filterText.getText().isEmpty()) {
						loader = new StockEntryLoader(viewer);
						loader.schedule();
					}
				}
			}
		});
		ToolBarManager tbm = new ToolBarManager();
		tbm.add(new Action("", Action.AS_CHECK_BOX) {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_FILTER.getImageDescriptor();
			}
			
			@Override
			public String getToolTipText(){
				return "Nur zu bestellende anzeigen";
			}
			
			@Override
			public void run(){
				StockEntryLoader.setFilterOrderOnly(isChecked());
				refreshConsiderFilter();
			}
		});
		tbm.createControl(parent);
		
		viewer = new TableViewer(parent,
			SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.VIRTUAL);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(labelProvider);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		for (int i = 0; i < columns.length; i++) {
			TableViewerColumn tvc = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn tc = tvc.getColumn();
			tc.setText(columns[i]);
			tc.setWidth(colwidth[i]);
			tc.setData(i);
			tvc.setLabelProvider(new ColumnStockEntryLabelProvider(i, labelProvider));
			
			ReflectiveEditingSupport poes = null;
			if (i == 5) {
				poes = new ReflectiveEditingSupport(viewer,
					ModelPackage.Literals.ISTOCK_ENTRY__MINIMUM_STOCK.getName(),
					new NumericCellEditorValidator(), true);
			} else if (i == 6) {
				poes = new ReflectiveEditingSupport(viewer,
					ModelPackage.Literals.ISTOCK_ENTRY__CURRENT_STOCK.getName(),
					new NumericCellEditorValidator(), true) {
					@Override
					protected boolean canEdit(Object element){
						boolean canEdit = super.canEdit(element);
						if (canEdit) {
							IStockEntry se = (IStockEntry) element;
							return !se.getStock().isCommissioningSystem();
						}
						return true;
					};
				};
			} else if (i == 7) {
				poes = new ReflectiveEditingSupport(viewer,
					ModelPackage.Literals.ISTOCK_ENTRY__MAXIMUM_STOCK.getName(),
					new NumericCellEditorValidator(), true);
			}
			
			if (poes != null) {
				tvc.setEditingSupport(poes.setModelService(CoreModelServiceHolder.get()));
			}
			
			if (i == 8) {
				EditingSupport providerEditingSupport = new EditingSupport(viewer) {
					
					@Override
					protected void setValue(Object element, Object value){
						IStockEntry se = (IStockEntry) element;
						if (se == null) {
							return;
						}
						
						LockResponse lr = LocalLockServiceHolder.get().acquireLock(se);
						if (!lr.isOk()) {
							return;
						}
						
						se.setProvider((IContact) value);
						
						lr = LocalLockServiceHolder.get().releaseLock((se));
						if (!lr.isOk()) {
							log.warn("Error releasing lock for [{}]: {}", se.getId(),
								lr.getStatus());
						}
						getViewer().refresh();
					}
					
					@Override
					protected Object getValue(Object element){
						IStockEntry se = (IStockEntry) element;
						if (se == null) {
							return null;
						}
						return se.getProvider();
					}
					
					@Override
					protected CellEditor getCellEditor(Object element){
						return new KontaktSelektorDialogCellEditor(
							((TableViewer) getViewer()).getTable(), "Lieferant auswählen",
							"Bitte selektieren Sie den Lieferant");
					}
					
					@Override
					protected boolean canEdit(Object element){
						IStockEntry stockEntry = (IStockEntry) element;
						return (stockEntry != null && stockEntry.getArticle() != null);
					}
				};
				tvc.setEditingSupport(providerEditingSupport);
			}
		}
		TableViewerFocusCellManager focusCellManager =
			new TableViewerFocusCellManager(viewer, new FocusCellHighlighter(viewer) {});
		ColumnViewerEditorActivationStrategy editorActivationStrategy =
			new ColumnViewerEditorActivationStrategy(viewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event){
					ViewerCell cell = (ViewerCell) event.getSource();
					return cell.getColumnIndex() > 3 && cell.getColumnIndex() < 7;
				}
			};
		TableViewerEditor.create(viewer, focusCellManager, editorActivationStrategy,
			TableViewerEditor.TABBING_HORIZONTAL);
		
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		
		contextMenu.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				manager.add(new CheckInOrderedAction(viewer));
				manager.add(new DeleteStockEntryAction(viewer));
				ArticleMachineOutputAction amoa = new ArticleMachineOutputAction(viewer);
				if (amoa.isVisible()) {
					manager.add(amoa);
					manager.add(new FullMachineInventoryAction(viewer));
				}
			}
		});
		
		new IdentifiableDropTarget(viewer.getControl(), new IdentifiableDropTarget.IReceiver() {
			
			@Override
			public void dropped(List<Identifiable> identifiables){
				for (Identifiable identifiable : identifiables) {
					if (identifiable instanceof IArticle) {
						if (!((IArticle) identifiable).isProduct()) {
							StockSelectorDialog ssd =
								new StockSelectorDialog(UiDesk.getTopShell(), false);
							int open = ssd.open();
							if (open == Dialog.OK) {
								if (ssd.getResult().length > 0) {
									IStock stock = (IStock) ssd.getResult()[0];
									if (stock != null) {
										StockServiceHolder.get().storeArticleInStock(stock,
											StoreToStringServiceHolder
												.getStoreToString((IArticle) identifiable));
										viewer.refresh();
									}
								}
							}
						}
					}
				}
			}
			
			@Override
			public boolean accept(List<Identifiable> identifiables){
				for (Identifiable identifiable : identifiables) {
					if (!(identifiable instanceof IArticle)) {
						return false;
					}
				}
				return true;
			}
		});
		
		// add drag support
		new IdentifiableDragSource(viewer);
		
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		
		makeActions();
		viewMenus = new ViewMenus(getViewSite());
		viewMenus.createToolbar(refreshAction);
		viewMenus.createMenu(exportAction);
		
		GlobalEventDispatcher.addActivationListener(this, this);
		CoreUiUtil.injectServices(this);
	}
	
	@Optional
	@Inject
	public void udpate(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IStockEntry entry){
		if (entry != null) {
			if (viewer != null && !viewer.getControl().isDisposed()) {
				viewer.update(entry, null);
			}
		}
	}
	
	private void makeActions(){
		refreshAction = new Action(Messages.StockView_reload) {
			{
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			}
			
			@Override
			public void run(){
				refreshConsiderFilter();
			}
		};
		
		exportAction =
			new Action(Messages.LagerView_exportAction, Images.IMG_EXPORT.getImageDescriptor()) {
				@Override
				public void run(){
					FileDialog dialog = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
					dialog.setFilterExtensions(new String[] {
						"*.csv"
					});
					dialog.setFilterNames(new String[] {
						"Comma Separated Values (CSV)"
					});
					
					dialog.setOverwrite(true);
					dialog.setFileName("lager_export.csv");
					String pathToSave = dialog.open();
					if (pathToSave != null) {
						CSVWriter csv = null;
						try {
							int errorUnkownArticle = 0;
							int success = 0;
							csv = new CSVWriter(new FileWriter(pathToSave));
							log.debug("csv export started for: " + pathToSave);
							String[] header = new String[] {
								"Name", "Pharmacode", "EAN", "Max", "Min",
								"Aktuell Packung an Lager", "Aktuell an Lager (Anbruch)",
								" Stück pro Packung", "Stück pro Abgabe", "Einkaufspreis",
								"Verkaufspreis",
								"Typ (P, N, ...)", "Lieferant"
							};
							csv.writeNext(header);
							
							List<IStockEntry> entries =
								CoreModelServiceHolder.get().getQuery(IStockEntry.class).execute();
							for (IStockEntry iStockEntry : entries) {
								String[] line = new String[header.length];
								IArticle article = iStockEntry.getArticle();
								if (article != null) {
									line[0] = article.getLabel();
									line[1] = article.getCode();
									line[2] = article.getGtin();
									line[3] = String.valueOf(iStockEntry.getMaximumStock());
									line[4] = String.valueOf(iStockEntry.getMinimumStock());
									line[5] = String.valueOf(iStockEntry.getCurrentStock());
									line[6] = String.valueOf(iStockEntry.getFractionUnits());
									line[7] = String.valueOf(article.getPackageSize());
									line[8] = String.valueOf(article.getSellingSize());
									line[9] =
										new Money(article.getPurchasePrice()).getAmountAsString();
									line[10] =
										new Money(article.getSellingPrice()).getAmountAsString();
									line[11] =
										Character.toString(article.getSubTyp().getTypeChar());
									IContact provider = iStockEntry.getProvider();
									if (provider != null) {
										line[12] = provider.getLabel();
									}
									csv.writeNext(line);
									success++;
								} else {
									errorUnkownArticle++;
									log.warn(
										"cannot export: id [" + iStockEntry.getId() + "] "
											+ iStockEntry.getLabel());
								}
							}
							csv.close();
							log.debug("csv export finished for: " + pathToSave);
							StringBuffer msg = new StringBuffer();
							msg.append("Der Export nach ");
							msg.append(pathToSave);
							msg.append(" ist abgeschlossen.");
							msg.append("\n\n");
							msg.append(success);
							msg.append(" Artikel wurden erfolgreich exportiert.");
							if (errorUnkownArticle > 0) {
								msg.append("\n");
								msg.append(errorUnkownArticle);
								msg.append(
									" Artikel konnten nicht exportiert werden (Unbekannte Artikel Typen).");
							}
							SWTHelper.showInfo("Lager export", msg.toString());
						} catch (Exception ex) {
							ExHandler.handle(ex);
							log.error("csv exporter error", ex);
							SWTHelper.showError("Fehler", ex.getMessage());
						} finally {
							if (csv != null) {
								try {
									csv.close();
								} catch (IOException e) {
									log.error("cannot close csv exporter", e);
								}
							}
						}
					}
					
				}
			};
	}
	
	@Override
	public void setFocus(){
		filterText.setFocus();
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
		
	public class FullMachineInventoryAction extends Action {
		private Viewer viewer;
		
		public FullMachineInventoryAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_SYNC.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return Messages.StockView_PerformFullInventoryOnCommSystem;
		}
		
		@Override
		public void run(){
			IStockEntry stockEntry = fetchSelection();
			IStatus status = StockCommissioningServiceHolder.get()
				.synchronizeInventory(stockEntry.getStock(), null, null);
			if (!status.isOK()) {
				ElexisStatus elStatus = new ElexisStatus(status);
				StatusManager.getManager().handle(elStatus, StatusManager.SHOW);
			}
		}
		
		private IStockEntry fetchSelection(){
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof IStockEntry) {
				return (IStockEntry) selection.getFirstElement();
			}
			return null;
		};
	}
	
	public class ArticleMachineOutputAction extends Action {
		private Viewer viewer;
		
		public ArticleMachineOutputAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		public boolean isVisible(){
			IStockEntry stockEntry = fetchSelection();
			if (stockEntry != null) {
				IStock stock = stockEntry.getStock();
				if (stock != null) {
					String driverUuid = stock.getDriverUuid();
					return (driverUuid != null && driverUuid.length() > 8);
				}
			}
			return false;
		}
		
		@Override
		public String getText(){
			return Messages.StockView_OutlayArticle;
		}
		
		@Override
		public void run(){
			IStockEntry stockEntry = fetchSelection();
			IStatus status =
				StockCommissioningServiceHolder.get().performArticleOutlay(stockEntry, 1, null);
			if (!status.isOK()) {
				ElexisStatus elStatus = new ElexisStatus(status);
				StatusManager.getManager().handle(elStatus, StatusManager.SHOW);
			}
		}
		
		private IStockEntry fetchSelection(){
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof IStockEntry) {
				return (IStockEntry) selection.getFirstElement();
			}
			return null;
		};
	}
	
	public class CheckInOrderedAction extends Action {
		private Viewer viewer;
		private IStockEntry stockEntry;
		
		public CheckInOrderedAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public boolean isEnabled(){
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof IStockEntry) {
				stockEntry = (IStockEntry) selection.getFirstElement();
				if (stockEntry.getArticle() != null) {
					return (OrderServiceHolder.get()
						.findOpenOrderEntryForStockEntry(stockEntry) != null);
				}
			}
			return false;
		}
		
		@Override
		public String getText(){
			return Messages.BestellView_CheckInCaption;
		}
		
		@Override
		public void run(){
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof IStockEntry) {
				stockEntry = (IStockEntry) selection.getFirstElement();
				if (stockEntry.getArticle() != null) {
					IOrderEntry orderEntry =
						OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry);
					OrderImportDialog dialog =
						new OrderImportDialog(viewer.getControl().getShell(),
							orderEntry.getOrder());
					dialog.open();
					viewer.refresh();
				}
			}
		}
	}
	
	public class DeleteStockEntryAction extends Action {
		private Viewer viewer;
		private IStockEntry stockEntry;
		
		public DeleteStockEntryAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public boolean isEnabled(){
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof IStockEntry) {
				stockEntry = (IStockEntry) selection.getFirstElement();
				if (stockEntry != null) {
					IStock stock = stockEntry.getStock();
					return stock != null && !stock.isCommissioningSystem();
				}
			}
			return false;
		}
		
		@Override
		public String getText(){
			return Messages.LagerView_deleteAction;
		}
		
		@Override
		public String getToolTipText(){
			return Messages.LagerView_deleteActionToolTip;
		}
		
		@Override
		public void run(){
			if (stockEntry != null) {
				IArticle article = stockEntry.getArticle();
				if (article != null && MessageDialog.openConfirm(viewer.getControl().getShell(),
					Messages.LagerView_deleteActionConfirmCaption, MessageFormat
						.format(Messages.LagerView_deleteConfirmBody, article.getName()))) {
					CoreModelServiceHolder.get().delete(stockEntry);
					viewer.refresh();
				}
			}
		}
	}
	
	private static class StockEntryLoader extends Job {
		private Viewer viewer;
		
		private static boolean filterOrderOnly = false;
		private String filter;
		
		private List<ch.elexis.core.model.IStockEntry> loaded;
		
		public StockEntryLoader(Viewer viewer, String filter){
			super("Stock loading ...");
			this.viewer = viewer;
			this.filter = filter;
		}
		
		public static void setFilterOrderOnly(boolean checked){
			filterOrderOnly = checked;
		}
		
		public StockEntryLoader(Viewer viewer){
			super("Stock loading ...");
			this.viewer = viewer;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor){
			monitor.beginTask("Stock loading ...", IProgressMonitor.UNKNOWN);
			loaded = StockServiceHolder.get().getAllStockEntries();
			if (filterOrderOnly) {
				loaded = loaded.parallelStream().filter(se -> selectOrderOnly(se))
					.collect(Collectors.toList());
			}
			if (filter != null) {
				loaded = loaded.parallelStream().filter(se -> selectFilter(se, filter))
					.collect(Collectors.toList());
			}
			
			loaded.sort((l, r) -> {
				if(l.getArticle() != null && r.getArticle() != null) {
					return l.getArticle().getLabel().compareTo(r.getArticle().getLabel());
				}
				return 0;
			});
			
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run(){
					viewer.setInput(loaded);
				}
			});
			return Status.OK_STATUS;
		}
		
		private boolean selectOrderOnly(IStockEntry se){
			Availability availability = StockServiceHolder.get().determineAvailability(se);
			if (availability != null) {
				switch (availability) {
				case CRITICAL_STOCK:
				case OUT_OF_STOCK:
					return true;
				default:
					return false;
				}
			}
			return false;
		}
		
		private boolean selectFilter(IStockEntry se, String filter){
			if (se.getArticle() != null && se.getArticle().getLabel() != null) {
				return se.getArticle().getLabel().toLowerCase().contains(filter.toLowerCase());
			}
			return false;
		}
	}
	
	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	@Override
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	@Override
	public void doSaveAs(){ /* leer */
	}
	
	@Override
	public boolean isDirty(){
		return true;
	}
	
	@Override
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	@Override
	public void activation(boolean mode){}
	
	@Override
	public void visible(boolean mode){
		List<IStockEntry> allEntries =
			CoreModelServiceHolder.get().getQuery(IStockEntry.class).execute();
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.setInput(Collections.singletonList(
				new String("Es sind " + allEntries.size() + " Lagereinträge vorhanden")));
		}
	}
}
