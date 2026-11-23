package ch.elexis.core.ui.mediorder.internal;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mediorder.MediorderHistoryRef;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;

/**
 * Turns the billing text of a medication order into a link. Clicking it opens
 * the {@link MediorderPart} with the patient selected and the history of the
 * billed order shown.
 */
public class MediorderKonsExtension implements IKonsExtension {

	@Override
	public String connect(IRichTextDisplay tf) {
		return XRefExtensionConstants.providerMediorderID;
	}

	@Override
	public boolean doLayout(StyleRange styleRange, String provider, String id) {
		if (styleRange != null) {
			styleRange.foreground = UiDesk.getColorFromRGB("FF8C00"); //$NON-NLS-1$
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
			LoggerFactory.getLogger(getClass()).warn("Patient {} of the order history is not available.", //$NON-NLS-1$
					reference.get().patientId());
			return false;
		}
		Optional<MediorderPart> mediorderPart = showMediorderPart();
		if (mediorderPart.isEmpty()) {
			return false;
		}
		Display.getDefault()
				.asyncExec(() -> mediorderPart.get().showHistory(patient.get(), reference.get().blobId()));
		return true;
	}

	private Optional<MediorderPart> showMediorderPart() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		EPartService partService = window != null ? window.getService(EPartService.class) : null;
		if (partService == null) {
			LoggerFactory.getLogger(getClass()).warn("No part service available to show the mediorder part."); //$NON-NLS-1$
			return Optional.empty();
		}
		MPart part = partService.showPart(MediorderPart.PART_ID, PartState.ACTIVATE);
		if (part != null && part.getObject() instanceof MediorderPart mediorderPart) {
			return Optional.of(mediorderPart);
		}
		LoggerFactory.getLogger(getClass()).warn("Mediorder part {} could not be shown.", MediorderPart.PART_ID); //$NON-NLS-1$
		return Optional.empty();
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
