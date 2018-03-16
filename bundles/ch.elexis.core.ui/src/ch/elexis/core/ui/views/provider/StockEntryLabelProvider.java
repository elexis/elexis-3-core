package ch.elexis.core.ui.views.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.service.StockService;
import ch.elexis.core.services.IStockService.Availability;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.data.Artikel;
import ch.elexis.data.Mandant;
import ch.elexis.data.StockEntry;

public class StockEntryLabelProvider extends LabelProvider
		implements ITableLabelProvider, ITableColorProvider {
	
	public Image getColumnImage(Object element, int columnIndex){
		return null;
	}
	
	public String getColumnText(Object element, int columnIndex){
		if (element instanceof StockEntry) {
			StockEntry se = (StockEntry) element;
			Artikel article = se.getArticle();
			switch (columnIndex) {
			case 0:
				return se.getStock().getCode();
			case 1:
				return (article != null) ? article.getPharmaCode() : "";
			case 2:
				return (article != null) ? article.getEAN() : "";
			case 3:
				return (article != null) ? article.getLabel() : "";
			case 4:
				return Integer.toString(se.getMinimumStock());
			case 5:
				return Integer.toString(se.getCurrentStock());
			case 6:
				return Integer.toString(se.getMaximumStock());
			case 7:
				return (se.getProvider() != null) ? se.getProvider().getLabel()
						: StringConstants.EMPTY;
			default:
				return StringConstants.EMPTY;
			}
		} else if (element instanceof String) {
			if (columnIndex == 3) {
				return (String) element;
			}
		}
		return null;
	}
	
	/**
	 * Lagerartikel are shown in blue, articles that should be ordered are shown in red
	 */
	public Color getForeground(Object element, int columnIndex){
		if (element instanceof StockEntry) {
			StockEntry se = (StockEntry) element;
			
			Availability availability = StockService.determineAvailability(se);
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
		if (element instanceof StockEntry) {
			StockEntry se = (StockEntry) element;
			Mandant owner = se.getStock().getOwner();
			if (owner != null) {
				return UiMandant.getColorForMandator(owner);
			}
		}
		return null;
	}
	
	public static class ColumnStockEntryLabelProvider extends ColumnLabelProvider {
		private int index;
		private StockEntryLabelProvider labelProvider;
		
		public ColumnStockEntryLabelProvider(int index, StockEntryLabelProvider labelProvider){
			this.labelProvider = labelProvider;
			this.index = index;
		}
		
		@Override
		public String getText(Object element){
			return labelProvider.getColumnText(element, index);
		}
		
		@Override
		public Image getImage(Object element){
			return labelProvider.getColumnImage(element, index);
		}
		
		@Override
		public Color getForeground(Object element){
			return labelProvider.getForeground(element, index);
		}
		
		@Override
		public Color getBackground(Object element){
			return labelProvider.getBackground(element, index);
		}
	}
}
