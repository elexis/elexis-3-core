/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.commands.EditEigenartikelUi;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.LagerartikelUtil;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.DefaultContentProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.WidgetProvider;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.Bestellung.Item;
import ch.elexis.data.PersistentObject;

public class LagerView extends ViewPart implements DoubleClickListener, ISaveablePart2,
		IActivationListener {
	public static final String ID = "ch.elexis.LagerView"; //$NON-NLS-1$
	CommonViewer cv;
	ViewerConfigurer vc;
	ElexisEventListener eeli_article = new ElexisUiEventListenerImpl(Artikel.class,
		ElexisEvent.EVENT_RELOAD) {
		public void catchElexisEvent(ElexisEvent ev){
			cv.notify(CommonViewer.Message.update);
		}
	};
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		cv = new CommonViewer();
		vc = new ViewerConfigurer(new DefaultContentProvider(cv, Artikel.class) {
			@Override
			public Object[] getElements(Object inputElement){
				return LagerartikelUtil.getAllLagerartikel().toArray();
			}
			
		}, new LagerLabelProvider() {}, null, // new DefaultControlFieldProvider(cv,new
			// String[]{"Name","Lieferant"}),
			new ViewerConfigurer.DefaultButtonProvider(), new LagerWidgetProvider());
		cv.create(vc, parent, SWT.NONE, getViewSite());
		cv.getConfigurer().getContentProvider().startListening();
		cv.addDoubleClickListener(this);
		
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		
		contextMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				manager.add(new CheckInOrderedAction(cv.getViewerWidget()));
			}
		});
		
		cv.setContextMenu(contextMenu);
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	@Override
	public void setFocus(){
		// cv.getConfigurer().getControlFieldProvider().setFocus();
	}
	
	@Override
	public void dispose(){
		cv.getConfigurer().getContentProvider().stopListening();
		cv.removeDoubleClickListener(this);
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	class LagerLabelProvider extends LabelProvider implements ITableLabelProvider,
			ITableColorProvider {
		
		public Image getColumnImage(Object element, int columnIndex){
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex){
			if (element instanceof Artikel) {
				Artikel art = (Artikel) element;
				switch (columnIndex) {
				case 0:
					return art.getPharmaCode();
				case 1:
					return art.getLabel();
				case 2:
					return Integer.toString(art.getIstbestand());
				case 3:
					return Integer.toString(art.getMinbestand());
				case 4:
					return Integer.toString(art.getMaxbestand());
				case 5:
					return Integer.toString(art.getIstbestand()); // TODO
					// Kontrolle
				case 6:
					return art.getLieferant().getLabel();
				default:
					return ""; //$NON-NLS-1$
				}
			} else {
				if (columnIndex == 0) {
					return element.toString();
				}
				return ""; //$NON-NLS-1$
				
			}
			
		}
		
		/**
		 * Lagerartikel are shown in blue, arrticles that should be ordered are shown in red
		 */
		public Color getForeground(Object element, int columnIndex){
			if (element instanceof Artikel) {
				Artikel art = (Artikel) element;
				
				if (art.isLagerartikel()) {
					int trigger =
						CoreHub.globalCfg.get(Preferences.INVENTORY_ORDER_TRIGGER,
							Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
					
					int ist = art.getIstbestand();
					int min = art.getMinbestand();
					
					boolean order = false;
					switch (trigger) {
					case Preferences.INVENTORY_ORDER_TRIGGER_BELOW:
						order = (ist < min);
						break;
					case Preferences.INVENTORY_ORDER_TRIGGER_EQUAL:
						order = (ist <= min);
						break;
					default:
						order = (ist < min);
					}
					
					if (order) {
						Boolean alreadyOrdered =
							art.getExt(Bestellung.ISORDERED).equalsIgnoreCase("true");
						if (alreadyOrdered)
							return UiDesk.getColor(UiDesk.COL_SKYBLUE);
						else
							return UiDesk.getColor(UiDesk.COL_RED);
					} else {
						return UiDesk.getColor(UiDesk.COL_BLUE);
					}
				}
			}
			
			return null;
		}
		
		public Color getBackground(Object element, int columnIndex){
			return null;
		}
	}
	
	class LagerWidgetProvider implements WidgetProvider {
		String[] columns = {
			Messages.LagerView_pharmacode, Messages.LagerView_name, Messages.LagerView_istBestand,
			Messages.LagerView_minBestand, Messages.LagerView_maxBestand,
			Messages.LagerView_controlled, Messages.LagerView_dealer
		};
		int[] colwidth = {
			60, 300, 40, 40, 40, 40, 200
		};
		
		public StructuredViewer createViewer(Composite parent){
			Table table = new Table(parent, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
			for (int i = 0; i < columns.length; i++) {
				TableColumn tc = new TableColumn(table, SWT.LEFT);
				tc.setText(columns[i]);
				tc.setWidth(colwidth[i]);
				tc.setData(i);
				tc.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						cv.getViewerWidget()
							.setSorter(
								new LagerTableSorter((Integer) ((TableColumn) e.getSource())
									.getData()));
					}
					
				});
				
			}
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			TableViewer ret = new TableViewer(table);
			ret.setSorter(new LagerTableSorter(1));
			return ret;
		}
		
		class LagerTableSorter extends ViewerSorter {
			int col;
			
			LagerTableSorter(int c){
				col = c;
			}
			
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				String s1 =
					((LagerLabelProvider) cv.getConfigurer().getLabelProvider()).getColumnText(e1,
						col).toLowerCase();
				String s2 =
					((LagerLabelProvider) cv.getConfigurer().getLabelProvider()).getColumnText(e2,
						col).toLowerCase();
				return s1.compareTo(s2);
			}
			
		}
	}
	
	public void doubleClicked(PersistentObject obj, CommonViewer cv){
		EditEigenartikelUi.executeWithParams(obj);
	}
	
	public void reloadContents(Class clazz){
		if (clazz.equals(Artikel.class)) {
			cv.notify(CommonViewer.Message.update);
		}
		
	}
	
	public static class CheckInOrderedAction extends Action {
		private Viewer viewer;
		private Artikel artikel;
		
		public CheckInOrderedAction(Viewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public boolean isEnabled(){
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()
				&& selection.getFirstElement() instanceof Artikel) {
				Artikel selectedArtikel = (Artikel) selection.getFirstElement();
				if (selectedArtikel.getExt(Bestellung.ISORDERED).equalsIgnoreCase("true")) {
					artikel = selectedArtikel;
					return true;
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
			Bestellung bestellung = Bestellung.lookupLastWithArticle(artikel);
			if (bestellung != null) {
				List<Item> orderedItem = new ArrayList<Bestellung.Item>();
				List<Item> items = bestellung.asList();
				for (Item item : items) {
					if (item.art.getId().equals(artikel.getId()))
						orderedItem.add(item);
				}
				OrderImportDialog dialog =
					new OrderImportDialog(viewer.getControl().getShell(), orderedItem);
				dialog.open();
				viewer.refresh();
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
	
	public void activation(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	public void visible(boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_article);
			eeli_article.catchElexisEvent(new ElexisEvent(null, Artikel.class,
				ElexisEvent.EVENT_RELOAD));
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_article);
		}
		
	}
}
