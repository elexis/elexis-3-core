package ch.elexis.core.ui.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;

public class AccessControlProcessor {

	private Map<String, List<String>> viewAccessControlMap;

	@Inject
	private IAccessControlService accessControlService;

	private Set<MPartDescriptor> removedDescriptors = new HashSet<>();

	@SuppressWarnings("restriction")
	@Execute
	public void execute(IEclipseContext context, MApplication mApplication, EModelService eModelService) {
		updateDescriptors(mApplication, eModelService);
		Display.getDefault().syncExec(() -> {
			updatePlaceholders(mApplication, eModelService);
			updateParts(mApplication, eModelService);
		});
		Display.getDefault().asyncExec(() -> {
			// reset perspective use platform ui see platform ResetPerspectiveHandler
			WorkbenchPage page = (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				page.resetPerspective();
			}
		});
	}

	@Optional
	@Inject
	public void login(@UIEventTopic("info/elexis/ui/login") IUser user, MApplication mApplication,
			EModelService eModelService) {
		if (!removedDescriptors.isEmpty()) {
			resetModel(mApplication, eModelService);
		}
		execute(mApplication.getContext(), mApplication, eModelService);
	}

	private void resetModel(MApplication mApplication, EModelService eModelService) {
		Display.getDefault().syncExec(() -> {
			if (removedDescriptors != null && !removedDescriptors.isEmpty()) {
				mApplication.getDescriptors().addAll(removedDescriptors);
				removedDescriptors = new HashSet<>();
			}

			List<MPlaceholder> foundPlaceholders = eModelService.findElements(mApplication, null, MPlaceholder.class,
					null);
			for (MPlaceholder placeholder : foundPlaceholders) {
				List<String> acStrings = getAccessControlStrings(placeholder);
				if (acStrings != null && !acStrings.isEmpty()) {
					placeholder.setVisible(true);
					placeholder.setToBeRendered(true);
				}
			}

			List<MPart> foundParts = eModelService.findElements(mApplication, null, MPart.class, null);
			for (MPart mPart : foundParts) {
				List<String> acStrings = getAccessControlStrings(mPart);
				if (acStrings != null && !acStrings.isEmpty()) {
					mPart.setVisible(false);
					mPart.setToBeRendered(false);
				}
			}
		});
	}

	private List<MPartDescriptor> updateDescriptors(MApplication mApplication, EModelService eModelService) {
		List<MPartDescriptor> ret = new ArrayList<MPartDescriptor>();
		for (MPartDescriptor foundPartDescriptor : new ArrayList<>(mApplication.getDescriptors())) {
			List<String> acStrings = getAccessControlStrings(foundPartDescriptor);
			if (acStrings != null && !acStrings.isEmpty()) {
				List<EvaluatableACE> aces = getAccessControlEntries(acStrings);
				for (EvaluatableACE ace : aces) {
					if (!accessControlService.evaluate(ace)) {
						while (mApplication.getDescriptors().remove(foundPartDescriptor)) {
							// System.out.println("Remove [" + foundPartDescriptor + "]");
						}
						removedDescriptors.add(foundPartDescriptor);
					}
				}
			}
		}
		return ret;
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
