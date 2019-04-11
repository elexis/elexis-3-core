package ch.elexis.core.ui.processor;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class ElexisProcessor {
	
	private MApplication mApplication;
	private EModelService eModelService;
	
	public ElexisProcessor(){}
	
	@Execute
	public void execute(MApplication mApplication, EModelService eModelService){
		this.mApplication = mApplication;
		this.eModelService = eModelService;

		updateToolbar();
		
		updateInjectViews();
	}
	
	/**
	 * Clears the persisted state for the main toolbar, otherwise the positioning of the
	 * toolbar-elements are in an incorrect order. That means all persisted toolbar-elements are
	 * positioned before the dynamically created elements from
	 * ApplicationActionBarAdvisor#fillCoolBar.
	 * 
	 **/
	private void updateToolbar(){
		MTrimBar mTrimBar =
			(MTrimBar) eModelService.find("org.eclipse.ui.main.toolbar", mApplication);
		if (mTrimBar != null && mTrimBar.getChildren() != null) {
			mTrimBar.getChildren().clear();
		}
	}
	
	private String[] injectViewIds = {
		"ch.elexis.Konsdetail", "ch.elexis.PatListView",
		"ch.elexis.core.ui.medication.views.MedicationView", "ch.elexis.icpc.encounterView",
		"ch.elexis.icpc.episodesView", "ch.elexis.omnivore.views.OmnivoreView"
	};
	
	private void updateInjectViews(){
		for (String viewId : injectViewIds) {
			List<MPart> foundParts =
				eModelService.findElements(mApplication, viewId, MPart.class, null);
			for (MPart mPart : foundParts) {
				List<String> tags = mPart.getTags();
				if (!tags.contains("inject")) {
					tags.add("inject");
				}
			}
		}
	}
}
