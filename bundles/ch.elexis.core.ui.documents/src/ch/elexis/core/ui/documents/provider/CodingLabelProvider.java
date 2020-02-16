package ch.elexis.core.ui.documents.provider;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.findings.ICoding;

public class CodingLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		if (element instanceof ICoding) {
			ICoding coding = (ICoding) element;
			return coding.getDisplay();
		}
		return super.getText(element);
	}
	
}
