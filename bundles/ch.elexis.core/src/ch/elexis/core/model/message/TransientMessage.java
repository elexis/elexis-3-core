package ch.elexis.core.model.message;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.model.IUser;

public class TransientMessage {
	
	private final String sender;
	private final String receiver;
	private boolean senderAcceptsAnswer;
	private LocalDateTime createDateTime;
	private String messageText;
	private Map<String, String> messageCodes = new HashMap<>();
	private int priority;
	/**
	 * Allow this message to leave the internal system, e.g. SMS or mail message
	 */
	private boolean alllowExternal = false;
	
	public TransientMessage(String sender, String receiver){
		this.sender = sender;
		this.receiver = receiver;
		senderAcceptsAnswer = true;
		createDateTime = LocalDateTime.now();
		priority = 0;
	}
	
	public String getSender(){
		return sender;
	}
	
	public String getReceiver(){
		return receiver;
	}
	
	public boolean isSenderAcceptsAnswer(){
		return senderAcceptsAnswer;
	}
	
	public void setSenderAcceptsAnswer(boolean value){
		senderAcceptsAnswer = value;
	}
	
	public LocalDateTime getCreateDateTime(){
		return createDateTime;
	}
	
	public void setCreateDateTime(LocalDateTime value){
		throw new UnsupportedOperationException();
	}
	
	public String getMessageText(){
		return messageText;
	}
	
	public void setMessageText(String value){
		messageText = value;
	}
	
	public Map<String, String> getMessageCodes(){
		return messageCodes;
	}
	
	public void setMessageCodes(Map<String, String> value){
		messageCodes = value;
	}
	
	public int getMessagePriority(){
		return priority;
	}
	
	public void setMessagePriority(int value){
		priority = value;
	}
	
	public void setSender(IUser user){
		throw new UnsupportedOperationException();
	}
	
	public void addMessageCode(String key, String value){
		messageCodes.put(key, value);
	}
	
	public boolean isAlllowExternal(){
		return alllowExternal;
	}
	
	public void setAlllowExternal(boolean alllowExternal){
		this.alllowExternal = alllowExternal;
	}
	
	@Override
	public String toString(){
		return String.format("%s [%s -> %s] %s", createDateTime, sender, receiver, messageText);
	}
	
}