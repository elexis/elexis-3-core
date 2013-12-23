package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabResult;

public class TogglePathologicAction extends Action {
	private List<LabResult> results;
	private StructuredViewer viewer;
	
	public TogglePathologicAction(List<LabResult> results, StructuredViewer viewer){
		super(Messages.TogglePathologic_title, Action.AS_CHECK_BOX); //$NON-NLS-1$
		this.results = results;
		this.viewer = viewer;
	}
	
	@Override
	public void run(){
		for (LabResult result : results) {
			result.setFlag(LabResult.PATHOLOGIC, !isChecked());
		}
		viewer.refresh();
	}
	
	@Override
	public boolean isEnabled(){
		for (LabResult result : results) {
			if (result.getItem().getTyp() == typ.DOCUMENT) {
				return false;
			}
		}
		return super.isEnabled();
	}
	
	@Override
	public boolean isChecked(){
		for (LabResult result : results) {
			if (result.isFlag(LabResult.PATHOLOGIC)) {
				return true;
			}
		}
		return false;
	}
}