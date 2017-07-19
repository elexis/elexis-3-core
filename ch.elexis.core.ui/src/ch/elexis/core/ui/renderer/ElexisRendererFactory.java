package ch.elexis.core.ui.renderer;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;

public class ElexisRendererFactory extends WorkbenchRendererFactory {
	private ElexisRenderer stackRenderer;
	
	public ElexisRendererFactory(){
		super();
	}
	
	@Override
	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent){
		if (uiElement instanceof MPartStack) {
			if (stackRenderer == null) {
				stackRenderer = new ElexisRenderer();
				super.initRenderer(stackRenderer);
			}
			return stackRenderer;
		}
		return super.getRenderer(uiElement, parent);
	}
}