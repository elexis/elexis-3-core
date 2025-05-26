package ch.elexis.core.ui.mediorder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.ICoverageService;

public class MediorderCanExecuteUtil {

	public static boolean canExecute(List<IStockEntry> stockEntries, ICoverageService coverageService) {
		boolean hasInStock = stockEntries.stream()
				.anyMatch(e -> MediorderUtil.determineState(e).equals(MediorderEntryState.IN_STOCK));
		if (!hasInStock) {
			return false;
		}

		Optional<ICoverage> coverage = coverageService
				.getLatestOpenCoverage(stockEntries.get(0).getStock().getOwner().asIPatient());
		if (coverage.isEmpty()) {
			return false;
		}

		Optional<IEncounter> encounter = coverageService.getLatestEncounter(coverage.get());
		if (encounter.isEmpty()) {
			return false;
		}

		Set<String> billedIds = encounter.get().getBilled().stream().map(billed -> billed.getBillable().getId())
				.collect(Collectors.toSet());
		return stockEntries.stream().allMatch(e -> billedIds.contains(e.getArticle().getId()));
	}
}
