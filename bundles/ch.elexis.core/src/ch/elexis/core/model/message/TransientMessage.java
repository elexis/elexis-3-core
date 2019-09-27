package ch.elexis.core.model.message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.elexis.core.model.IUser;

public class TransientMessage {
	
	private final String id;
	private final String sender;
	private final boolean senderIsUser;
	private List<String> receiver = new ArrayList<>();
	private boolean senderAcceptsAnswer;
	private LocalDateTime createDateTime;
	private String messageText;
	private Map<String, String> messageCodes = new HashMap<>();
	private int priority;
	private List<String> preferredTransporters = new ArrayList<>();
	
	public TransientMessage(String sender, boolean senderIsUser, String[] receiver){
		this.sender = sender;
		this.senderIsUser = senderIsUser;
		this.receiver.addAll(Arrays.asList(receiver));
		this.id = UUID.randomUUID().toString();
		senderAcceptsAnswer = true;
		createDateTime = LocalDateTime.now();
		priority = 0;
	}
	
	public String getId(){
		return id;
	}
	
	public String getSender(){
		return sender;
	}
	
	public boolean isSenderIsUser(){
		return senderIsUser;
	}
	
	public List<String> getReceiver(){
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
	
	public void addReceiver(String receiver){
		this.receiver.add(receiver);
	}
	
	public void addReceiver(IUser receiver){
		this.receiver.add(receiver.getId());
	}
	
	public List<String> getPreferredTransporters(){
		return preferredTransporters;
	}
	
}