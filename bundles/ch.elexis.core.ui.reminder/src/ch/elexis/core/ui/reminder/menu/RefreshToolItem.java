 
package ch.elexis.core.ui.reminder.menu;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.ui.views.IRefreshable;

public class RefreshToolItem {
	@Execute
	public void execute(MPart mpart, UISynchronize uiSynchronize) {
		if (mpart != null && mpart.getObject() instanceof IRefreshable) {
			uiSynchronize.asyncExec(() -> {
				((IRefreshable) mpart.getObject()).refresh();
			});
		}
	}
		
}