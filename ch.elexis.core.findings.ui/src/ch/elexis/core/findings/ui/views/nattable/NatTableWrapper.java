/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings.ui.views.nattable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Class for managing a {@link NatTable} with a {@link IRowDataProvider} and a
 * {@link SelectionLayer}.
 * 
 * @author thomas
 *
 */
public class NatTableWrapper implements ISelectionProvider {
	private NatTable natTable;
	
	private IDataProvider dataProvider;
	private SelectionLayer selectionLayer;
	
	private List<Object> currentSelection = new ArrayList<>();
	
	private ListenerList<IDoubleClickListener> doubleClickListeners = new ListenerList<>();
	private ListenerList<ISelectionChangedListener> selectionListener = new ListenerList<>();
	
	public NatTableWrapper(NatTable natTable, IDataProvider dataProvider,
		SelectionLayer selectionLayer){
		this.natTable = natTable;
		this.dataProvider = dataProvider;
		this.selectionLayer = selectionLayer;
	}
	
	public SelectionLayer getSelectionLayer(){
		return selectionLayer;
	}
	
	public IDataProvider getDataProvider(){
		return dataProvider;
	}
	
	public boolean isDisposed(){
		return natTable == null || natTable.isDisposed();
	}
	
	public void configure(){
		natTable.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(ILayerEvent event){
				if (event instanceof CellSelectionEvent) {
					currentSelection.clear();
					CellSelectionEvent cellEvent = (CellSelectionEvent) event;
					Collection<ILayerCell> cells = cellEvent.getSelectionLayer().getSelectedCells();
					for (ILayerCell iLayerCell : cells) {
						Object selectedObj = dataProvider.getDataValue(iLayerCell.getColumnIndex(),
							iLayerCell.getRowIndex());
						if (selectedObj != null) {
							currentSelection.add(selectedObj);
						}
					}
					// call listeners
					Object[] listeners = selectionListener.getListeners();
					for (Object object : listeners) {
						((ISelectionChangedListener) object)
							.selectionChanged(new SelectionChangedEvent(NatTableWrapper.this,
								new StructuredSelection(currentSelection)));
					}
				}
			}
		});
		
		natTable.addConfiguration(new AbstractUiBindingConfiguration() {
			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry){
				uiBindingRegistry.registerDoubleClickBinding(
					new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON),
					new DblClickMouseAction());
			}
			
			class DblClickMouseAction implements IMouseAction {
				@Override
				public void run(NatTable natTable, MouseEvent event){
					if (currentSelection != null && currentSelection.size() == 1) {
						Object[] listeners = doubleClickListeners.getListeners();
						for (Object object : listeners) {
							((IDoubleClickListener) object).doubleClick(NatTableWrapper.this,
								getSelection());
						}
					}
				}
			}
		});
		natTable.configure();
	}
	
	public NatTable getNatTable(){
		return natTable;
	}
	
	public void addDoubleClickListener(IDoubleClickListener listener){
		doubleClickListeners.add(listener);
	}
	
	public void removeDoubleClickListener(IDoubleClickListener listener){
		doubleClickListeners.remove(listener);
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionListener.add(listener);
	}
	
	@Override
	public ISelection getSelection(){
		if (!currentSelection.isEmpty()) {
			return new StructuredSelection(currentSelection);
		}
		return StructuredSelection.EMPTY;
	}
	
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionListener.remove(listener);
	}
	
	@Override
	public void setSelection(ISelection selection){
		// currently not supported 
	}
	
	public static interface IDoubleClickListener {
		public void doubleClick(NatTableWrapper source, ISelection selection);
	}
	
	public void addContextMenu(String string, IWorkbenchPartSite iWorkbenchPartSite){
		
		MenuManager mgr = new MenuManager();
		Menu popupmenu = new PopupMenuBuilder(natTable, mgr).build();
		iWorkbenchPartSite.registerContextMenu(string, mgr, null);
		
		natTable.addConfiguration(new AbstractUiBindingConfiguration() {
			
			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry){
				uiBindingRegistry.registerMouseDownBinding(
					new MouseEventMatcher(SWT.NONE, null, MouseEventMatcher.RIGHT_BUTTON),
					new PopupMenuAction(popupmenu));
			}
		});
	}
}
