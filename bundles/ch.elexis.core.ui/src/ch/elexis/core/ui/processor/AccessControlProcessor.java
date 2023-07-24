package ch.elexis.core.ui.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;

public class AccessControlProcessor {

	private Map<String, List<String>> viewAccessControlMap;

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
			List<String> acStrings = getAccessControlStrings(foundPartDescriptor);
			if (acStrings != null && !acStrings.isEmpty()) {
				List<EvaluatableACE> aces = getAccessControlEntries(acStrings);
				for (EvaluatableACE ace : aces) {
					if (!accessControlService.evaluate(ace)) {
						while (mApplication.getDescriptors().remove(foundPartDescriptor)) {
							// System.out.println("Remove [" + foundPartDescriptor + "]");
						}
					}
				}
			}
		}
	}

	private void updatePlaceholders(MApplication mApplication, EModelService eModelService) {
		List<MPlaceholder> foundPlaceholders = eModelService.findElements(mApplication, null, MPlaceholder.class, null);
		for (MPlaceholder placeholder : foundPlaceholders) {
			List<String> acStrings = getAccessControlStrings(placeholder);
			if (acStrings != null && !acStrings.isEmpty()) {
				List<EvaluatableACE> aces = getAccessControlEntries(acStrings);
				for (EvaluatableACE ace : aces) {
					if (!accessControlService.evaluate(ace)) {
						placeholder.setVisible(false);
						placeholder.setToBeRendered(false);
					}
				}
			}
		}
	}

	private void updateParts(MApplication mApplication, EModelService eModelService) {
		List<MPart> foundParts = eModelService.findElements(mApplication, null, MPart.class, null);
		for (MPart mPart : foundParts) {
			List<String> acStrings = getAccessControlStrings(mPart);
			if (acStrings != null && !acStrings.isEmpty()) {
				List<EvaluatableACE> aces = getAccessControlEntries(acStrings);
				for (EvaluatableACE ace : aces) {
					if (!accessControlService.evaluate(ace)) {
						mPart.setVisible(false);
						mPart.setToBeRendered(false);
					}
				}
			}
		}
	}

	private List<String> getViewAccessControlStrings(String viewId) {
		if (viewAccessControlMap == null) {
			viewAccessControlMap = new HashMap<>();
			IExtensionRegistry exr = Platform.getExtensionRegistry();
			IExtensionPoint exp = exr.getExtensionPoint(ExtensionPointConstantsUi.ACCESSCONTROL);
			if (exp != null) {
				IExtension[] extensions = exp.getExtensions();
				for (IExtension ex : extensions) {
					IConfigurationElement[] elems = ex.getConfigurationElements();
					for (IConfigurationElement el : elems) {
						if ("view".equals(el.getName())) {
							String elementViewId = el.getAttribute("id");
							String elementObject = el.getAttribute("object");
							String elementRole = el.getAttribute("role");
							if (StringUtils.isNotBlank(elementObject) || StringUtils.isNotBlank(elementRole)) {
								List<String> acStrings = new ArrayList<>();
								if (StringUtils.isNotBlank(elementObject)) {
									acStrings.add("object:" + elementObject.trim());
								}
								if (StringUtils.isNotBlank(elementRole)) {
									acStrings.add("role:" + elementRole.trim());
								}
								viewAccessControlMap.put(elementViewId, acStrings);
							}
						}
					}
				}
			}
		}
		return viewAccessControlMap.get(viewId);
	}

	private List<String> getAccessControlStrings(MApplicationElement partDescriptor) {
		List<String> ret = getAccessControlTags(partDescriptor.getTags());
		if (ret.isEmpty()) {
			ret = getViewAccessControlStrings(partDescriptor.getElementId());
		}
		return ret;
	}

	private List<String> getAccessControlTags(List<String> tags) {
		List<String> ret = new ArrayList<>();
		for (String tag : tags) {
			if (tag.startsWith("accesscontrol:")) {
				ret.add(tag.substring("accesscontrol:".length()));
			}
		}
		return ret;
	}

	private List<EvaluatableACE> getAccessControlEntries(List<String> acStrings) {
		List<EvaluatableACE> ret = new ArrayList<EvaluatableACE>();
		for (String acString : acStrings) {
			if (acString.startsWith("object:")) {
				String acObjTag = acString.substring("object:".length());
				String[] objParts = acObjTag.split(",");
				if (objParts != null && objParts.length > 0) {
					for (String objPart : objParts) {
						ret.add(new ObjectEvaluatableACE(objPart.trim(), Right.VIEW));
					}
				}
			}
		}
		return ret;
	}
}
