package ch.elexis.core.findings.ui.views;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.findings.IObservation;

public class ObservationLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		if (element instanceof IObservation) {
			IObservation observation = ((IObservation) element);
			String text = observation.getText().orElse("");
			// remove observation name from text, is already displayed in column header
			return text.replace(observation.getCoding().get(0).getDisplay(), "");
		}
		return super.getText(element);
	}
}
