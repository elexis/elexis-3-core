package at.medevit.elexis.text.docx.util;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.Text;

public class FindTextVisitor extends TraversalUtilVisitor<Text> {

	private List<Text> foundElements = new ArrayList<>();
	private String value;

	public FindTextVisitor(String value) {
		this.value = value;
	}

	public FindTextVisitor() {
		// no value
	}

	@Override
	public void apply(Text element) {
		if (value != null) {
			if (element != null && element.getValue() != null && element.getValue().equals(value)) {
				foundElements.add(element);
			}
		} else {
			foundElements.add(element);
		}
	}

	public List<Text> getFound() {
		return foundElements;
	}
}
