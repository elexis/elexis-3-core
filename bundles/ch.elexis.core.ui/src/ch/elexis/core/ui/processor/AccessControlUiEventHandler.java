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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE + "ui/accesscontrol/update",
		EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE + "ui/accesscontrol/reset",
		EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE })
public class AccessControlUiEventHandler implements EventHandler {

	@Reference
	private IAccessControlService accessControlService;

	private boolean startUpComplete = false;

	private MApplication mApplication;

	@Inject
	private EModelService eModelService;

	@Inject
	private IEventBroker eventBroker;

	private Map<String, List<String>> viewAccessControlMap;

	private Set<MPartDescriptor> removedDescriptors = new HashSet<>();


	private void updateModel() {
		LoggerFactory.getLogger(getClass()).info("UPDATE MODEL " + mApplication + " / " + eModelService);

		updateDescriptors();
		updateParts();
		updatePlaceholders();
		updatePartStacks();
	}

	@Override
	public void handleEvent(Event event) {
		if (UIEvents.UILifeCycle.APP_STARTUP_COMPLETE.equals(event.getTopic())) {
			Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
			if (property instanceof MApplication) {
				mApplication = (MApplication) property;
			}
			CoreUiUtil.injectServices(this);
			startUpComplete = true;
			Display.getDefault().asyncExec(() -> {
				updateModel();
			});
		}
		if (startUpComplete) {
			if (event.getTopic().endsWith("ui/accesscontrol/reset")) {
				Display.getDefault().asyncExec(() -> {
					LoggerFactory.getLogger(getClass()).info("RESET for event [" + event + "]");
					if (mApplication != null && eModelService != null) {
						resetModel();
					}
				});
			}
			if (event.getTopic().endsWith("ui/accesscontrol/update")) {
				Display.getDefault().asyncExec(() -> {
					LoggerFactory.getLogger(getClass()).info("UPDATE for event [" + event + "]");
					if (mApplication != null && eModelService != null) {
						updateModel();
					}
				});
			}
		}
	}

	private void resetModel() {
		LoggerFactory.getLogger(getClass()).info("RESET MODEL " + mApplication + " / " + eModelService);

		if (removedDescriptors != null && !removedDescriptors.isEmpty()) {
			mApplication.getDescriptors().addAll(removedDescriptors);
			removedDescriptors = new HashSet<>();
		}
	}

	private List<MPartDescriptor> updateDescriptors() {
		List<MPartDescriptor> ret = new ArrayList<>();
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

	private void updatePartStacks() {
		MPerspective activePerspective = GlobalActions.getActivePerspective(eModelService);
		List<MPartStack> foundpartStacks = eModelService.findElements(activePerspective, null, MPartStack.class, null);
		for (MPartStack partStack : foundpartStacks) {
			MStackElement selectedPart = partStack.getSelectedElement();
			if (selectedPart != null && selectedPart.isVisible() && selectedPart.isToBeRendered()) {
				sendStackSelectedElement(partStack.getSelectedElement(), partStack);
			} else {
				for (MStackElement part : partStack.getChildren()) {
					if (part.isVisible() && part.isToBeRendered()) {
						sendStackSelectedElement(part, partStack);
					}
				}
			}
		}
	}

	private void sendStackSelectedElement(MStackElement mStackElement, MPartStack partStack) {
		Map<String, Object> argMap = new HashMap<>();
		argMap.put("ChangedElement", partStack);
		argMap.put("OldValue", null);
		argMap.put("NewValue", partStack.getSelectedElement());
		eventBroker.send("org/eclipse/e4/ui/model/ui/ElementContainer/selectedElement/SET", argMap);
	}

	private void updatePlaceholders() {
		List<MPlaceholder> foundPlaceholders = eModelService.findElements(mApplication, null, MPlaceholder.class, null);
		for (MPlaceholder placeholder : foundPlaceholders) {
			List<String> acStrings = getAccessControlStrings(placeholder);
			if (acStrings != null && !acStrings.isEmpty()) {
				List<EvaluatableACE> aces = getAccessControlEntries(acStrings);
				for (EvaluatableACE ace : aces) {
					if (!accessControlService.evaluate(ace)) {
						placeholder.setVisible(false);
						placeholder.getTags().add("AccessControlUiEventHandler:hidden");
					} else {
						if (placeholder.getTags().contains("AccessControlUiEventHandler:hidden")) {
							placeholder.setVisible(true);
							placeholder.getTags().remove("AccessControlUiEventHandler:hidden");
						}
					}
				}
			}
		}
	}

	private void updateParts() {
		List<MPart> foundParts = eModelService.findElements(mApplication, null, MPart.class, null);
		for (MPart mPart : foundParts) {
			List<String> acStrings = getAccessControlStrings(mPart);
			if (acStrings != null && !acStrings.isEmpty()) {
				List<EvaluatableACE> aces = getAccessControlEntries(acStrings);
				for (EvaluatableACE ace : aces) {
					if (!accessControlService.evaluate(ace)) {
						mPart.setVisible(false);
						mPart.getTags().add("AccessControlUiEventHandler:hidden");
					} else {
						if (mPart.getTags().contains("AccessControlUiEventHandler:hidden")) {
							mPart.setVisible(true);
							mPart.getTags().remove("AccessControlUiEventHandler:hidden");
						}
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
		List<EvaluatableACE> ret = new ArrayList<>();
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
