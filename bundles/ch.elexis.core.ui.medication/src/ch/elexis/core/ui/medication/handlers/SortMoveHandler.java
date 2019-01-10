package ch.elexis.core.ui.medication.handlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.medication.views.MedicationComposite;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;

public class SortMoveHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		String direction = event.getParameter("ch.elexis.core.ui.medication.sortmove.direction"); //$NON-NLS-1$
		if (direction != null) {
			IWorkbenchPart part = HandlerUtil.getActivePart(event);
			if (part instanceof MedicationView) {
				MedicationComposite composite = ((MedicationView) part).getMedicationComposite();
				composite.setViewerSortOrder(ViewerSortOrder.MANUAL);
				
				TableViewer activeViewer = composite.getActiveTableViewer();
				if (activeViewer != null) {
					int selectionIndex = activeViewer.getTable().getSelectionIndex();
					
					if(moveNotPossible(selectionIndex, activeViewer, direction)) {
						return null;
					}
					
					List<TableItem> asList = Arrays.asList(activeViewer.getTable().getItems());
					if (directionIsUp(direction)) {
						Collections.swap(asList, selectionIndex, selectionIndex - 1);
					} else {
						Collections.swap(asList, selectionIndex, selectionIndex + 1);
					}
					
					for (int i = 0; i < asList.size(); i++) {
						TableItem tableItem = asList.get(i);
						MedicationTableViewerItem pres =
							(MedicationTableViewerItem) tableItem.getData();
						pres.setOrder(i);
					}
					
					activeViewer.refresh();
					if (directionIsUp(direction)) {
						activeViewer.getTable().setSelection(selectionIndex - 1);
					} else {
						activeViewer.getTable().setSelection(selectionIndex + 1);
					}
				}
			}
		}
		
		return null;
	}
	
	private boolean moveNotPossible(int selectionIndex, TableViewer activeViewer, String direction){
		if ("up".equals(direction)) {
			return selectionIndex == 0;
		} else if ("down".equals(direction)) {
			return selectionIndex == activeViewer.getTable().getItemCount() - 1;
		}
		return true;
	}
	
	private boolean directionIsUp(String direction){
		return "up".equals(direction);
	}
}
