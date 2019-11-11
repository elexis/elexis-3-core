package ch.elexis.core.ui.util;

import java.time.LocalDate;
import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;

import ch.elexis.core.model.ICoverage;

/**
 * Comparator used to sort {@link ICoverage} objects for ui representation.
 * 
 */
public class FallComparator implements Comparator<Object>
{
	@Override
	public int compare(Object o1, Object o2) {
		int comp = 0;
		if (o1 instanceof ICoverage && o2 instanceof ICoverage) {
			ICoverage f1 = (ICoverage) o1;
			ICoverage f2 = (ICoverage) o2;
			// compare gesetz
			boolean isFall1Closed = !f1.isOpen();
			boolean isFall2Closed = !f2.isOpen();
			comp = ObjectUtils.compare(isFall1Closed, isFall2Closed);

			if (comp == 0) {
				comp = ObjectUtils.compare(f1.getBillingSystem().getName(), f2.getBillingSystem().getName());
				if (comp == 0) {
					LocalDate f1DateFrom = f1.getDateFrom();
					LocalDate f2DateFrom = f2.getDateFrom();
					if (f1DateFrom != null && f2DateFrom != null) {
						// compare beginn date
						comp = f1.getDateFrom().compareTo(f2.getDateFrom());
						if (comp == 0) {
							comp = ObjectUtils.compare(f1.getDescription(), f2.getDescription());
							if (comp == 0) {
								comp = ObjectUtils.compare(f1.getId(), f2.getId());
							}
						}
					} else {
						return f1.getLastupdate().compareTo(f2.getLastupdate());
					}
				}
			}
		}
		return comp;
	}
}