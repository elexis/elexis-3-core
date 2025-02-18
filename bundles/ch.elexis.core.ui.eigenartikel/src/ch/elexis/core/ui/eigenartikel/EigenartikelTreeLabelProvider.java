package ch.elexis.core.ui.eigenartikel;

import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IStockService.Availability;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import jakarta.inject.Inject;

public class EigenartikelTreeLabelProvider extends LabelProvider implements IColorProvider {

	@Inject
	private IEclipseContext eclipseContext;

	public EigenartikelTreeLabelProvider() {
		// trigger injection of application context
		CoreUiUtil.injectServicesWithContext(this);
	}

	@Override
	public String getText(Object element) {
		Long availability = null;
		IArticle article = (IArticle) element;
		if (eclipseContext != null) {
			MPart mPart = eclipseContext.getActive(MPart.class);
			if (mPart != null && "ch.elexis.LeistungenView".equals(mPart.getElementId()) //$NON-NLS-1$
					&& ContextServiceHolder.get().getTyped(IEncounter.class).isPresent()) {
				availability = getAvailability(article,
						Optional.of(ContextServiceHolder.get().getTyped(IEncounter.class).get().getMandator()));
			} else {
				availability = getAvailability(article, ContextServiceHolder.get().getActiveMandator());
			}
		} else {
			availability = StockServiceHolder.get().getCumulatedStockForArticle(article);
		}
		if (availability != null) {
			return article.getLabel() + " (LB: " + availability + ")"; //$NON-NLS-2$
		}
		return article.getLabel();
	}

	private Long getAvailability(IArticle article, Optional<IMandator> mandator) {
		List<IStockEntry> stockEntries = StockServiceHolder.get()
				.findAllStockEntriesForArticle(StoreToStringServiceHolder.getStoreToString(article));
		if (!stockEntries.isEmpty()) {
			if (mandator.isPresent()) {
				return Long.valueOf(stockEntries.stream().filter(
						se -> (se.getStock().getOwner() == null || se.getStock().getOwner().equals(mandator.get())))
						.mapToInt(se -> se.getCurrentStock()).sum());
			} else {
				return Long.valueOf(stockEntries.stream().mapToInt(se -> se.getCurrentStock()).sum());
			}
		}
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		IArticle article = (IArticle) element;
		Availability availability = StockServiceHolder.get().getCumulatedAvailabilityForArticle(article);
		if (availability != null) {
			switch (availability) {
			case CRITICAL_STOCK:
			case OUT_OF_STOCK:
				return UiDesk.getColor(UiDesk.COL_RED);
			default:
				return UiDesk.getColor(UiDesk.COL_BLUE);
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		IArticle article = (IArticle) element;
		if (article.isProduct()) {
			return UiDesk.getColor(UiDesk.COL_SKYBLUE);
		}
		return null;
	}
}
