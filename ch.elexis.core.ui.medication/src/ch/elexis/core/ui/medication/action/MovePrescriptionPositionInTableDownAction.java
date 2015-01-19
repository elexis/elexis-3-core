package ch.elexis.core.ui.medication.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.ui.medication.views.Messages;
import ch.elexis.data.Prescription;

public class MovePrescriptionPositionInTableDownAction extends Action {
	
	private TableViewer tv;
	
	public MovePrescriptionPositionInTableDownAction(TableViewer tv){
		this.tv = tv;
	}
	
	@Override
	public int getAccelerator(){
		return SWT.COMMAND | SWT.ARROW_DOWN;
	}
	
	@Override
	public String getText(){
		return Messages.MovePrescriptionPositionInTableDownAction_Label;
	}
	
	@Override
	public void run(){
		int selectionIndex = tv.getTable().getSelectionIndex();
		if (selectionIndex == tv.getTable().getItemCount() + 1)
			return;
		
		List<TableItem> asList = Arrays.asList(tv.getTable().getItems());
		Collections.swap(asList, selectionIndex, selectionIndex + 1);
		
		for (int i = 0; i < asList.size(); i++) {
			TableItem tableItem = asList.get(i);
			Prescription pres = (Prescription) tableItem.getData();
			pres.set(Prescription.FLD_SORT_ORDER, Integer.toString(i));
		}
		
		tv.refresh();
		tv.getTable().setSelection(selectionIndex + 1);
	}
	
}
