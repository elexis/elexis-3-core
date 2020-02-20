package ch.elexis.core.ui.documents.provider;

import org.eclipse.jface.fieldassist.IContentProposal;

import ch.elexis.core.findings.ICoding;

public class CodingContentProposal implements IContentProposal {
	
	private final ICoding coding;
	
	public CodingContentProposal(ICoding coding){
		super();
		this.coding = coding;
	}
	
	@Override
	public String getContent(){
		// TODO Auto-generated method stub
		return coding.getCode();
	}
	
	@Override
	public int getCursorPosition(){
		return 0;
	}
	
	@Override
	public String getLabel(){
		// TODO Auto-generated method stub
		return coding.getDisplay();
	}
	
	@Override
	public String getDescription(){
		// TODO Auto-generated method stub
		return coding.getSystem();
	}
	
	public ICoding getCoding(){
		return coding;
	}
	
}
