package ch.elexis.core.model.message;

import ch.elexis.core.model.IMessageParty;
import ch.elexis.core.model.IUser;

public class MessageParty implements IMessageParty {
	
	private final IUser user;
	private final String stationIdentifier;
	
	public MessageParty(IUser user){
		this.user = user;
		this.stationIdentifier = null;
	}
	
	public MessageParty(String stationIdentifier){
		this.stationIdentifier = stationIdentifier;
		this.user = null;
	}
	
	@Override
	public IUser getUser(){
		return user;
	}
	
	@Override
	public String getStationId(){
		return stationIdentifier;
	}
	
}