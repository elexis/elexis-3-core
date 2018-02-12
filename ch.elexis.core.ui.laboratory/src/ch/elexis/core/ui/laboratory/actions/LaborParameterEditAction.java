package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.Messages;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.laboratory.commands.EditLabItemUi;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;

public class LaborParameterEditAction extends RestrictedAction {
	
	private List<LabResult> currentSelection;
	private StructuredViewer viewer;
	
	public LaborParameterEditAction(List<LabResult> currentSelection, StructuredViewer viewer){
		super(AccessControlDefaults.LABPARAM_EDIT, Messages.AccessControlDefaults_EditLaboratoryParameter);
		this.currentSelection = currentSelection;
		this.viewer = viewer;
	}
	
	@Override
	public void doRun(){
		if (currentSelection != null && currentSelection.size() == 1) {
			LabItem labItem = (LabItem) currentSelection.get(0).getItem();
			EditLabItemUi.executeWithParams(labItem);
		}
		
	}
	
}
