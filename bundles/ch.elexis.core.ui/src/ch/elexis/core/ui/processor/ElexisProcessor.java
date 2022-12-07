package ch.elexis.core.ui.processor;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElexisProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ElexisProcessor.class);

	private MApplication mApplication;
	private EModelService eModelService;

	private static String[] removeModelElements = new String[] { "ch.elexis.SchwarzesBrett" };

	public ElexisProcessor() {
	}

	@Execute
	public void execute(MApplication mApplication, EModelService eModelService) {
		this.mApplication = mApplication;
		this.eModelService = eModelService;

		if (eModelService != null) {
			updateModelVersions(mApplication, eModelService);
		}

		updateToolbar();

		updateInjectViews();

		updateE4Views();

		updateCloseablePlaceholder();
	}

	/**
	 * Clears the persisted state for the main toolbar, otherwise the positioning of
	 * the toolbar-elements are in an incorrect order. That means all persisted
	 * toolbar-elements are positioned before the dynamically created elements from
	 * ApplicationActionBarAdvisor#fillCoolBar.
	 *
	 **/
	private void updateToolbar() {
		MTrimBar mTrimBar = (MTrimBar) eModelService.find("org.eclipse.ui.main.toolbar", mApplication);
		if (mTrimBar != null && mTrimBar.getChildren() != null) {
			mTrimBar.getChildren().clear();
		}
	}

	private String[] e4ViewIds = { "at.medevit.elexis.agenda.ui.view.agenda",
			"at.medevit.elexis.agenda.ui.view.parallel", "at.medevit.elexis.agenda.ui.view.week" };

	private void updateInjectViews() {
		List<MPart> foundParts = eModelService.findElements(mApplication, null, MPart.class, null);
		for (MPart mPart : foundParts) {
			// add inject to all compatibility views
			if (mPart.getContributionURI().endsWith("e4.compatibility.CompatibilityView")) {
				List<String> tags = mPart.getTags();
				if (!tags.contains("inject")) {
					tags.add("inject");
				}
			}
		}
	}

	private void updateCloseablePlaceholder() {
		List<MPlaceholder> foundMPlaceholders = eModelService.findElements(mApplication, null, MPlaceholder.class,
				null);
		for (MPlaceholder mPlaceholder : foundMPlaceholders) {
			// set closeable true
			if (!mPlaceholder.isCloseable()) {
				mPlaceholder.setCloseable(true);
			}
		}
	}

	private void updateE4Views() {
		for (String viewId : e4ViewIds) {
			List<MPart> foundParts = eModelService.findElements(mApplication, viewId, MPart.class, null);
			for (MPart mPart : foundParts) {
				// remove references to old CompatibilityView part
				if (mPart.getContributionURI() == null || mPart.getContributionURI().endsWith("CompatibilityView")) {
					EcoreUtil.delete((EObject) mPart);
				}
			}
		}
	}

	private void updateModelVersions(MApplication mApplication, EModelService eModelService) {
		try {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				MTrimmedWindow mWindow = (MTrimmedWindow) windows.get(0);
				// remove old model elements like perspectives etc
				for (String modelElementId : removeModelElements) {
					MUIElement element = eModelService.find(modelElementId, mApplication);
					if (element != null) {
						if (element instanceof MPerspective) {
							eModelService.removePerspectiveModel((MPerspective) element, mWindow);
							logger.info("model element (perspective): " + modelElementId + " removed!");
						} else {
							MElementContainer<MUIElement> parent = element.getParent();
							parent.getChildren().remove(element);
							element.setToBeRendered(false);
							logger.info("model element: " + modelElementId + " removed!");
						}
					}
				}
			} else {
				logger.warn("cannot find active window");
			}
		} catch (Exception e) {
			logger.error("unexpected exception - cannot do updates on models", e);
		}
	}
}
