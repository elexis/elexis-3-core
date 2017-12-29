package ch.elexis.core.ui.util;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.PlatformUI;

public class PerspectiveUtil {
	
	public static MPerspective getActivePerspective(){
		EModelService modelService = getService(EModelService.class);
		MTrimmedWindow mWindow = getActiveWindow();
		if (mWindow != null) {
			return modelService.getActivePerspective(mWindow);
		}
		return null;
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	public static MTrimmedWindow getActiveWindow(){
		EModelService modelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		
		MTrimmedWindow mWindow = (MTrimmedWindow) modelService.find("IDEWindow", mApplication);
		if (mWindow == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				mWindow = (MTrimmedWindow) windows.get(0);
			}
		}
		return mWindow;
	}
}
