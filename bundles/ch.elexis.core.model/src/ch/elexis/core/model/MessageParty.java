package ch.elexis.core.model;

public class MessageParty implements IMessageParty {
	
	private final IUser user;
	
	public MessageParty(IUser user){
		this.user = user;
	}
	
	@Override
	public IUser getUser(){
		return user;
	}
	
	@Override
	public String getStationId(){
		// TODO support
		return null;
	}
	
}