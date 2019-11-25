package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.laboratory.controls.LaborOrderViewerItem;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;

public class LaborResultOrderDeleteAction extends Action implements IAction {
	
	private List<?> selectedOrdersOrResults;
	private final Shell shell;
	private final StructuredViewer viewer;
	
	public LaborResultOrderDeleteAction(List<?> list, StructuredViewer viewer){
		super(ch.elexis.core.l10n.Messages.LabResultOrOrderDeleteAction_title);
		this.selectedOrdersOrResults = list;
		this.viewer = viewer;
		this.shell = viewer.getControl().getShell();
	}
	
	public LaborResultOrderDeleteAction(List<?> list, Shell shell){
		super(ch.elexis.core.l10n.Messages.LabResultOrOrderDeleteAction_title);
		this.selectedOrdersOrResults = list;
		this.viewer = null;
		this.shell = shell;
	}
	
	@Override
	public void run(){
				
		for (Object object : selectedOrdersOrResults) {
			
			LabResult result;
			LabOrder order;
			
			if (object instanceof LabOrder) {
				order = (LabOrder) object;
				result = (LabResult) order.getLabResult();
			} else if (object instanceof LabResult) {
				result = (LabResult) object;
				order = result.getLabOrder();
			} else if (object instanceof LaborOrderViewerItem) {
				// drop-in-replacement support for LaborOrdersComposite
				result = ((LaborOrderViewerItem) object).getLabResult();
				order = ((LaborOrderViewerItem) object).getLabOrder();
			} else {
				throw new IllegalArgumentException("Unknown list entry type of class " //$NON-NLS-1$
					+ object.getClass());
			}
			
			boolean delete = MessageDialog.openConfirm(shell, "Resultat/Verordnung entfernen",
				"Soll das Resultat [" + result
					+ "] sowie die zugeh. Verordnung wirklich entfernt werden?");
			
			if (delete) {
				AcquireLockBlockingUi.aquireAndRun(result, new ILockHandler() {
					@Override
					public void lockFailed(){
						// do nothing
					}
					
					@Override
					public void lockAcquired(){
						if (result != null) {
							result.delete();
						}
						if (order != null) {
							order.delete();
						}
						if (viewer != null) {
							viewer.refresh();
						}
						
					}
				});
				ElexisEventDispatcher.reload(LabResult.class);
			}
		}
	}
	
	@Override
	public boolean isEnabled(){
		return selectedOrdersOrResults != null && !selectedOrdersOrResults.isEmpty();
	}
	
}
