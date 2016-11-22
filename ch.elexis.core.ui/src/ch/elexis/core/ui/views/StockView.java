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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.stock.IStockEntry;
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

public class StockView extends ViewPart implements ISaveablePart2, IActivationListener {
	public StockView(){}
	
	public static final String ID = "ch.elexis.core.ui.views.StockView"; //$NON-NLS-1$
	private Logger log = LoggerFactory.getLogger(StockView.class);
	
	private CommonViewer cv;
	private ViewerConfigurer vc;
	private ViewMenus viewMenus;
	private IAction refreshAction;
	
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
				manager.add(new ArticleLayoutAction(cv.getViewerWidget()));
			}
		});
		
		new PersistentObjectDropTarget(cv.getViewerWidget().getControl(),
			new PersistentObjectDropTarget.IReceiver() {
				
				@Override
				public void dropped(PersistentObject o, DropTargetEvent e){
					if (o instanceof Artikel) {
						Artikel art = (Artikel) o;
						StockSelectorDialog ssd = new StockSelectorDialog(UiDesk.getTopShell());
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
					poes = new PersistentObjectEditingSupport(ret, StockEntry.FLD_MIN);
				} else if (i == 5) {
					poes = new PersistentObjectEditingSupport(ret, StockEntry.FLD_CURRENT);
				} else if (i == 6) {
					poes = new PersistentObjectEditingSupport(ret, StockEntry.FLD_MAX);
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
	
	public class ArticleLayoutAction extends Action {
		private Viewer viewer;
		
		public ArticleLayoutAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public boolean isEnabled(){
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
			// TODO Status Dialog
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
