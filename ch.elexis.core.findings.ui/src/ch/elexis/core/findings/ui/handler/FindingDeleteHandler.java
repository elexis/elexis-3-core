package ch.elexis.core.findings.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;

public class FindingDeleteHandler extends AbstractHandler implements IHandler {
	
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
					
						}
						
						@Override
						public void lockAcquired(){
							
							try {
								FindingsUiUtil.deleteObservation(iFinding);
							} catch (ElexisException e) {
								MessageDialog.openError(UiDesk.getDisplay().getActiveShell(),
									"Fehler", e.getMessage());
							}
							
							ElexisEventDispatcher.getInstance()
								.fire(new ElexisEvent((IPersistentObject) iFinding, IFinding.class,
									ElexisEvent.EVENT_DELETE));
						}
					});
			}
			
		}
		return null;
	}
}
