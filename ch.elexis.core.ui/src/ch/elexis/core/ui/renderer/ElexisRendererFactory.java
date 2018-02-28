package ch.elexis.core.ui.renderer;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;

@SuppressWarnings("restriction")
public class ElexisRendererFactory extends WorkbenchRendererFactory {
	private ElexisStackRenderer stackRenderer;
	
	public ElexisRendererFactory(){
		super();
	}
	
	@Override
	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent){
		if (uiElement instanceof MPartStack) {
			if (stackRenderer == null) {
				stackRenderer = new ElexisStackRenderer();
				super.initRenderer(stackRenderer);
			}
			return stackRenderer;
		}
		return super.getRenderer(uiElement, parent);
	}
}