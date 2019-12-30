package ch.elexis.core.ui.documents.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.documents.views.DocumentsView;

public class FlatViewHandler extends AbstractHandler implements IElementUpdater {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.findView(DocumentsView.ID);
		if (viewPart instanceof DocumentsView) {
			((DocumentsView) viewPart)
				.switchFlatView(!CoreHub.userCfg.get(DocumentsView.SETTING_FLAT_VIEW, false));
		}
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters){
		boolean bFlat = CoreHub.userCfg.get(DocumentsView.SETTING_FLAT_VIEW, false);
		element.setChecked(bFlat);
	}
}
