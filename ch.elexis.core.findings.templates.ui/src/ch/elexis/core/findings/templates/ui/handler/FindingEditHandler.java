package ch.elexis.core.findings.templates.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.templates.ui.dlg.FindingsEditDialog;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;

public class FindingEditHandler extends AbstractHandler implements IHandler {
	public static final String COMMAND_ID = "ch.elexis.core.findings.templates.ui.commandEdit";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			Object item = ((StructuredSelection) selection).getFirstElement();
			if (item instanceof IFinding) {
				
				IFinding iFinding = (IFinding) item;
				AcquireLockBlockingUi.aquireAndRun((IPersistentObject) iFinding,
					new ILockHandler() {
						@Override
						public void lockFailed(){
							// do nothing
						}
						
						@Override
						public void lockAcquired(){
							FindingsEditDialog findingsEditDialog = new FindingsEditDialog(
								Display.getDefault().getActiveShell(), iFinding);
							int ret = findingsEditDialog.open();
							
							findingsEditDialog.releaseAllLocks();
							if (ret == MessageDialog.OK) {
								ElexisEventDispatcher.getInstance()
									.fire(new ElexisEvent((IPersistentObject) iFinding,
									IFinding.class, ElexisEvent.EVENT_RELOAD));
							}
						}
					});
			}
			
		}
		return null;
	}
	
}
