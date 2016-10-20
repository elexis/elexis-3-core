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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Factory class for creating {@link NatTableWrapper} elements on a {@link Composite}.
 * 
 * @author thomas
 *
 */
public class NatTableFactory {
	
	/**
	 * Create a single column {@link NatTableWrapper}. The {@link IRowDataProvider} parameter is not
	 * optional, the {@link AbstractRegistryConfiguration} is optional.
	 * 
	 * @param parent
	 * @param dataProvider
	 * @param customConfiguration
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static NatTableWrapper createSingleColumnTable(Composite parent,
		IRowDataProvider<? extends Object> dataProvider,
		AbstractRegistryConfiguration customConfiguration){
		
		NatTableWrapper natTableWrapper = new NatTableWrapper();
		
		DataLayer bodyDataLayer = new DataLayer(dataProvider);
		bodyDataLayer.setColumnPercentageSizing(true);
		bodyDataLayer.setColumnWidthPercentageByPosition(0, 100);
		// disable drawing cells lines
		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer) {
			private CellLayerPainter painter = new CellLayerPainter();
			
			@Override
			public ILayerPainter getLayerPainter(){
				return painter;
			}
		};
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
		viewportLayer.setRegionName(GridRegion.BODY);
		
		NatTable natTable =
			new NatTable(parent, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, viewportLayer, false);
		natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		natTable.addConfiguration(new DefaultSingleColumnStyleConfiguration());
		if (customConfiguration != null) {
			natTable.addConfiguration(customConfiguration);
		}
		
		natTableWrapper.setNatTable(natTable);
		natTableWrapper.setDataProvider((IRowDataProvider<Object>) dataProvider);
		natTableWrapper.setSelectionLayer(selectionLayer);
		
		natTableWrapper.configure();
		
		return natTableWrapper;
	}
	
	public static class DefaultSingleColumnStyleConfiguration
			extends DefaultNatTableStyleConfiguration {
		
		private Style selectionStyle = new Style();
		
		@Override
		public void configureRegistry(IConfigRegistry configRegistry){
			hAlign = HorizontalAlignmentEnum.LEFT;
			super.configureRegistry(configRegistry);
			
			selectionStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			selectionStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
			selectionStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
			selectionStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle,
				DisplayMode.SELECT, "selectionAnchor");
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle,
				DisplayMode.SELECT);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
				new NatTableCustomCellPainter() {
					@Override
					public void paintCell(ILayerCell cell, GC gc, Rectangle bounds,
						IConfigRegistry cellConfigRegistry){
						int preferredHeight = getPreferredHeight(cell, gc, cellConfigRegistry);
						if (preferredHeight != bounds.height && preferredHeight != bounds.height + 1
							&& preferredHeight != bounds.height - 1) {
							ILayer layer = cell.getLayer();
							if (layer != null) {
								cell.getLayer().doCommand(new RowResizeCommand(layer,
									cell.getRowPosition(), preferredHeight));
							}
						}
						super.paintCell(cell, gc, bounds, cellConfigRegistry);
					}
				});
		}
	}
}
