package ch.elexis.core.ui.processor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.services.IAccessControlService;

public class AccessControlProcessor {

	@Inject
	private IAccessControlService accessControlService;

	@Execute
	public void execute(MApplication mApplication, EModelService eModelService) {

		updateDescriptors(mApplication, eModelService);
		updatePlaceholders(mApplication, eModelService);
		updateParts(mApplication, eModelService);
	}

	private void updateDescriptors(MApplication mApplication, EModelService eModelService) {
		for (MPartDescriptor foundPartDescriptor : new ArrayList<>(mApplication.getDescriptors())) {
			List<EvaluatableACE> aces = getAccessControlTags(foundPartDescriptor.getTags());
			for (EvaluatableACE ace : aces) {
				if (!accessControlService.evaluate(ace)) {
					mApplication.getDescriptors().remove(foundPartDescriptor);
				}
			}
		}
	}

	private void updatePlaceholders(MApplication mApplication, EModelService eModelService) {
		List<MPlaceholder> foundPlaceholders = eModelService.findElements(mApplication, null, MPlaceholder.class, null);
		for (MPlaceholder placeholder : foundPlaceholders) {
			List<EvaluatableACE> aces = getAccessControlTags(placeholder.getTags());
			for (EvaluatableACE ace : aces) {
				if (!accessControlService.evaluate(ace)) {
					placeholder.setVisible(false);
					placeholder.setToBeRendered(false);
				}
			}
		}
	}

	private void updateParts(MApplication mApplication, EModelService eModelService) {
		List<MPart> foundParts = eModelService.findElements(mApplication, null, MPart.class, null);
		for (MPart mPart : foundParts) {
			List<EvaluatableACE> aces = getAccessControlTags(mPart.getTags());
			for (EvaluatableACE ace : aces) {
				if (!accessControlService.evaluate(ace)) {
					mPart.setVisible(false);
					mPart.setToBeRendered(false);
				}
			}
		}
	}

	private List<EvaluatableACE> getAccessControlTags(List<String> tags) {
		List<EvaluatableACE> ret = new ArrayList<EvaluatableACE>();
		for (String tag : tags) {
			if (tag.startsWith("accesscontrol:")) {
				ret.add(new ObjectEvaluatableACE(tag.substring("accesscontrol:".length()), Right.VIEW));
			}
		}
		return ret;
	}
}
