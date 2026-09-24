package ch.elexis.core.ui.mediorder.internal;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mediorder.MediorderHistoryRef;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;

/**
 * Turns the billing text of a medication order into a link. Clicking it opens
 * the history of the billed order in a {@link MediorderHistoryDialog}.
 */
public class MediorderKonsExtension implements IKonsExtension {

	@Override
	public String connect(IRichTextDisplay tf) {
		return XRefExtensionConstants.providerMediorderID;
	}

	@Override
	public boolean doLayout(StyleRange styleRange, String provider, String id) {
		if (styleRange != null) {
			styleRange.foreground = UiDesk.getColor(UiDesk.COL_BLUE);
			styleRange.underline = true;
		}
		return true;
	}

	@Override
	public boolean doXRef(String refProvider, String refID) {
		Optional<MediorderHistoryRef> reference = MediorderHistoryRef.decode(refID);
		if (reference.isEmpty()) {
			return false;
		}
		Optional<IPatient> patient = CoreModelServiceHolder.get().load(reference.get().patientId(), IPatient.class);
		if (patient.isEmpty()) {
			LoggerFactory.getLogger(getClass()).warn("Patient {} of the order history is not available.",
					reference.get().patientId());
			return false;
		}
		new MediorderHistoryDialog(Display.getDefault().getActiveShell(), patient.get(), reference.get().blobId())
				.open();
		return true;
	}

	@Override
	public IAction[] getActions() {
		return null;
	}

	@Override
	public void insert(Object o, int pos) {
	}

	@Override
	public void removeXRef(String refProvider, String refID) {
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
	}
}
