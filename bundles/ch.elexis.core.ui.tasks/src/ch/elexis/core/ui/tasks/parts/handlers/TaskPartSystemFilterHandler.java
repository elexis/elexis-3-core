
package ch.elexis.core.ui.tasks.parts.handlers;

import java.util.Collections;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;

import ch.elexis.core.ui.e4.parts.IRefreshablePart;

public class TaskPartSystemFilterHandler {

	public static final String SHOW_SYSTEM_TASKS = "sst";

	@Execute
	public void execute(MPart part, MDirectToolItem item) {
		((IRefreshablePart) part.getObject()).refresh(Collections.singletonMap(SHOW_SYSTEM_TASKS, item.isSelected()));
	}

}