package ch.elexis.core.model.tasks;

public class TaskException extends Exception {
	
	public static final int EXECUTION_REJECTED = 1;
	public static final int RWC_INVALID_ID = 2;
	public static final int RWC_NO_INSTANCE_FOUND = 3;
	public static final int PERSISTENCE_ERROR = 4;
	public static final int PARAMETERS_MISSING = 5;
	public static final int EXECUTION_ERROR = 6;
	public static final int TRIGGER_REGISTER_ERROR = 7;
	public static final int TRIGGER_NOT_AVAILABLE = 8;
	
	private static final long serialVersionUID = -6228358636762420555L;
	
	private final int exceptionCode;
	
	public TaskException(int exceptionCode, Throwable re){
		super(re);
		this.exceptionCode = exceptionCode;
	}
	
	public int getExceptionCode(){
		return exceptionCode;
	}
}
