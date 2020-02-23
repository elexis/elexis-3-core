package ch.elexis.core.ui.documents.provider;

import org.eclipse.jface.fieldassist.ContentProposal;

import ch.elexis.core.findings.ICoding;

public class CodingContentProposal extends ContentProposal {
	
	private final ICoding coding;
	
	public CodingContentProposal(String label, ICoding coding){
		super(label, null);
		this.coding = coding;
	}
	
	public ICoding getCoding(){
		return coding;
	}
}
