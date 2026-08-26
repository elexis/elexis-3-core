package ch.elexis.core.fhir.mapper.r4;

public class AttributeMapperException extends RuntimeException {

	private static final long serialVersionUID = 1470937277085473595L;

	private final String severity;
	private final int code;

	public AttributeMapperException(String severity, String message, int code) {
		super(message);
		this.severity = severity;
		this.code = code;
	}

	public String getSeverity() {
		return severity;
	}

	public int getCode() {
		return code;
	}

}
