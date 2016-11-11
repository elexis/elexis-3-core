/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views.artikel;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.stock.IStockService.Availability;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Artikel;

public class ArtikelLabelProvider extends DefaultLabelProvider implements ITableColorProvider {
	
	@Override
	public Image getColumnImage(Object element, int columnIndex){
		if (element instanceof Artikel) {
			return null;
		} else {
			return Images.IMG_ACHTUNG.getImage();
		}
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex){
		if (element instanceof Artikel) {
			Artikel art = (Artikel) element;
			String ret = art.getInternalName();
			Integer amount = CoreHub.getStockService().getCumulatedStockForArticle(art);
			if (amount != null) {
				ret += " (" + Integer.toString(amount) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return ret;
		}
		return super.getColumnText(element, columnIndex);
	}
	
	/**
	 * Lagerartikel are shown in blue, articles that should be ordered are shown in red
	 */
	public Color getForeground(Object element, int columnIndex){
		if (element instanceof Artikel) {
			Artikel art = (Artikel) element;
			Availability availability =
				CoreHub.getStockService().getCumulatedAvailabilityForArticle(art);
			if (availability != null) {
				switch (availability) {
				case CRITICAL_STOCK:
				case OUT_OF_STOCK:
					return UiDesk.getColor(UiDesk.COL_RED);
				default:
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
