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
package ch.elexis.core.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Class for managing a {@link NatTable} with a {@link IRowDataProvider} and a
 * {@link SelectionLayer}.
 * 
 * @author thomas
 *
 */
public class NatTableWrapper implements ISelectionProvider {
	private NatTable natTable;
	
	private IRowDataProvider<Object> dataProvider;
	private SelectionLayer selectionLayer;
	
	private List<Object> currentSelection = new ArrayList<>();
	private List<Integer> rowHeights = new ArrayList<>();
	
	private ListenerList doubleClickListeners = new ListenerList();
	private ListenerList selectionListener = new ListenerList();
	
	protected void setSelectionLayer(SelectionLayer selectionLayer){
		this.selectionLayer = selectionLayer;
	}
	
	public SelectionLayer getSelectionLayer(){
		return selectionLayer;
	}
	
	protected void setDataProvider(IRowDataProvider<Object> dataProvider){
		this.dataProvider = dataProvider;
	}
	
	public IRowDataProvider<?> getDataProvider(){
		return dataProvider;
	}
	
	public boolean isDisposed(){
		return natTable == null || natTable.isDisposed();
	}
	
	protected void setNatTable(NatTable natTable){
		this.natTable = natTable;
	}
	
	public void configure(){
		natTable.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(ILayerEvent event){
				if (event instanceof CellSelectionEvent) {
					currentSelection.clear();
					CellSelectionEvent cellEvent = (CellSelectionEvent) event;
					int[] selectedIdxs =
						cellEvent.getSelectionLayer().getFullySelectedRowPositions();
					for (int selectionIdx : selectedIdxs) {
						currentSelection.add(dataProvider.getRowObject(selectionIdx));
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
		if (selection instanceof StructuredSelection) {
			if(!selection.isEmpty()) {
				List<?> list = ((StructuredSelection) selection).toList();
				int[] rowIdx = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					rowIdx[i] = dataProvider.indexOfRowObject(list.get(i));
				}
				natTable.doCommand(new SelectRowsCommand(selectionLayer, 0, rowIdx, false, false, rowIdx[0]));
			} else {
				natTable.doCommand(new ClearAllSelectionsCommand());
			}
		}
	}
	
	public static interface IDoubleClickListener {
		public void doubleClick(NatTableWrapper source, ISelection selection);
	}
	
	public Point computeSize(int wHint, int hHint){
		Point ret = natTable.computeSize(wHint, hHint);
		int calcHeight = calculateHeight();
		if (calcHeight > ret.y) {
			ret.y = calcHeight;
		}
		return ret;
	}
	
	private int calculateHeight(){
		int rows = dataProvider.getRowCount();
		ILayerCell lastCell = natTable.getCellByPosition(0, rows - 1);
		if(lastCell != null) {
			Rectangle lastRowBounds = lastCell.getBounds();
			return lastRowBounds.y + lastRowBounds.height;
		}
		return 0;
	}
}
