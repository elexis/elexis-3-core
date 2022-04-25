package ch.elexis.core.findings.templates.ui.dlg;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ch.elexis.core.findings.ILocalCoding;

public class LocalCodeViewerComparator extends ViewerComparator {

	private LabelProvider labelProvider;

	public LocalCodeViewerComparator(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ILocalCoding && e2 instanceof ILocalCoding) {
			String l1 = labelProvider.getText(e1);
			String l2 = labelProvider.getText(e2);
			return l1.compareTo(l2);
		}
		return super.compare(viewer, e1, e2);
	}
}
