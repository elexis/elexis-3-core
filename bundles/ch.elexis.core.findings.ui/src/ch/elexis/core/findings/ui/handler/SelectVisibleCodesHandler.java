package ch.elexis.core.findings.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ui.dialogs.VisibleCodingsSelectionDialog;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;

public class SelectVisibleCodesHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		
		VisibleCodingsSelectionDialog dialog =
			new VisibleCodingsSelectionDialog(shell, FindingsUiUtil.getAvailableCodings());
		dialog.setSelected(FindingsUiUtil.loadVisibleCodings());
		if (dialog.open() == Window.OK) {
			FindingsUiUtil.saveVisibleCodings(dialog.getSelected());
			ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null, ICoding.class,
				ElexisEvent.EVENT_RELOAD, ElexisEvent.PRIORITY_NORMAL));
		}
		return null;
	}
}
