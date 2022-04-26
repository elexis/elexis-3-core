package ch.elexis.core.ui.documents.tester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.TreeSelection;

import ch.elexis.core.model.IDocument;

public class DocumentsPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isMimeType".equals(property) && args != null && args.length == 1 //$NON-NLS-1$
				&& args[0] instanceof String) {
			if (receiver instanceof TreeSelection) {
				receiver = ((TreeSelection) receiver).getFirstElement();
			}
			if (receiver instanceof IDocument) {
				IDocument document = (IDocument) receiver;
				return document.getMimeType().toLowerCase().endsWith((String) args[0]);
			}
		}
		return false;
	}
}
