package ch.elexis.core.ui.util;

import java.time.LocalDate;
import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;

import ch.elexis.core.model.ICoverage;

public class CoverageComparator implements Comparator<Object>
{
	@Override
	public int compare(Object o1, Object o2) {
		int comp = 0;
		if (o1 instanceof ICoverage && o2 instanceof ICoverage) {
			ICoverage coverage1 = (ICoverage) o1;
			ICoverage coverage2 = (ICoverage) o2;
			// compare gesetz
			boolean is1Closed = !coverage1.isOpen();
			boolean is2Closed = !coverage2.isOpen();
			comp = ObjectUtils.compare(is1Closed, is2Closed);

			if (comp == 0) {
				comp =
					ObjectUtils.compare(coverage1.getBillingSystem(), coverage2.getBillingSystem());
				if (comp == 0) {
					// compare beginn date
					LocalDate t1 = coverage1.getDateFrom();
					LocalDate t2 = coverage2.getDateFrom();
					if (t1 != null && t2 != null) {
						comp = t1.isEqual(t2) ? 0 : (t1.isBefore(t2) ? 1 : -1);
						if (comp == 0) {
							comp = ObjectUtils.compare(coverage1.getDescription(),
								coverage2.getDescription());
							if (comp == 0) {
								comp = ObjectUtils.compare(coverage1.getId(), coverage2.getId());
							}
						}
					}
				}
			}
		}
		return comp;
	}
}