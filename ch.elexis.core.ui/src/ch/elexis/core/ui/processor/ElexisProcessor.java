package ch.elexis.core.ui.processor;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import ch.elexis.core.ui.renderer.ElexisRenderer;

public class ElexisProcessor {
	
	public ElexisProcessor(){
	}
	
	@Execute
	public void execute(MApplication mApplication, EModelService eModelService){
		
		MPartStack stack =
			(MPartStack) eModelService.find(ElexisRenderer.ELEXIS_FASTVIEW_STACK, mApplication);
		
		// no stack found create one
		if (stack == null) {
			MTrimmedWindow window = (MTrimmedWindow) eModelService.find("IDEWindow", mApplication);
			if (window == null) {
				List<MWindow> windows = mApplication.getChildren();
				if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
					window = (MTrimmedWindow) windows.get(0);
				}
			}
			if (window != null) {
				MPartStack mPartStack = eModelService.createModelElement(MPartStack.class);
				mPartStack.setElementId(ElexisRenderer.ELEXIS_FASTVIEW_STACK);
				mPartStack.setToBeRendered(true);
				mPartStack.getTags().add("Minimized");
				mPartStack.setOnTop(false);
				mPartStack.setVisible(false);
				mPartStack.getTags().add("NoAutoCollapse");
				mPartStack.getTags().add("active");
				window.getChildren().add(mPartStack);
			
				MToolControl mToolControl = eModelService.createModelElement(MToolControl.class);
				mToolControl.setElementId("my.minimized.parts(minimized)");
				mToolControl.setContributionURI(
					"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack");
				mToolControl.setToBeRendered(true);
				mToolControl.setVisible(true);
				mToolControl.getTags().add("TrimStack");
				
				MTrimBar mTrimBar = eModelService.getTrim(window, SideValue.BOTTOM);
				mTrimBar.getChildren().add(mToolControl);
				mTrimBar.setVisible(true);
				mTrimBar.setToBeRendered(true);
			}
		}
	}
}
