package ch.elexis.core.ui.laboratory.controls.util;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerRow;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.core.ui.laboratory.controls.Messages;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class LabResultEditingSupport extends LabOrderEditingSupport {
	
	private TreeViewerColumn column;
	private LaborResultsComposite composite;

	public LabResultEditingSupport(LaborResultsComposite laborResultsComposite, TreeViewer viewer,
		TreeViewerColumn column){
		super(viewer);
		this.column = column;
		this.composite = laborResultsComposite;
	}
	
	@Override
	protected void addValidator(){
		textCellEditor.setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value){
				IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				LaborItemResults results = (LaborItemResults) selection.getFirstElement();
				if (results != null && value instanceof String) {
					if (results.getLabItem().getTyp() == typ.NUMERIC
						|| results.getLabItem().getTyp() == typ.ABSOLUTE) {
						try {
							String editedValue = (String) value;
							if (editedValue.startsWith(SMALLER) || editedValue.startsWith(BIGGER)) {
								String nrValue =
									editedValue.replace(SMALLER, "").replace(BIGGER, "");
								editedValue = nrValue.trim();
							}
							Float.parseFloat(editedValue);
							
						} catch (NumberFormatException e) {
							return Messages.LaborOrdersComposite_validatorNotNumber;
						}
					}
				}
				return null;
			}
		});
	}

	@Override
	protected boolean canEdit(Object element){
		return element instanceof LaborItemResults;
	}

	@Override
	protected CellEditor getCellEditor(Object element){
		if (element instanceof LaborItemResults) {
			LabItem labItem = ((LaborItemResults) element).getLabItem();
			if (labItem.getTyp() == typ.DOCUMENT) {
				return null;
			} else {
				return textCellEditor;
			}
		}
		return null;
	}
	
	@Override
	protected Object getValue(Object element){
		return ""; //$NON-NLS-1$
	}
	
	private TimeTool getDate() {
		return (TimeTool) column.getColumn().getData(LaborResultsComposite.COLUMN_DATE_KEY);
	}

	@Override
	protected void setValue(Object element, Object value){
		if (element instanceof LaborItemResults && value != null) {
			LabItem labItem = ((LaborItemResults) element).getLabItem();
			if (labItem.getTyp() == typ.DOCUMENT) {
				return;
			}
			LabResult result = createResult(labItem, LabOrder.getOrCreateManualLabor());
			if (result.getItem().getTyp() == typ.TEXT) {
				result.setResult("Text"); //$NON-NLS-1$
				result.set(LabResult.COMMENT, value.toString());
			} else if (result.getItem().getTyp() == typ.DOCUMENT) {
				// dont know what todo ...
			} else {
				result.setResult(value.toString());
			}
			int columnIdx = focusCell.getFocusCell().getColumnIndex();
			ViewerRow row = focusCell.getFocusCell().getViewerRow();
			ViewerRow nextRow = row.getNeighbor(ViewerRow.BELOW, true);
			composite.reload();
			if (nextRow != null) {
				getViewer().setSelection(new StructuredSelection(nextRow.getElement()), true);
				getViewer().editElement(nextRow.getElement(), columnIdx);
			}
		}
	}

	private LabResult createResult(LabItem item, Kontakt origin){
		TimeTool now = new TimeTool();
		LabOrder order =
			new LabOrder(CoreHub.actUser, CoreHub.actMandant,
				ElexisEventDispatcher.getSelectedPatient(), item, null, LabOrder.getNextOrderId(),
				"Eingabe", now);
		LabResult result = order.createResult(origin);
		order.setState(LabOrder.State.DONE);
		result.setTransmissionTime(now);
		result.setObservationTime(getDate());
		return result;
	}
}
