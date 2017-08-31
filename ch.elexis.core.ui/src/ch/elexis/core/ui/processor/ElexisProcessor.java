package ch.elexis.core.ui.processor;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class ElexisProcessor {
	
	public ElexisProcessor(){}
	
	@Execute
	public void execute(MApplication mApplication, EModelService eModelService){
		
		/**
		 * Clears the persisted state for the main toolbar, otherwise the positioning of the
		 * toolbar-elements are in an incorrect order. That means all persisted toolbar-elements are
		 * positioned before the dynamically created elements from
		 * ApplicationActionBarAdvisor#fillCoolBar.
		 * 
		 **/
		MTrimBar mTrimBar =
			(MTrimBar) eModelService.find("org.eclipse.ui.main.toolbar", mApplication);
		if (mTrimBar != null && mTrimBar.getChildren() != null) {
			mTrimBar.getChildren().clear();
		}
	}
}
