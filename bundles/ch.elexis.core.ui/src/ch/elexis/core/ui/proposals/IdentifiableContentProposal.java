package ch.elexis.core.ui.proposals;

import org.eclipse.jface.fieldassist.ContentProposal;

public class IdentifiableContentProposal<T> extends ContentProposal {
	
	private final T identifiable;
	
	public IdentifiableContentProposal(String label, T identifiable){
		super(label, null);
		this.identifiable = identifiable;
	}
	
	public T getIdentifiable(){
		return identifiable;
	}
	
}
