package ch.elexis.core.ui.reminder.part;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.graphics.Point;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.reminder.part.nattable.ReminderBodyDataProvider;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn.Type;

public class DragAndDropSupport implements DragSourceListener, DropTargetListener {

	private final NatTable natTable;
	private final SelectionLayer selectionLayer;
	private final ReminderBodyDataProvider dataProvider;

	private IReminder draggedReminder;

	public DragAndDropSupport(NatTable natTable, SelectionLayer selectionLayer,
			ReminderBodyDataProvider dataProvider) {
		this.natTable = natTable;
		this.selectionLayer = selectionLayer;
		this.dataProvider = dataProvider;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		if (this.selectionLayer.getSelectedRowCount() == 0) {
			event.doit = false;
		} else if (!this.natTable.getRegionLabelsByXY(event.x, event.y).hasLabel(GridRegion.BODY)) {
			event.doit = false;
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		Optional<IReminder> selection = ContextServiceHolder.get().getTyped(IReminder.class);
		if (selection.isPresent()) {
			this.draggedReminder = selection.get();
			event.data = StoreToStringServiceHolder.get().storeToString(draggedReminder).get();
		}
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
//		this.data.remove(this.draggedReminder);
		this.draggedReminder = null;

		// clear selection
		this.selectionLayer.clear();

		this.natTable.refresh();
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
	}

	@Override
	public void dragOver(DropTargetEvent event) {
	}

	@Override
	public void drop(DropTargetEvent event) {
		if (StringUtils.isNotBlank((String) event.data)) {
			Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString((String) event.data);
			if (loaded.isPresent() && loaded.get() instanceof IReminder) {
				IReminder reminder = (IReminder) loaded.get();
				ReminderColumn dropColumn = dataProvider.getColumns().get(getColumnPosition(event));
				if(dropColumn.getType() == Type.ALL) {
					reminder.setGroup(null);
					reminder.getResponsible().forEach(c -> {
						reminder.removeResponsible(c);
					});
					reminder.setResponsibleAll(true);
				} else if (dropColumn.getType() == Type.USER) {
					reminder.setGroup(null);
					reminder.getResponsible().forEach(c -> {
						reminder.removeResponsible(c);
					});
					reminder.setResponsibleAll(false);
					reminder.addResponsible(dropColumn.getResponsible());
				} else if (dropColumn.getType() == Type.PATIENT && dropColumn.getPatient() != null) {
					reminder.setContact(dropColumn.getPatient());
				} else if (dropColumn.getType() == Type.GROUP) {
					reminder.setResponsibleAll(false);
					reminder.getResponsible().forEach(c -> {
						reminder.removeResponsible(c);
					});
					reminder.setGroup(dropColumn.getGroup());
				} else if (dropColumn.getType() == Type.POPUP) {
					if (reminder.getContact().isPatient()) {
						reminder.setVisibility(Visibility.POPUP_ON_PATIENT_SELECTION);
					}
				}
				CoreModelServiceHolder.get().save(reminder);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, reminder);
			}
		}
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
	}

	private int getColumnPosition(DropTargetEvent event) {
		Point pt = event.display.map(null, this.natTable, event.x, event.y);
		int position = this.natTable.getColumnPositionByX(pt.x);
		return position;
	}

}
