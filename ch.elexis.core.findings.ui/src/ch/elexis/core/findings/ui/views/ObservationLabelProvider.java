package ch.elexis.core.findings.ui.views;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.findings.IObservation;

public class ObservationLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		if (element instanceof IObservation) {
			String text = ((IObservation) element).getText().orElse("");
			// remove observation name from text, is already displayed in column header
			int index = text.indexOf(" ");
			if (index != -1) {
				return text.substring(index);
			} else {
				return text;
			}
		}
		return super.getText(element);
	}
}
