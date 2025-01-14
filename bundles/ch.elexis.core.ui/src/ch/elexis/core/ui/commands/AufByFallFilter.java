package ch.elexis.core.ui.commands;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ISickCertificate;

public class AufByFallFilter extends ViewerFilter {
	private String fallID;
	private boolean isActive = true;

	public void setFallID(String fallID) {
		this.fallID = fallID;
	}

	public void resetFallID() {
		this.fallID = null;
	}

	public void toggleFilter() {
		isActive = !isActive;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!isActive || fallID == null) {
			return true;
		}
		if (element instanceof ISickCertificate) {
			ISickCertificate certificate = (ISickCertificate) element;
			ICoverage coverage = certificate.getCoverage();
			return coverage != null && fallID.equals(coverage.getId());
		}
		return false;
	}

	public void setFilterActive(boolean isActive) {
		this.isActive = isActive;
	}
}
