package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
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
			result.setFlag(LabResultConstants.PATHOLOGIC, !isChecked());
			result.setPathologicDescription(new PathologicDescription(Description.PATHO_MANUAL));
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