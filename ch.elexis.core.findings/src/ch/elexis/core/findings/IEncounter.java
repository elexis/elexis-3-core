package ch.elexis.core.findings;

public interface IEncounter extends IFinding {
	public String getConsultationId();
	
	public void setConsultationId(String consultationId);
	
	public String getServiceProviderId();
	
	public void setServiceProviderId(String serviceProviderId);
}
