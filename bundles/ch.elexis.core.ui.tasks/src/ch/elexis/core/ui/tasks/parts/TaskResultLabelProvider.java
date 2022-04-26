package ch.elexis.core.ui.tasks.parts;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.ui.icons.Images;

public class TaskResultLabelProvider extends ColumnLabelProvider {

	private static TaskResultLabelProvider instance;

	private TaskResultLabelProvider() {
	}

	public static TaskResultLabelProvider getInstance() {
		if (instance == null) {
			instance = new TaskResultLabelProvider();
		}
		return instance;
	}

	public String getIconURI(Object element) {
		if (element == null) {
			return null;
		}
		Images icon = getImages(element);
		return (icon != null) ? icon.getIconURI() : null;
	}

	@Override
	public Image getImage(Object element) {
		if (element == null) {
			return null;
		}
		Images icon = getImages(element);
		return (icon != null) ? icon.getImage() : null;
	}

	private Images getImages(Object element) {
		ITask task = (ITask) element;
		switch (task.getState()) {
		case IN_PROGRESS:
			return Images.IMG_GEAR;
		case COMPLETED:
			return Images.IMG_TICK;
		case COMPLETED_WARN:
			return Images.IMG_ACHTUNG;
		case COMPLETED_MANUAL:
			return Images.IMG_HAND;
		case CANCELLED:
			return Images.IMG_STOP;
		case FAILED:
			return Images.IMG_AUSRUFEZ;
		case QUEUED:
			return Images.IMG_CLOCK;
		default:
			break;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		if (element == null) {
			return null;
		}
		ITask task = (ITask) element;
		return task.getState().name();
	}

}
