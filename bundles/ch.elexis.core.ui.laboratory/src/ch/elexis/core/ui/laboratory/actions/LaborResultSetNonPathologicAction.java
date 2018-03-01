package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.LabResult;

public class LaborResultSetNonPathologicAction extends Action {
	private List<LabResult> results;
	private StructuredViewer viewer;
	
	public LaborResultSetNonPathologicAction(List<LabResult> results, StructuredViewer viewer){
		super(Messages.SetNonPathologic);
		this.results = results;
		this.viewer = viewer;
	}
	
	@Override
	public void run(){
		for (LabResult result : results) {
			AcquireLockBlockingUi.aquireAndRun(result, new ILockHandler() {
				@Override
				public void lockFailed(){
					// do nothing
				}
				
				@Override
				public void lockAcquired(){
					result.setFlag(LabResultConstants.PATHOLOGIC, false);
					result.setPathologicDescription(new PathologicDescription(Description.PATHO_MANUAL));
				}
			});
		}
		viewer.refresh();
	}
	
	@Override
	public boolean isEnabled(){
		for (LabResult result : results) {
			if (result.getItem().getTyp() == LabItemTyp.DOCUMENT) {
				return false;
			}
		}
		return super.isEnabled();
	}
	
	@Override
	public boolean isChecked(){
		for (LabResult result : results) {
			if (result.isFlag(LabResultConstants.PATHOLOGIC)) {
				return true;
			}
		}
		return false;
	}
}