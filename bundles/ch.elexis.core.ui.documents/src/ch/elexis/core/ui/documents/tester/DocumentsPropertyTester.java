package ch.elexis.core.ui.documents.tester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.TreeSelection;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.holder.ConfigServiceHolder;

public class DocumentsPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isMimeType".equals(property) && args != null && args.length == 1 //$NON-NLS-1$
				&& args[0] instanceof String) {
			if (receiver instanceof TreeSelection) {
				receiver = ((TreeSelection) receiver).getFirstElement();
			}
			if (receiver instanceof List) {
				for (Object object : (List<?>) receiver) {
					return testIsMimeType(object, (String) args[0]);
				}
			} else {
				return testIsMimeType(receiver, (String) args[0]);
			}
		} else if ("isExternFile".equals(property)) { //$NON-NLS-1$
			if (receiver instanceof TreeSelection) {
				receiver = ((TreeSelection) receiver).getFirstElement();
			}
			if (receiver instanceof List) {
				for (Object object : (List<?>) receiver) {
					return testIsExternFile(object);
				}
			} else {
				return testIsExternFile(receiver);
			}
		}
		return false;
	}

	private boolean testIsMimeType(Object receiver, String mime) {
		if (receiver instanceof IDocument) {
			IDocument document = (IDocument) receiver;
			return document.getMimeType().toLowerCase().endsWith(mime);
		}
		return false;
	}

	private boolean testIsExternFile(Object receiver) {
		if (receiver instanceof IDocument) {
			IDocument document = (IDocument) receiver;
			if ("ch.elexis.data.store.brief".equals(document.getStoreId())) { //$NON-NLS-1$
				return ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false);
			} else if ("ch.elexis.data.store.omnivore".equals(document.getStoreId())) { //$NON-NLS-1$
				return isOmnivoreStoreInFilesystem();
			}
		}
		return false;
	}

	private boolean isOmnivoreStoreInFilesystem() {
		if (ConfigServiceHolder.getGlobal("ch.elexis.omnivore/store_in_fs_global", false)) { //$NON-NLS-1$
			return ConfigServiceHolder.getGlobal("ch.elexis.omnivore/store_in_fs", false); //$NON-NLS-1$
		} else {
			return ConfigServiceHolder.getLocal("ch.elexis.omnivore/store_in_fs", false); //$NON-NLS-1$
		}
	}
}
