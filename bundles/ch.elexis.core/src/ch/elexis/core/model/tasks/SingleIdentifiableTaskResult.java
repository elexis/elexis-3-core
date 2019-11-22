package ch.elexis.core.model.tasks;

/**
 * Carry the information of a result on a single identifiable with a running task.
 */
public class SingleIdentifiableTaskResult {
	
	private final String code;
	private final String storeToString;
	private final String message;
	private String data;
	
	public SingleIdentifiableTaskResult(String identifiableStoreToString, String resultCode){
		this(identifiableStoreToString, resultCode, null);
	}
	
	public SingleIdentifiableTaskResult(String identifiableStoreToString, String resultCode,
		String message){
		this.storeToString = identifiableStoreToString;
		this.code = resultCode;
		this.message = message;
	}
	
	public String getResultCode(){
		return code;
	}
	
	public String getIdentifiableStoreToString(){
		return storeToString;
	}
	
	public String getResultMessage(){
		return message;
	}
	
	public String getData(){
		return data;
	}
	
	public void setData(String data){
		this.data = data;
	}
	
}
