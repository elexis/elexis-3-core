package ch.elexis.core.ui.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.apache.commons.beanutils.BeanUtils;
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

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public class ReflectiveEditingSupport extends EditingSupport {
	
	private TextCellEditor editor;
	private final String field;
	
	private Logger log = LoggerFactory.getLogger(ReflectiveEditingSupport.class);
	private IModelService modelService;
	
	public ReflectiveEditingSupport(TableViewer columnViewer, String field){
		this(columnViewer, field, null, false);
	}
	
	public ReflectiveEditingSupport(TableViewer columnViewer, String field,
		ICellEditorValidator validator, boolean markValidationFailed){
		super(columnViewer);
		this.field = field;
		this.editor = new TextCellEditor(columnViewer.getTable());
		if (validator != null)
		{
			editor.setValidator(validator);
			
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
	
	public ReflectiveEditingSupport setModelService(IModelService modelService){
		this.modelService = modelService;
		return this;
	}
	
	@Override
	protected CellEditor getCellEditor(Object element){
		return editor;
	}
	
	@Override
	protected boolean canEdit(Object element){
		return (!Objects.isNull(element) && !(element instanceof String));
	}
	
	@Override
	protected Object getValue(Object element){
		try {
			return BeanUtils.getProperty(element, field);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LoggerFactory.getLogger(getClass())
				.error("Error getting property [" + field + "] of [" + element + "]", e);
		}
		return null;
	}
	
	@Override
	protected void setValue(Object element, Object value){
		if (canEdit(element)) {
			LockResponse lr = LocalLockServiceHolder.get().acquireLock(element);
			if (!lr.isOk()) {
				return;
			}
			try {
				BeanUtils.setProperty(element, field, value);
				if (modelService != null && element instanceof Identifiable) {
					modelService.save((Identifiable) element);
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				LoggerFactory.getLogger(getClass())
					.error("Error setting property [" + field + "] of [" + element + "]", e);
			}
			lr = LocalLockServiceHolder.get().releaseLock(element);
			if (!lr.isOk()) {
				log.warn("Error releasing lock for [{}]: {}", element, lr.getStatus());
			}
			getViewer().refresh(true);
		}
	}
}
