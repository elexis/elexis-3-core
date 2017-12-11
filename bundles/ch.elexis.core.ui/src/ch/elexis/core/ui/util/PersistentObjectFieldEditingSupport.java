package ch.elexis.core.ui.util;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import ch.elexis.data.PersistentObject;

public class PersistentObjectFieldEditingSupport extends EditingSupport {
	
	private String field;
	private ColumnViewer viewer;
	private TextCellEditor editor;
	
	public PersistentObjectFieldEditingSupport(TableViewer viewer, String field){
		super(viewer);
		this.field = field;
		this.viewer = viewer;
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
		return ((PersistentObject) element).get(field);
	}
	
	@Override
	protected void setValue(Object element, Object value){
		((PersistentObject) element).set(field, String.valueOf(value));
		viewer.update(element, null);
	}
	
}
