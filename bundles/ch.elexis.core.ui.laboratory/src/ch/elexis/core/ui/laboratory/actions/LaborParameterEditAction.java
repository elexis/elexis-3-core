package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.admin.Messages;
import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.laboratory.commands.EditLabItemUi;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;

public class LaborParameterEditAction extends RestrictedAction {

	private List<LabResult> currentSelection;
	private StructuredViewer viewer;

	public LaborParameterEditAction(List<LabResult> currentSelection, StructuredViewer viewer) {
		super(EvACE.of(ILabItem.class, Right.UPDATE), Messages.AccessControlDefaults_EditLaboratoryParameter);
		this.currentSelection = currentSelection;
		this.viewer = viewer;
	}

	@Override
	public void doRun() {
		if (currentSelection != null && currentSelection.size() == 1) {
			LabItem labItem = (LabItem) currentSelection.get(0).getItem();
			EditLabItemUi.executeWithParams(NoPoUtil.loadAsIdentifiable(labItem, ILabItem.class).orElse(null));
		}

	}

}
