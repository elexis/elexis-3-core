package ch.elexis.core.model.message;

import ch.elexis.core.model.IMessageParty;

public class MessageParty implements IMessageParty {
	
	private final String identifier;
	private final int type;
	
	public static class MessagePartyType {
		public static final int USER = 0;
		public static final int STATION = 1;
	}
	
	public MessageParty(String identifier){
		this(identifier, MessagePartyType.USER);
	}
	
	public MessageParty(String identifier, int type){
		this.identifier = identifier;
		this.type = type;
	}
	
	@Override
	public String getIdentifier(){
		return identifier;
	}
	
	@Override
	public int getType(){
		return type;
	}
	
}