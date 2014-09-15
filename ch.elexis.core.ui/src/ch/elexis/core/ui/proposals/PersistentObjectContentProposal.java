package ch.elexis.core.ui.proposals;

import org.eclipse.jface.fieldassist.ContentProposal;

public class PersistentObjectContentProposal<T> extends ContentProposal {

	private final T persistentObject;
	
	public PersistentObjectContentProposal(String label, T persistentObject){
		super(label, null);
		this.persistentObject = persistentObject;
	}

	public T getPersistentObject(){
		return persistentObject;
	}
	
}
