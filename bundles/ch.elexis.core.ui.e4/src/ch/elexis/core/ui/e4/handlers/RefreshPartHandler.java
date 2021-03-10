
package ch.elexis.core.ui.e4.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.ui.e4.parts.IRefreshablePart;

public class RefreshPartHandler {
	
	@Execute
	public void execute(MPart part){
		Object object = part.getObject();
		if (object instanceof IRefreshablePart) {
			((IRefreshablePart) object).refresh();
		} else {
			throw new IllegalArgumentException(object + " is not an instance of IRefreshablePart");
		}
	}
	
	@CanExecute
	public boolean canExecute(MPart part){
		return true;
	}
	
}