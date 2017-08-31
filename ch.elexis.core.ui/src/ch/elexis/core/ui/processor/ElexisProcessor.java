package ch.elexis.core.ui.processor;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class ElexisProcessor {
	
	public ElexisProcessor(){}
	
	@Execute
	public void execute(MApplication mApplication, EModelService eModelService){
		MTrimBar mTrimBar =
			(MTrimBar) eModelService.find("org.eclipse.ui.main.toolbar", mApplication);
		if (mTrimBar != null && mTrimBar.getChildren() != null) {
			mTrimBar.getChildren().clear();
		}
	}
}
