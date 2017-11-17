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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.dialogs.StockSelectorDialog;
import ch.elexis.core.ui.editors.KontaktSelektorDialogCellEditor;
import ch.elexis.core.ui.editors.PersistentObjectEditingSupport;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.Message;
import ch.elexis.core.ui.util.viewers.DefaultContentProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.WidgetProvider;
import ch.elexis.core.ui.views.provider.StockEntryLabelProvider;
import ch.elexis.data.Artikel;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Stock;
import ch.elexis.data.StockEntry;
import ch.elexis.scripting.CSVWriter;
import ch.rgw.tools.ExHandler;

public class StockView extends ViewPart implements ISaveablePart2, IActivationListener {
	public StockView(){}
	
	public static final String ID = "ch.elexis.core.ui.views.StockView"; //$NON-NLS-1$
	private Logger log = LoggerFactory.getLogger(StockView.class);
	
	private CommonViewer cv;
	private ViewerConfigurer vc;
	private ViewMenus viewMenus;
	private IAction refreshAction, exportAction;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		cv = new CommonViewer();
		vc = new ViewerConfigurer(new DefaultContentProvider(cv, StockEntry.class) {
			@Override
			public Object[] getElements(Object inputElement){
				return new Query<StockEntry>(StockEntry.class).execute().toArray();
			}
			
		}, new StockEntryLabelProvider() {}, null, new ViewerConfigurer.DefaultButtonProvider(),
			new LagerWidgetProvider());
		cv.create(vc, parent, SWT.NONE, getViewSite());
		cv.getConfigurer().getContentProvider().startListening();
		
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		
		contextMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				manager.add(new CheckInOrderedAction(cv.getViewerWidget()));
				manager.add(new DeleteStockEntryAction(cv.getViewerWidget()));
				ArticleMachineOutputAction amoa =
					new ArticleMachineOutputAction(cv.getViewerWidget());
				if (amoa.isVisible()) {
					manager.add(amoa);
					manager.add(new FullMachineInventoryAction(cv.getViewerWidget()));
				}
				
			}
		});
		
		new PersistentObjectDropTarget(cv.getViewerWidget().getControl(),
			new PersistentObjectDropTarget.IReceiver() {
				
				@Override
				public void dropped(PersistentObject o, DropTargetEvent e){
					if (o instanceof Artikel) {
						Artikel art = (Artikel) o;
						StockSelectorDialog ssd =
							new StockSelectorDialog(UiDesk.getTopShell(), false);
						int open = ssd.open();
						if (open == Dialog.OK) {
							if (ssd.getResult().length > 0) {
								Stock stock = (Stock) ssd.getResult()[0];
								if (stock != null) {
									CoreHub.getStockService().storeArticleInStock(stock,
										art.storeToString());
									cv.notify(Message.update);
								}
							}
						}
					}
				}
				
				@Override
				public boolean accept(PersistentObject o){
					if (o instanceof Artikel) {
						Artikel art = (Artikel) o;
						return !art.isProduct();
					}
					return false;
				}
			});
		
		cv.setContextMenu(contextMenu);
		
		makeActions();
		viewMenus = new ViewMenus(getViewSite());
		viewMenus.createToolbar(refreshAction);
		viewMenus.createMenu(exportAction);
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	private void makeActions(){
		refreshAction = new Action(Messages.StockView_reload) {
			{
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			}
			
			@Override
			public void run(){
				cv.notify(Message.update);
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
								"Typ nach Liste (SL, SL-Betäubung, P, N, LPPV, Migl)", "Lieferant"
							};
							csv.writeNext(header);
							
							for (Object o : vc.getContentProvider().getElements(null)) {
								if (o instanceof StockEntry) {
									String[] line = new String[header.length];
									StockEntry stockEntry = (StockEntry) o;
									Artikel artikel = stockEntry.getArticle();
									if (artikel != null) {
										line[0] = artikel.getLabel();
										line[1] = artikel.getPharmaCode();
										line[2] = artikel.getEAN();
										line[3] = String.valueOf(stockEntry.getMaximumStock());
										line[4] = String.valueOf(stockEntry.getMinimumStock());
										line[5] = String.valueOf(stockEntry.getCurrentStock());
										line[6] = String.valueOf(stockEntry.getFractionUnits());
										line[7] = String.valueOf(artikel.getPackungsGroesse());
										line[8] = String.valueOf(artikel.getAbgabeEinheit());
										line[9] = artikel.getEKPreis().getAmountAsString();
										line[10] = artikel.getVKPreis().getAmountAsString();
										line[11] = artikel.get(Artikel.FLD_TYP);
										Kontakt provider = stockEntry.getProvider();
										if (provider != null) {
											line[12] = provider.getLabel();
										}
										csv.writeNext(line);
										success++;
									} else {
										errorUnkownArticle++;
										log.warn("cannot export: id [" + stockEntry.getId()
											+ "] artikelId ["
											+ stockEntry.get(StockEntry.FLD_ARTICLE_ID)
											+ "] artikelType ["
											+ stockEntry.get(StockEntry.FLD_ARTICLE_TYPE) + "] ");
									}
									
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
		// cv.getConfigurer().getControlFieldProvider().setFocus();
	}
	
	@Override
	public void dispose(){
		cv.getConfigurer().getContentProvider().stopListening();
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	class LagerWidgetProvider implements WidgetProvider {
		String[] columns = {
			Messages.LagerView_stock, Messages.LagerView_pharmacode, Messages.LagerView_gtin,
			Messages.LagerView_name, Messages.LagerView_minBestand, Messages.LagerView_istBestand,
			Messages.LagerView_maxBestand, Messages.LagerView_dealer
		};
		int[] colwidth = {
			40, 75, 90, 250, 35, 35, 35, 150
		};
		
		public StructuredViewer createViewer(Composite parent){
			Table table =
				new Table(parent, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.VIRTUAL);
			table.setHeaderVisible(true);
			table.setLinesVisible(false);
			TableViewer ret = new TableViewer(table);
			for (int i = 0; i < columns.length; i++) {
				TableViewerColumn tvc = new TableViewerColumn(ret, SWT.NONE);
				TableColumn tc = tvc.getColumn();
				tc.setText(columns[i]);
				tc.setWidth(colwidth[i]);
				tc.setData(i);
				tc.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						cv.getViewerWidget().setSorter(new LagerTableSorter(
							(Integer) ((TableColumn) e.getSource()).getData()));
					}
				});
				
				PersistentObjectEditingSupport poes = null;
				if (i == 4) {
					poes = new PersistentObjectEditingSupport(ret, StockEntry.FLD_MIN,
						Integer.class, true);
				} else if (i == 5) {
					poes = new PersistentObjectEditingSupport(ret, StockEntry.FLD_CURRENT,
						Integer.class, true) {
						protected boolean canEdit(Object element){
							boolean canEdit = super.canEdit(element);
							if (canEdit) {
								StockEntry se = (StockEntry) element;
								return !se.getStock().isCommissioningSystem();
							}
							return true;
						};
					};
				} else if (i == 6) {
					poes = new PersistentObjectEditingSupport(ret, StockEntry.FLD_MAX,
						Integer.class, true);
				}
				
				if (poes != null) {
					tvc.setEditingSupport(poes);
				}
				
				if (i == 7) {
					EditingSupport providerEditingSupport = new EditingSupport(ret) {
						
						@Override
						protected void setValue(Object element, Object value){
							StockEntry se = (StockEntry) element;
							if (se == null) {
								return;
							}
							
							LockResponse lr = CoreHub.getLocalLockService().acquireLock(se);
							if (!lr.isOk()) {
								return;
							}
							
							se.setProvider((Kontakt) value);
							
							lr = CoreHub.getLocalLockService().releaseLock((se));
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
							return (Kontakt) se.getProvider();
						}
						
						@Override
						protected CellEditor getCellEditor(Object element){
							return new KontaktSelektorDialogCellEditor(
								((TableViewer) getViewer()).getTable(), "Lieferant auswählen",
								"Bitte selektieren Sie den Lieferant");
						}
						
						@Override
						protected boolean canEdit(Object element){
							StockEntry stockEntry = (StockEntry) element;
							return (stockEntry != null && stockEntry.getArticle() != null);
						}
					};
					tvc.setEditingSupport(providerEditingSupport);
				}
			}
			ret.setSorter(new LagerTableSorter(3));
			
			TableViewerFocusCellManager focusCellManager =
				new TableViewerFocusCellManager(ret, new FocusCellHighlighter(ret) {});
			ColumnViewerEditorActivationStrategy editorActivationStrategy =
				new ColumnViewerEditorActivationStrategy(ret) {
					@Override
					protected boolean isEditorActivationEvent(
						ColumnViewerEditorActivationEvent event){
						ViewerCell cell = (ViewerCell) event.getSource();
						return cell.getColumnIndex() > 3 && cell.getColumnIndex() < 7;
					}
				};
			TableViewerEditor.create(ret, focusCellManager, editorActivationStrategy,
				TableViewerEditor.TABBING_HORIZONTAL);
			
			return ret;
		}
		
		class LagerTableSorter extends ViewerSorter {
			int col;
			
			LagerTableSorter(int c){
				col = c;
			}
			
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				String s1 = ((StockEntryLabelProvider) cv.getConfigurer().getLabelProvider())
					.getColumnText(e1, col).toLowerCase();
				String s2 = ((StockEntryLabelProvider) cv.getConfigurer().getLabelProvider())
					.getColumnText(e2, col).toLowerCase();
				return s1.compareTo(s2);
			}
			
		}
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
		
		public void run(){
			StockEntry stockEntry = fetchSelection();
			IStatus status = CoreHub.getStockCommissioningSystemService()
				.synchronizeInventory(stockEntry.getStock(), null, null);
			if (!status.isOK()) {
				ElexisStatus elStatus = new ElexisStatus(status);
				StatusManager.getManager().handle(elStatus, StatusManager.SHOW);
			}
		}
		
		private StockEntry fetchSelection(){
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof StockEntry) {
				return (StockEntry) selection.getFirstElement();
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
			StockEntry stockEntry = fetchSelection();
			if (stockEntry != null) {
				Stock stock = stockEntry.getStock();
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
		
		public void run(){
			StockEntry stockEntry = fetchSelection();
			IStatus status = CoreHub.getStockCommissioningSystemService()
				.performArticleOutlay(stockEntry, 1, null);
			if (!status.isOK()) {
				ElexisStatus elStatus = new ElexisStatus(status);
				StatusManager.getManager().handle(elStatus, StatusManager.SHOW);
			}
		}
		
		private StockEntry fetchSelection(){
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof StockEntry) {
				return (StockEntry) selection.getFirstElement();
			}
			return null;
		};
	}
	
	public class CheckInOrderedAction extends Action {
		private Viewer viewer;
		private StockEntry stockEntry;
		
		public CheckInOrderedAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public boolean isEnabled(){
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof StockEntry) {
				stockEntry = (StockEntry) selection.getFirstElement();
				if (stockEntry.getArticle() != null) {
					return (CoreHub.getOrderService()
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
			OrderImportDialog dialog =
				new OrderImportDialog(viewer.getControl().getShell(), stockEntry);
			dialog.open();
			viewer.refresh();
			
		}
	}
	
	public class DeleteStockEntryAction extends Action {
		private Viewer viewer;
		private StockEntry stockEntry;
		
		public DeleteStockEntryAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public boolean isEnabled(){
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof StockEntry) {
				stockEntry = (StockEntry) selection.getFirstElement();
				if (stockEntry != null) {
					Stock stock = stockEntry.getStock();
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
				Artikel article = stockEntry.getArticle();
				if (article != null && MessageDialog.openConfirm(viewer.getControl().getShell(),
					Messages.LagerView_deleteActionConfirmCaption, MessageFormat
						.format(Messages.LagerView_deleteConfirmBody, article.getName()))) {
					stockEntry.delete();
					viewer.refresh();
				}
			}
		}
	}
	
	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	public void activation(boolean mode){}
	
	public void visible(boolean mode){
		if (mode) {
			cv.notify(CommonViewer.Message.update);
		}
	}
}
