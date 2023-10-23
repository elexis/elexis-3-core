package ch.elexis.core.ui.property;

import java.util.Optional;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.BriefExternUtil;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.util.DocumentLetterUtil;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	public PropertyTester() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("documenteditlocal".equals(property)) { //$NON-NLS-1$
			// no local copy / local edit if brief extern
			if (ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false) && BriefExternUtil
					.isValidExternPath(DocumentLetterUtil.getOperatingSystemSpecificExternalStoragePath(), false)) {
				return false;
			}
			return CoreHub.localCfg.get(Preferences.P_TEXT_EDIT_LOCAL, false);
		} else if ("billable".equals(property)) { //$NON-NLS-1$
			if (args != null && args.length == 1) {
				if ("activeencounter".equals(args[0])) { //$NON-NLS-1$
					Optional<IEncounter> selectedEncounter = ContextServiceHolder.get().getTyped(IEncounter.class);
					if (selectedEncounter.isPresent() && selectedEncounter.get().isBillable()) {
						return selectedEncounter.get().getInvoice() == null
								|| (selectedEncounter.get().getInvoice().getState() == InvoiceState.CANCELLED
										|| selectedEncounter.get().getInvoice().getState() == InvoiceState.DEPRECIATED);
					}
				}
			}
		}
		return false;
	}

}
