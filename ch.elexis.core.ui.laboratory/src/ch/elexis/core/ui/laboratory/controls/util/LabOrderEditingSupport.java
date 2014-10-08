package ch.elexis.core.ui.laboratory.controls.util;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.laboratory.controls.Messages;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class LabOrderEditingSupport extends EditingSupport {
	protected final String SMALLER = "<";
	protected final String BIGGER = ">";

	protected TextCellEditor textCellEditor;
	protected TreeViewerFocusCellManager focusCell;
	
	public LabOrderEditingSupport(TreeViewer viewer){
		super(viewer);
		
		setUpCellEditor(viewer);
		addValidator();
	}
	
	protected void addValidator(){
		textCellEditor.setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value){
				IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				LabOrder order = (LabOrder) selection.getFirstElement();
				if (order != null && value instanceof String) {
					if (order.getLabItem().getTyp() == typ.NUMERIC
						|| order.getLabItem().getTyp() == typ.ABSOLUTE) {
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
	
	protected void setUpCellEditor(TreeViewer viewer){
		// set up validation of the cell editors
		textCellEditor = new TextCellEditor(viewer.getTree());

		textCellEditor.addListener(new ICellEditorListener() {
			@Override
			public void editorValueChanged(boolean oldValidState, boolean newValidState){
				if (newValidState) {
					textCellEditor.getControl().setBackground(
						Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				} else {
					textCellEditor.getControl().setBackground(
						Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
			}
			
			@Override
			public void cancelEditor(){
				textCellEditor.getControl().setBackground(
					Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
			
			@Override
			public void applyEditorValue(){
				textCellEditor.getControl().setBackground(
					Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		
		focusCell = new TreeViewerFocusCellManager(viewer, new FocusCellHighlighter(viewer) {
			
		});
		
		ColumnViewerEditorActivationStrategy actSupport =
			new ColumnViewerEditorActivationStrategy(viewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event){
					return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.KEYPAD_CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
				}
			};
		
		TreeViewerEditor.create(viewer, focusCell, actSupport, ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}

	@Override
	protected boolean canEdit(Object element){
		return (element instanceof LabOrder)
			&& (((LabOrder) element).getLabItem().getTyp() != typ.FORMULA);
	}

	@Override
	protected CellEditor getCellEditor(Object element){
		if (element instanceof LabOrder) {
			LabItem labItem = ((LabOrder) element).getLabItem();
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
		if (element instanceof LabOrder) {
			LabItem labItem = ((LabOrder) element).getLabItem();
			if (labItem.getTyp() == typ.DOCUMENT) {
				return "Doc"; //$NON-NLS-1$
			} else if (labItem.getTyp() == typ.TEXT) {
				LabResult result = ((LabOrder) element).getLabResult();
				if (result != null) {
					return result.getComment();
				}
			} else {
				LabResult result = ((LabOrder) element).getLabResult();
				if (result != null) {
					return result.getResult();
				}
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	protected void setValue(Object element, Object value){
		if (element instanceof LabOrder && value != null) {
			LabResult result = ((LabOrder) element).getLabResult();
			if (result == null) {
				result = createResult((LabOrder) element, LabOrder.getOrCreateManualLabor());
			}
			
			if (result.getItem().getTyp() == typ.TEXT) {
				result.setResult("Text"); //$NON-NLS-1$
				result.set(LabResult.COMMENT, value.toString());
				((LabOrder) element).setState(LabOrder.State.DONE);
			} else if (result.getItem().getTyp() == typ.DOCUMENT) {
				// dont know what todo ...
			} else {
				result.setResult(value.toString());
				((LabOrder) element).setState(LabOrder.State.DONE);
			}
			int columnIdx = focusCell.getFocusCell().getColumnIndex();
			ViewerRow row = focusCell.getFocusCell().getViewerRow();
			ViewerRow nextRow = row.getNeighbor(ViewerRow.BELOW, true);
			getViewer().refresh();
			if (nextRow != null) {
				getViewer().setSelection(new StructuredSelection(nextRow.getElement()), true);
				getViewer().editElement(nextRow.getElement(), columnIdx);
			}
		}
	}

	private LabResult createResult(LabOrder order, Kontakt origin){
		LabResult result = order.createResult(origin);
		result.setTransmissionTime(new TimeTool());
		return result;
	}
}
