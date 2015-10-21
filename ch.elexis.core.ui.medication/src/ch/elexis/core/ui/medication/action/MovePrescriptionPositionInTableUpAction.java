package ch.elexis.core.ui.medication.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.ui.medication.views.MedicationComposite;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.Messages;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;

public class MovePrescriptionPositionInTableUpAction extends Action {
	
	private TableViewer tv;
	private MedicationComposite mediComposite;
	
	public MovePrescriptionPositionInTableUpAction(TableViewer tv, MedicationComposite mediComposite){
		this.tv = tv;
		this.mediComposite = mediComposite;
	}
	
	@Override
	public int getAccelerator(){
		return SWT.COMMAND | SWT.CTRL | SWT.ARROW_UP;
	}
	
	@Override
	public String getText(){
		return Messages.MovePrescriptionPositionInTableUpAction_Label;
	}
	
	@Override
	public void run(){
		mediComposite.switchToViewerSoftOrderIfNotActive(ViewerSortOrder.MANUAL);
		
		int selectionIndex = tv.getTable().getSelectionIndex();
		if (selectionIndex == 0)
			return;
		
		List<TableItem> asList = Arrays.asList(tv.getTable().getItems());
		Collections.swap(asList, selectionIndex, selectionIndex - 1);
		
		for (int i = 0; i < asList.size(); i++) {
			TableItem tableItem = asList.get(i);
			MedicationTableViewerItem pres = (MedicationTableViewerItem) tableItem.getData();
			pres.setOrder(Integer.toString(i));
		}
		
		tv.refresh();
		tv.getTable().setSelection(selectionIndex - 1);
	}
}
