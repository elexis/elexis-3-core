package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.l10n.Messages;

/**
 * Inform the user about deprecated views, see ticket 28206.
 *
 * <p>
 * On startup all deprecated views the user still has in the restored
 * perspective are reported in one dialog, so that a user with several of them
 * does not get a cascade of dialogs at login. Afterwards each view reports
 * itself the first time it gets activated.
 * </p>
 *
 * @see DeprecatedViews
 */
@Component(property = { EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, //$NON-NLS-1$
		EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.ACTIVATE }, immediate = true) //$NON-NLS-1$
public class DeprecatedViewsEventHandler implements EventHandler {

	private Map<String, DeprecatedViewInfo> infos = new HashMap<>();

	@Override
	public void handleEvent(Event event) {
		if (UIEvents.UILifeCycle.APP_STARTUP_COMPLETE.equals(event.getTopic())) {
			Display.getDefault().asyncExec(this::reportRestoredViews);
			return;
		}

		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (element instanceof MPart) {
			reportPart((MPart) element);
		}
	}

	private void reportRestoredViews() {
		try {
			if (!PlatformUI.isWorkbenchRunning()) {
				return;
			}
			MApplication mApplication = PlatformUI.getWorkbench().getService(MApplication.class);
			EModelService modelService = PlatformUI.getWorkbench().getService(EModelService.class);
			if (mApplication == null || modelService == null) {
				return;
			}

			List<String> lines = new ArrayList<>();
			MUIElement searchRoot = getSearchRoot(mApplication, modelService);
			for (MPart part : modelService.findElements(searchRoot, null, MPart.class, null)) {
				if (!isOpen(part)) {
					continue;
				}
				DeprecatedViews.Entry entry = DeprecatedViews.get(part.getElementId());
				if (entry == null) {
					continue;
				}
				DeprecatedViewInfo info = getInfo(part.getElementId(), entry);
				if (info.isShown()) {
					continue;
				}
				info.markShown();
				lines.add(describe(getTitle(part), entry));
			}

			if (lines.isEmpty()) {
				return;
			}

			boolean single = lines.size() == 1;
			String title = single ? Messages.DeprecatedView_Title : Messages.DeprecatedView_TitlePlural;
			String header = single ? Messages.DeprecatedView_ListHeader : Messages.DeprecatedView_ListHeaderPlural;
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), title,
					header + "\n\n" + String.join("\n", lines)); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).warn("Error reporting deprecated views", e); //$NON-NLS-1$
		}
	}

	private void reportPart(MPart part) {
		DeprecatedViews.Entry entry = DeprecatedViews.get(part.getElementId());
		if (entry == null) {
			return;
		}
		DeprecatedViewInfo info = getInfo(part.getElementId(), entry);
		if (info.isShown()) {
			return;
		}
		String title = getTitle(part);
		Display.getDefault().asyncExec(() -> info.showInfo(title));
	}

	private MUIElement getSearchRoot(MApplication mApplication, EModelService modelService) {
		MWindow window = mApplication.getSelectedElement();
		if (window == null) {
			return mApplication;
		}
		MPerspective perspective = modelService.getActivePerspective(window);
		return perspective != null ? perspective : window;
	}

	private boolean isOpen(MPart part) {
		return part.isToBeRendered() && part.getWidget() != null;
	}

	private DeprecatedViewInfo getInfo(String elementId, DeprecatedViews.Entry entry) {
		return infos.computeIfAbsent(DeprecatedViews.normalizeId(elementId),
				id -> new DeprecatedViewInfo(entry.getSuccessor()));
	}

	private String describe(String title, DeprecatedViews.Entry entry) {
		if (StringUtils.isBlank(entry.getSuccessor())) {
			return MessageFormat.format(Messages.DeprecatedView_ListEntry, title);
		}
		return MessageFormat.format(Messages.DeprecatedView_ListEntryWithSuccessor, title, entry.getSuccessor());
	}

	private String getTitle(MPart part) {
		return StringUtils.isBlank(part.getLabel()) ? part.getElementId() : part.getLabel();
	}
}
