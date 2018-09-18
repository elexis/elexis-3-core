package ch.elexis.core.findings.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.dialogs.FindingsEditDialog;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;

public class FindingEditHandler extends AbstractHandler implements IHandler {
	public static final String COMMAND_ID = "ch.elexis.core.findings.ui.commandEdit";
	
	private Boolean ret;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ret = Boolean.FALSE;
		ISelection selection = getSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			Object item = ((StructuredSelection) selection).getFirstElement();
			if (item instanceof IFinding) {
				
				IFinding iFinding = (IFinding) item;
				AcquireLockBlockingUi.aquireAndRun(iFinding,
					new ILockHandler() {
						@Override
						public void lockFailed(){
							// do nothing
						}
						
						@Override
						public void lockAcquired(){
							FindingsEditDialog findingsEditDialog = new FindingsEditDialog(
								Display.getDefault().getActiveShell(), iFinding);
							int dialogRet = findingsEditDialog.open();
							
							findingsEditDialog.releaseAllLocks();
							if (dialogRet == MessageDialog.OK) {
								//								if (iFinding instanceof IObservation) {
								//									// do recursive save for observation children
								//									IObservation observation = (IObservation) iFinding;
								//									if (observation.getObservationType() == ObservationType.REF) {
								//										List<IObservation> obsChildren = new ArrayList<>();
								//										ModelUtil.getObservationChildren(observation, obsChildren,
								//											100);
								//										for (IObservation iObservation : obsChildren) {
								//											FindingsServiceComponent.getService()
								//												.saveFinding(iObservation);
								//										}
								//									}
								//								}
								FindingsServiceComponent.getService().saveFinding(iFinding);
								ElexisEventDispatcher.getInstance()
									.fire(new ElexisEvent(iFinding,
										IFinding.class, ElexisEvent.EVENT_RELOAD));
								ret = Boolean.TRUE;
							}
						}
					});
			}
		}
		return ret;
	}
	
	private ISelection getSelection(ExecutionEvent executionEvent){
		ISelection selection = null;
		if (executionEvent.getTrigger() != null) {
			// try e3 style if the event is called from plugin.xml and not called manually
			selection = HandlerUtil.getCurrentSelection(executionEvent);
		} else {
			IEclipseContext iEclipseContext =
				PlatformUI.getWorkbench().getService(IEclipseContext.class);
			selection = (StructuredSelection) iEclipseContext.get(COMMAND_ID.concat(".selection"));
			iEclipseContext.remove(COMMAND_ID.concat(".selection"));
		}
		return selection;
	}
}
