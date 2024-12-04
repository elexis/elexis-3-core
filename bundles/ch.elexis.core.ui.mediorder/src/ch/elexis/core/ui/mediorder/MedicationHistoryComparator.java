package ch.elexis.core.ui.mediorder;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ch.elexis.core.model.IOrderEntry;

public class MedicationHistoryComparator extends ViewerComparator {
	private int propertyIndex;
	private int direction;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	public MedicationHistoryComparator() {
		this.propertyIndex = 0;
		this.direction = -1;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			direction *= -1;
		}
		this.propertyIndex = column;
	}

	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		IOrderEntry orderEntry1 = (IOrderEntry) o1;
		IOrderEntry orderEntry2 = (IOrderEntry) o2;

		switch (propertyIndex) {
		case 0:
			String articleName1 = orderEntry1.getArticle().getName();
			String articleName2 = orderEntry2.getArticle().getName();
			return Objects.compare(articleName1, articleName2, Comparator.nullsFirst(Comparator.naturalOrder()))
					* direction;
		case 1:
			String orderAmount1 = String.valueOf(orderEntry1.getAmount());
			String orderAmount2 = String.valueOf(orderEntry2.getAmount());
			return Objects.compare(orderAmount1, orderAmount2, Comparator.nullsFirst(Comparator.naturalOrder()))
					* direction;
		case 2:
			String orderDate1 = orderEntry1.getOrder().getTimestamp().format(dateFormatter);
			String orderDate2 = orderEntry2.getOrder().getTimestamp().format(dateFormatter);
			return Objects.compare(orderDate1, orderDate2, Comparator.nullsFirst(Comparator.naturalOrder()))
					* direction;
		}

		return super.compare(viewer, o1, o2);
	}
}
