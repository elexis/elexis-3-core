package ch.elexis.core.ui.util;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;

import ch.elexis.data.Fall;
import ch.rgw.tools.TimeTool;

public class FallComparator implements Comparator<Object>
{
	@Override
	public int compare(Object o1, Object o2) {
		int comp = 0;
		if (o1 instanceof Fall && o2 instanceof Fall) {
			Fall f1 = (Fall) o1;
			Fall f2 = (Fall) o2;
			// compare gesetz
			boolean isFall1Closed = !f1.isOpen();
			boolean isFall2Closed = !f2.isOpen();
			comp = ObjectUtils.compare(isFall1Closed, isFall2Closed);

			if (comp == 0) {
				comp = ObjectUtils.compare(f1.getAbrechnungsSystem(), f2.getAbrechnungsSystem());
				if (comp == 0) {
					// compare beginn date
					TimeTool t1 = new TimeTool(f1.getBeginnDatum());
					TimeTool t2 = new TimeTool(f2.getBeginnDatum());
					comp = t1.isEqual(t2) ? 0 : (t1.isBefore(t2) ? 1 : -1);
					if (comp == 0) {
						comp = ObjectUtils.compare(f1.getBezeichnung(), f2.getBezeichnung());
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