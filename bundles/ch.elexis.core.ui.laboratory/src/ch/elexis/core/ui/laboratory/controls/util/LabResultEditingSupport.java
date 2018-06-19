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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.core.ui.laboratory.controls.Messages;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LabResultEditingSupport extends EditingSupport {
	
	protected final String SMALLER = "<";
	protected final String BIGGER = ">";
	
	protected TextCellEditor textCellEditor;
	protected TreeViewerFocusCellManager focusCell;
	
	private TreeViewerColumn column;
	private LaborResultsComposite composite;

	public LabResultEditingSupport(LaborResultsComposite laborResultsComposite, TreeViewer viewer,
		TreeViewerColumn column){
		super(viewer);
		this.column = column;
		this.composite = laborResultsComposite;
		
		setUpCellEditor(viewer);
		addValidator();
	}
	
	protected void setUpCellEditor(ColumnViewer viewer){
		// set up validation of the cell editors
		textCellEditor = new TextCellEditor((Composite) viewer.getControl());
		
		textCellEditor.addListener(new ICellEditorListener() {
			@Override
			public void editorValueChanged(boolean oldValidState, boolean newValidState){
				if (newValidState) {
					textCellEditor.getControl()
						.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				} else {
					textCellEditor.getControl()
						.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
			}
			
			@Override
			public void cancelEditor(){
				textCellEditor.getControl()
					.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
			
			@Override
			public void applyEditorValue(){
				textCellEditor.getControl()
					.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		
		focusCell =
			new TreeViewerFocusCellManager((TreeViewer) viewer, new FocusCellHighlighter(viewer) {
			
			});
			
		ColumnViewerEditorActivationStrategy actSupport =
			new ColumnViewerEditorActivationStrategy(viewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event){
					return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
							&& event.keyCode == SWT.CR)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
							&& event.keyCode == SWT.KEYPAD_CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
				}
			};
		
		TreeViewerEditor.create((TreeViewer) viewer, focusCell, actSupport,
			ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}
	
	protected void addValidator(){
		textCellEditor.setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value){
				IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				LaborItemResults results = (LaborItemResults) selection.getFirstElement();
				if (results != null && value instanceof String) {
					if (results.getLabItem().getTyp() == LabItemTyp.NUMERIC
						|| results.getLabItem().getTyp() == LabItemTyp.ABSOLUTE) {
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
			if (labItem.getTyp() == LabItemTyp.DOCUMENT) {
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
	protected void setValue(final Object element, final Object value){
		if (element instanceof LaborItemResults && value != null) {
			LabItem labItem = ((LaborItemResults) element).getLabItem();
			if (labItem.getTyp() == LabItemTyp.DOCUMENT) {
				return;
			}
			LabResult result = createResult(labItem, LabOrder.getOrCreateManualLabor());
			final LabResult lockResult = result;
			AcquireLockBlockingUi.aquireAndRun(result, new ILockHandler() {
				
				@Override
				public void lockFailed(){
					// do nothing
					
				}
				
				@Override
				public void lockAcquired(){
					if (lockResult.getItem().getTyp() == LabItemTyp.TEXT) {
						lockResult.setResult("Text"); //$NON-NLS-1$
						lockResult.set(LabResult.COMMENT, value.toString());
					} else if (lockResult.getItem().getTyp() == LabItemTyp.DOCUMENT) {
						// dont know what todo ...
					} else {
						lockResult.setResult(value.toString());
					}
				}
			});
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
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		LabOrder order =
			new LabOrder(CoreHub.actUser, findMandant(patient), patient, item, null,
				LabOrder.getNextOrderId(),
				"Eingabe", now);
		LabResult result = order.createResult(origin);
		order.setState(LabOrder.State.DONE);
		result.setTransmissionTime(now);
		result.setObservationTime(getDate());
		return result;
	}
	
	private Mandant findMandant(Patient patient){
		// lookup mandant of last kons
		Konsultation konsultation = patient.getLastKonsultation();
		if (konsultation != null && konsultation.exists()) {
			Mandant mandant = konsultation.getMandant();
			if (mandant != null && mandant.getId() != null) {
				return mandant;
			}
		}
		// use current mandant
		Mandant mandant = ElexisEventDispatcher.getSelectedMandator();
		return mandant;
	}
}
