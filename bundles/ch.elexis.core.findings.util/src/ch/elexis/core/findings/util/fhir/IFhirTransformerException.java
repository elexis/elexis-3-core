package ch.elexis.core.findings.util.fhir;

public class IFhirTransformerException extends RuntimeException {
	
	private static final long serialVersionUID = 1470937277085473595L;
	
	private final String severity;
	private final int code;
	
	public IFhirTransformerException(String severity, String message, int code){
		super(message);
		this.severity = severity;
		this.code = code;
	}
	
	public String getSeverity(){
		return severity;
	}
	
	public int getCode(){
		return code;
	}
	
}
