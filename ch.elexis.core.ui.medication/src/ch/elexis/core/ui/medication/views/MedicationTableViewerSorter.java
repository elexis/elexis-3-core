package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class MedicationTableViewerSorter extends ViewerSorter {
	
	private TableViewer tv;

	public MedicationTableViewerSorter(TableViewer tv){
		this.tv = tv;
	}
	
	
}
