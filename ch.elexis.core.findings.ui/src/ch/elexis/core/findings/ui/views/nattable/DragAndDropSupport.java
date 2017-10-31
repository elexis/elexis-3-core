package ch.elexis.core.findings.ui.views.nattable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.graphics.Point;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;

public class DragAndDropSupport implements DragSourceListener, DropTargetListener {
	
	private static final String DATA_SEPARATOR = "|";
	
	private final NatTableWrapper tableWrapper;
	
	private List<Object> draggedObjects;
	
	PersistentObjectFactory factory = new PersistentObjectFactory();
	
	public DragAndDropSupport(NatTableWrapper tableWrapper){
		this.tableWrapper = tableWrapper;
	}
	
	@Override
	public void dragStart(DragSourceEvent event){
		if (this.tableWrapper.getSelection().isEmpty()) {
			event.doit = false;
		} else if (!this.tableWrapper.getNatTable().getRegionLabelsByXY(event.x, event.y)
			.hasLabel(GridRegion.BODY)) {
			event.doit = false;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void dragSetData(DragSourceEvent event){
		// we know that we use the RowSelectionModel with single selection
		StructuredSelection selection = (StructuredSelection) tableWrapper.getSelection();
		
		if (!selection.isEmpty()) {
			this.draggedObjects = new ArrayList<>(selection.toList());
			StringBuilder builder = new StringBuilder();
			for (Object object : draggedObjects) {
				if(builder.length() > 0) {
					builder.append(DATA_SEPARATOR);
				}
				builder.append(getStringForObject(object));
			}
			event.data = builder.toString();
		}
	}
	
	private Object getStringForObject(Object object){
		if (object instanceof PersistentObject) {
			return ((PersistentObject) object).storeToString();
		}
		return object.toString();
	}
	
	@Override
	public void dragFinished(DragSourceEvent event){
		this.draggedObjects = null;
		
		this.tableWrapper.getNatTable().refresh();
	}
	
	@Override
	public void dragEnter(DropTargetEvent event){
		event.detail = DND.DROP_COPY;
	}
	
	@Override
	public void dragLeave(DropTargetEvent event){}
	
	@Override
	public void dragOperationChanged(DropTargetEvent event){}
	
	@Override
	public void dragOver(DropTargetEvent event){}
	
	@Override
	public void drop(DropTargetEvent event){
		String[] data = (event.data != null ? event.data.toString().split("\\" + DATA_SEPARATOR)
				: new String[] {});
		if (data.length > 0) {
			for (String string : data) {
				Object object = getObjectForString(data[0]);
				if (object != null) {
					int rowPosition = getRowPosition(event);
					int columnPosition = getColumnPosition(event);
					// TODO implement add to the data provider ...
				}
			}
		}
	}
	
	private Object getObjectForString(String string){
		// PersistentObject ?
		if(string.contains("::")) {
			return factory.createFromString(string);
		}
		return null;
	}
	
	private int getColumnPosition(DropTargetEvent event){
		Point pt = event.display.map(null, tableWrapper.getNatTable(), event.x, event.y);
		int position = this.tableWrapper.getNatTable().getColumnPositionByX(pt.x);
		return position;
	}
	
	@Override
	public void dropAccept(DropTargetEvent event){}
	
	private int getRowPosition(DropTargetEvent event){
		Point pt = event.display.map(null, tableWrapper.getNatTable(), event.x, event.y);
		int position = this.tableWrapper.getNatTable().getRowPositionByY(pt.y);
		return position;
	}
}