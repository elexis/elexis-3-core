package ch.elexis.core.findings;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IFinding {
	
	public enum RawContentFormat {
		FHIR_JSON, FHIR_XML
	}

	public String getId();
	
	public String getPatientId();
	
	public void setPatientId(String patientId);
	
	public List<ICoding> getCoding();
	
	public void addCoding(ICoding coding);
	
	public Optional<LocalDateTime> getEffectiveTime();
	
	public void setEffectiveTime(LocalDateTime time);
	
	public Optional<String> getText();
	
	public RawContentFormat getRawContentFormat();

	public String getRawContent();

	public void setRawContent(String content);

	default Date getDate(LocalDateTime localDateTime){
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	default LocalDateTime getLocalDateTime(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
}
