package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.IStockService.Availability;

import ch.rgw.tools.StringTool;
public class EigenartikelTreeLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		IArticle ea = (IArticle) element;
		String name = ea.getName();
		if (!ea.isProduct()) {
			String label = StringTool.leer;
			label += ea.getPackageSize() + StringTool.space
				+ (ea.getPackageUnit() != null ? ea.getPackageUnit() : StringTool.leer);
			Availability availability =
				StockServiceHolder.get().getCumulatedAvailabilityForArticle(ea);
			if (availability != null) {
				label += " (" + availability.toString() + ")";
			}
			return name +StringTool.space +label;
		}
		return name;
	}
}
