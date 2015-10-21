package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class MedicationTableViewerEditingSupport extends EditingSupport {

	private TextCellEditor editor;
	
	public MedicationTableViewerEditingSupport(TableViewer viewer){
		super(viewer);
		this.editor = new TextCellEditor(viewer.getTable());
	}
	
	@Override
	protected CellEditor getCellEditor(Object element){
		return editor;
	}
	
	@Override
	protected boolean canEdit(Object element){
		return true;
	}
	
	@Override
	protected Object getValue(Object element){
		MedicationTableViewerItem mtvi = (MedicationTableViewerItem) element;
		return mtvi.getBemerkung();
	}
	
	@Override
	protected void setValue(Object element, Object value){
		MedicationTableViewerItem mtvi = (MedicationTableViewerItem) element;
		mtvi.setBemerkung(value.toString());
	}
	
}

