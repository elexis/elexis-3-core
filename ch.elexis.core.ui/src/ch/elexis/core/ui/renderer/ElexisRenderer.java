package ch.elexis.core.ui.renderer;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.widgets.Menu;

import ch.elexis.core.ui.fastview.ElexisFastViewUtil;


@SuppressWarnings("restriction")
public class ElexisRenderer extends StackRenderer {
	@Override
	protected void populateTabMenu(final Menu menu, MPart part){
		super.populateTabMenu(menu, part);
		ElexisFastViewUtil.createFastViewMenuItem(menu, part);
	}
}
