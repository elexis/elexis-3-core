package ch.elexis.core.findings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IFinding {
	
	public String getId();
	
	public String getPatientId();
	
	public void setPatientId(String patientId);
	
	public List<ICoding> getCoding();
	
	public void addCoding(ICoding coding);
	
	public Optional<LocalDateTime> getEffectiveTime();
	
	public void setEffectiveTime(LocalDateTime time);
	
	public Optional<String> getText();
}
