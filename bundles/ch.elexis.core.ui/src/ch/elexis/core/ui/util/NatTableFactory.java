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
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.resize.AutoResizeRowPaintListener;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Factory class for creating {@link NatTableWrapper} elements on a
 * {@link Composite}.
 *
 * @author thomas
 *
 */
public class NatTableFactory {

	/**
	 * Create a single column {@link NatTableWrapper}. The {@link IRowDataProvider}
	 * parameter is not optional, the {@link AbstractRegistryConfiguration} is
	 * optional.
	 *
	 * @param parent
	 * @param dataProvider
	 * @param customConfiguration
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static NatTableWrapper createSingleColumnTable(Composite parent,
			IRowDataProvider<? extends Object> dataProvider, AbstractRegistryConfiguration customConfiguration) {

		NatTableWrapper natTableWrapper = new NatTableWrapper();

		DataLayer bodyDataLayer = new DataLayer(dataProvider);
		// disable drawing cells lines
		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer) {
			private CellLayerPainter painter = new CellLayerPainter();

			@Override
			public ILayerPainter getLayerPainter() {
				return painter;
			}
		};
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
		viewportLayer.setRegionName(GridRegion.BODY);

		NatTable natTable = new NatTable(parent, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, viewportLayer, false);
		natTable.setBackground(natTable.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		natTable.addConfiguration(new DefaultSingleColumnStyleConfiguration());
		if (customConfiguration != null) {
			natTable.addConfiguration(customConfiguration);
		}
		AutoResizeRowPaintListener resizeRowPaintListener = new AutoResizeRowPaintListener(natTable, viewportLayer,
				bodyDataLayer);
		// register the AutoResizeRowPaintListener for lazy auto row resize
		natTable.addPaintListener(resizeRowPaintListener);

		natTableWrapper.setNatTable(natTable);
		natTableWrapper.setDataProvider((IRowDataProvider<Object>) dataProvider);
		natTableWrapper.setSelectionLayer(selectionLayer);

		natTableWrapper.configure();
		// workaround for setting column with to 100% as this is currently broken due to
		// a SWT update of Elexis
		// TODO revert after NatTable / Target update for Elexis 3.3
		// bodyDataLayer.setColumnPercentageSizing(true);
		// bodyDataLayer.setColumnWidthPercentageByPosition(0, 100);
		natTable.addControlListener(new ResizeColumnListener(bodyDataLayer));

		return natTableWrapper;
	}

	private static class ResizeColumnListener implements ControlListener {

		private DataLayer bodyDataLayer;

		public ResizeColumnListener(DataLayer bodyDataLayer) {
			this.bodyDataLayer = bodyDataLayer;
		}

		@Override
		public void controlMoved(ControlEvent e) {
			// do nothing

		}

		@Override
		public void controlResized(ControlEvent e) {
			if (e.widget instanceof NatTable) {

				this.bodyDataLayer.setColumnWidthByPosition(0,
						(((NatTable) e.widget).getBounds().width > 25 ? ((NatTable) e.widget).getBounds().width - 25
								: ((NatTable) e.widget).getBounds().width));
			}
		}
	}

	public static class DefaultSingleColumnStyleConfiguration extends DefaultNatTableStyleConfiguration {

		private Style selectionStyle = new Style();

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			hAlign = HorizontalAlignmentEnum.LEFT;
			super.configureRegistry(configRegistry);

			selectionStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
					Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			selectionStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
			selectionStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle, DisplayMode.SELECT,
					SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle, DisplayMode.SELECT);

			cellPainter = new RichTextCellPainter(true, false, true);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
					new BackgroundPainter(cellPainter));
		}
	}
}
