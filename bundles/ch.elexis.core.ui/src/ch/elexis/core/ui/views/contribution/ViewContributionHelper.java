package ch.elexis.core.ui.views.contribution;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 3.2
 */
public class ViewContributionHelper {
	
	public static Comparator<IViewContribution> sortByContributionPositionOrder =
		(e1, e2) -> Integer.compare(e1.getContributionPositionOrder(),
			e2.getContributionPositionOrder());
	
	/**
	 * Filter the list of {@link IViewContribution} elements according to a given position, and sort
	 * it considering the contribution position order
	 * 
	 * @param detailComposites
	 * @param i
	 * @return
	 */
	public static List<IViewContribution> getFilteredAndPositionSortedContributions(
		List<IViewContribution> detailComposites, int i){
		if (detailComposites != null && detailComposites.size() > 0) {
			return detailComposites.stream()
				.filter(p -> p.getContributionPosition() == i && p.isAvailable())
				.sorted(sortByContributionPositionOrder).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
}
