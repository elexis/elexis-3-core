package ch.elexis.core.ui.laboratory.controls.util;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.laboratory.controls.LaborOrderViewerItem;
import ch.elexis.core.ui.laboratory.controls.Messages;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

import ch.rgw.tools.StringTool;
public class LabOrderEditingSupport extends EditingSupport {
	protected final String SMALLER = "<";
	protected final String BIGGER = ">";

	protected TextCellEditor textCellEditor;
	protected TableViewerFocusCellManager focusCell;
	
	public LabOrderEditingSupport(ColumnViewer viewer){
		super(viewer);
		
		setUpCellEditor(viewer);
		addValidator();
	}
	
	protected void addValidator(){
		textCellEditor.setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value){
				IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				LaborOrderViewerItem viewerItem =
					(LaborOrderViewerItem) selection.getFirstElement();
				if (viewerItem != null && value instanceof String) {
					if (viewerItem.getLabItemTyp() == LabItemTyp.NUMERIC
						|| viewerItem.getLabItemTyp() == LabItemTyp.ABSOLUTE) {
						try {
							String editedValue = (String) value;
							if (editedValue.startsWith(SMALLER) || editedValue.startsWith(BIGGER)) {
								String nrValue =
									editedValue.replace(SMALLER, StringTool.leer).replace(BIGGER, StringTool.leer);
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
	
	protected void setUpCellEditor(ColumnViewer viewer){
		// set up validation of the cell editors
		textCellEditor = new TextCellEditor((Composite) viewer.getControl());

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
		
		focusCell =
			new TableViewerFocusCellManager((TableViewer) viewer, new FocusCellHighlighter(viewer) {
			
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
		
		TableViewerEditor.create((TableViewer) viewer, focusCell, actSupport,
			ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}

	@Override
	protected boolean canEdit(Object element){
		return (element instanceof LaborOrderViewerItem)
			&& (((LaborOrderViewerItem) element).getLabItemTyp() != LabItemTyp.FORMULA);
	}

	@Override
	protected CellEditor getCellEditor(Object element){
		if (element instanceof LaborOrderViewerItem) {
			LaborOrderViewerItem viewerItem = ((LaborOrderViewerItem) element);
			if (viewerItem.getLabItemTyp() == LabItemTyp.DOCUMENT) {
				return null;
			} else {
				return textCellEditor;
			}
		}
		return null;
	}
	
	@Override
	protected Object getValue(Object element){
		if (element instanceof LaborOrderViewerItem) {
			LaborOrderViewerItem viewerItem = (LaborOrderViewerItem) element;
			if (viewerItem.getLabItemTyp() == LabItemTyp.DOCUMENT) {
				return "Doc"; //$NON-NLS-1$
			} else if (viewerItem.getLabItemTyp() == LabItemTyp.TEXT) {
				LabResult result = viewerItem.getLabResult();
				if (result != null) {
					return result.getComment();
				}
			} else {
				LabResult result = viewerItem.getLabResult();
				if (result != null) {
					return result.getResult();
				}
			}
		}
		return StringTool.leer; //$NON-NLS-1$
	}
	
	@Override
	protected void setValue(final Object element, final Object value){
		LaborOrderViewerItem viewerItem = getSelectedItem();
		if (viewerItem instanceof LaborOrderViewerItem && value != null) {
			LabResult result = (LabResult) viewerItem.getLabResult();
			if (result == null) {
				result =
					createResult(viewerItem, LabOrder.getOrCreateManualLabor());
			}
			final LabResult lockResult = result;
			AcquireLockBlockingUi.aquireAndRun(result, new ILockHandler() {
				
				@Override
				public void lockFailed(){
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void lockAcquired(){
					if (lockResult.getItem().getTyp() == LabItemTyp.TEXT) {
						lockResult.setResult("Text"); //$NON-NLS-1$
						lockResult.set(LabResult.COMMENT, value.toString());
						viewerItem.setState(LabOrder.State.DONE);
					} else if (lockResult.getItem().getTyp() == LabItemTyp.DOCUMENT) {
						// dont know what todo ...
					} else {
						lockResult.setResult(value.toString());
						viewerItem.setState(LabOrder.State.DONE);
					}
				}
			});

			int columnIdx = focusCell.getFocusCell().getColumnIndex();
			ViewerRow row = focusCell.getFocusCell().getViewerRow();
			ViewerRow nextRow = row.getNeighbor(ViewerRow.BELOW, true);
			viewerItem.refreshResultString();
			if (nextRow != null) {
				getViewer().setSelection(new StructuredSelection(nextRow.getElement()), true);
				getViewer().editElement(nextRow.getElement(), columnIdx);
			}
		}
	}

	private LaborOrderViewerItem getSelectedItem(){
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			return (LaborOrderViewerItem) ((IStructuredSelection) selection).getFirstElement();
		}
		return null;
	}
	
	private LabResult createResult(LaborOrderViewerItem viewerItem, Kontakt origin){
		LabResult result = viewerItem.createResult(origin);
		result.setTransmissionTime(new TimeTool());
		return result;
	}
}
