package ch.elexis.core.ui.editors;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.data.PersistentObject;

public class PersistentObjectEditingSupport extends EditingSupport {
	
	private TextCellEditor editor;
	private final String field;
	
	private Logger log = LoggerFactory.getLogger(PersistentObjectEditingSupport.class);
	
	public PersistentObjectEditingSupport(TableViewer columnViewer, String field){
		super(columnViewer);
		this.field = field;
		this.editor = new TextCellEditor(columnViewer.getTable());
	}
	
	public PersistentObjectEditingSupport(TableViewer columnViewer, String field, Class<? extends Serializable> fieldTypeClazz)
	{
		this(columnViewer, field, fieldTypeClazz, false);
	}
	
	public PersistentObjectEditingSupport(TableViewer columnViewer, String field, Class<? extends Serializable> fieldTypeClazz, boolean markValidationFailed){
		this(columnViewer, field);
		if (fieldTypeClazz != null)
		{
			editor.setValidator(new ICellEditorValidator() {
				
				@Override
				public String isValid(Object value) {
					try 
					{
						String val = (String) value;
						fieldTypeClazz.getDeclaredMethod("valueOf", String.class).invoke(null, val);
						return null;
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassCastException e) {
						return "dummy";
					}
				}
			});
			
			if (markValidationFailed)
			{
				editor.addListener(new ICellEditorListener() {
					@Override
					public void editorValueChanged(boolean oldValidState, boolean newValidState){
						if (newValidState) {
							editor.getControl().setBackground(
								Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
						} else {
							editor.getControl().setBackground(
								Display.getCurrent().getSystemColor(SWT.COLOR_RED));
						}
					}
					
					@Override
					public void cancelEditor(){
						editor.getControl().setBackground(
							Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					}
					
					@Override
					public void applyEditorValue(){
						editor.getControl().setBackground(
							Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					}
				});
			}
		}
	}
	
	
	@Override
	protected CellEditor getCellEditor(Object element){
		return editor;
	}
	
	@Override
	protected boolean canEdit(Object element){
		return (!Objects.isNull(element));
	}
	
	@Override
	protected Object getValue(Object element){
		PersistentObject po = (PersistentObject) element;
		if (po == null) {
			return StringConstants.EMPTY;
		}
		return po.get(field);
	}
	
	@Override
	protected void setValue(Object element, Object value){
		PersistentObject po = (PersistentObject) element;
		if (po == null) {
			return;
		}
		LockResponse lr = CoreHub.getLocalLockService().acquireLock(po);
		if (!lr.isOk()) {
			return;
		}
		
		po.set(field, (String) value);
		lr = CoreHub.getLocalLockService().releaseLock(po);
		if (!lr.isOk()) {
			log.warn("Error releasing lock for [{}]: {}", po.getId(), lr.getStatus());
		}
		getViewer().refresh(true);
	}
	
}
