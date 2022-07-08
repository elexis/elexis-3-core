package ch.elexis.core.ui.compatibility;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.UiDesk;

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class UiStartupHandler implements EventHandler {

	@Override
	public void handleEvent(Event event) {
		LoggerFactory.getLogger(getClass()).info("APPLICATION STARTUP COMPLETE"); //$NON-NLS-1$
		Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
		if (property instanceof MApplication) {
			MApplication application = (MApplication) property;

			EModelService modelService = application.getContext().get(EModelService.class);

			UiDesk.asyncExec(new Runnable() {
				public void run() {
					addMandantSelectionItem(application, modelService);
					ElexisFastViewUtil.registerPerspectiveListener();
				}
			});

		}
	}

	private void addMandantSelectionItem(MApplication mApplication, EModelService eModelService) {

		MTrimBar trimbar = (MTrimBar) eModelService.find("org.eclipse.ui.main.toolbar", mApplication); //$NON-NLS-1$

		if (trimbar != null) {
			MTrimElement mTrimElement = null;
			int position = 0;
			int i = 0;
			List<MTrimElement> childrens = trimbar.getChildren();
			for (MTrimElement element : childrens) {

				if ("ch.elexis.core.ui.toolcontrol.mandantselection".equals(element.getElementId())) { //$NON-NLS-1$
					mTrimElement = element;
				}

				if (position == 0 && ("ch.elexis.toolbar1".equals(element.getElementId()) //$NON-NLS-1$
						|| "PerspectiveSpacer".equals(element.getElementId()))) { //$NON-NLS-1$
					position = i;
				}
				i++;
			}

			if (mTrimElement == null) {
				MToolControl mToolControl = eModelService.createModelElement(MToolControl.class);
				mToolControl.setElementId("ch.elexis.core.ui.toolcontrol.mandantselection"); //$NON-NLS-1$
				mToolControl.setContributionURI(
						"bundleclass://ch.elexis.core.ui/ch.elexis.core.ui.coolbar.MandantSelectionContributionItem"); //$NON-NLS-1$
				mToolControl.setToBeRendered(true);
				mToolControl.setVisible(true);

				childrens.add(position, mToolControl);
			}
		}

	}
}
