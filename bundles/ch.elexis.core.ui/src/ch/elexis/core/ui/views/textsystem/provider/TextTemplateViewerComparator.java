package ch.elexis.core.ui.views.textsystem.provider;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import ch.elexis.core.ui.views.textsystem.model.TextTemplate;

public class TextTemplateViewerComparator extends ViewerComparator {
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;
	private int propertyIndex;
	
	public TextTemplateViewerComparator(){
		this.propertyIndex = 0;
		direction = DESCENDING;
	}
	
	public int getDirection(){
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}
	
	public void setColumn(int column){
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		TextTemplate template1 = (TextTemplate) e1;
		TextTemplate template2 = (TextTemplate) e2;
		
		int rc = 0;
		switch (propertyIndex) {
		case 1:
			rc = template1.getName().compareTo(template2.getName());
			break;
		case 2:
			rc = template1.getMimeTypePrintname().compareTo(template2.getMimeTypePrintname());
			break;
		case 3:
			rc = template1.getMandantLabel().compareTo(template2.getMandantLabel());
			break;
		case 4:
			if (template1.askForAddress() != template2.askForAddress()) {
				rc = (template1.askForAddress() ? 1 : -1);
			}
			break;
		case 5:
			if (!template1.getDescription().isEmpty() || !template2.getDescription().isEmpty()) {
				rc = template1.getDescription().compareToIgnoreCase(template2.getDescription());
			}
			break;
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
}
