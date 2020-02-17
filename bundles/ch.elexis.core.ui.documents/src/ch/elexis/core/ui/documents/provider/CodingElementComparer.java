package ch.elexis.core.ui.documents.provider;

import java.util.Objects;

import org.eclipse.jface.viewers.IElementComparer;

import ch.elexis.core.findings.ICoding;

public class CodingElementComparer implements IElementComparer {
	
	@Override
	public int hashCode(Object element){
		return Objects.hashCode(element);
	}
	
	@Override
	public boolean equals(Object a, Object b){
		if (a == b)
			return true;
		if (b == null)
			return false;
		if (a instanceof ICoding && b instanceof ICoding) {
			ICoding e1 = (ICoding) a;
			ICoding e2 = (ICoding) b;
			return Objects.equals(e1.getSystem(), e2.getSystem())
				&& Objects.equals(e1.getCode(), e2.getCode());
		}
		return Objects.equals(a, b);
	}
}
