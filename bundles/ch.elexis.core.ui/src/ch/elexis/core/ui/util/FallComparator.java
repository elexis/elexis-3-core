package ch.elexis.core.ui.util;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;

import ch.elexis.core.model.ICoverage;
import ch.rgw.tools.TimeTool;

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
				comp = ObjectUtils.compare(f1.getBillingSystem().getLaw().name(), f2.getBillingSystem().getLaw().name());
				if (comp == 0) {
					// compare beginn date
					TimeTool t1 = new TimeTool(f1.getDateFrom());
					TimeTool t2 = new TimeTool(f2.getDateFrom());
					comp = t1.isEqual(t2) ? 0 : (t1.isBefore(t2) ? 1 : -1);
					if (comp == 0) {
						comp = ObjectUtils.compare(f1.getDescription(), f2.getDescription());
						if (comp == 0) {
							comp = ObjectUtils.compare(f1.getId(), f2.getId());
						}
					}
				}
			}
		}
		return comp;
	}
}