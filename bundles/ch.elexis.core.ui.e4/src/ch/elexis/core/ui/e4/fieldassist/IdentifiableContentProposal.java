package ch.elexis.core.ui.e4.fieldassist;

import org.eclipse.jface.fieldassist.ContentProposal;

public class IdentifiableContentProposal<T> extends ContentProposal {

	private final T identifiable;

	private String additionalValue;

	public IdentifiableContentProposal(String label, T identifiable) {
		super(label, null);
		this.identifiable = identifiable;
	}

	public T getIdentifiable() {
		return identifiable;
	}

	public IdentifiableContentProposal<T> withAdditionalValue(String additionalValue) {
		this.additionalValue = additionalValue;
		return this;
	}

	public String getAdditionalValue() {
		return additionalValue;
	}
}
