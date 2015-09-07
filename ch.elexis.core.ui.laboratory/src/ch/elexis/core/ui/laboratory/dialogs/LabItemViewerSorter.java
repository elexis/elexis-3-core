package ch.elexis.core.ui.laboratory.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.elexis.data.LabItem;

public class LabItemViewerSorter extends ViewerSorter {
	private LabItemLabelProvider labelProvider;
	
	public LabItemViewerSorter(LabItemLabelProvider labelProvider){
		this.labelProvider = labelProvider;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		LabItem left = (LabItem) e1;
		LabItem right = (LabItem) e2;

		return labelProvider.getText(left).compareTo(labelProvider.getText(right));
	}
}